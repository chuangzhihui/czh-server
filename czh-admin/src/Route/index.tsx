import React, { lazy, Suspense, useCallback, useEffect, useMemo, useRef } from 'react';
import { useRoutes } from 'react-router-dom';
import { Breadcrumb, Dropdown, theme } from 'antd';
import { DownOutlined } from '@ant-design/icons';
import { Link, Navigate, Route, Routes, useNavigate, useOutletContext } from 'react-router-dom';
import Loading from '../component/Loading';
import './index.scss';

const Login = lazy(() => import('../pages/Login'));
const Layout = lazy(() => import('../pages/Layout'));
const Page404 = lazy(() => import('../component/Page404'));

/** 页面组件必须 forwardRef 并暴露 refresh（见《前端开发规范》7.1） */
export interface AdminPageRef {
    refresh: () => void;
}

export type AdminPageComponent = React.LazyExoticComponent<
    React.ForwardRefExoticComponent<Record<string, unknown> & React.RefAttributes<AdminPageRef>>
>;

/** 菜单节点：parents = 父链 key；childKeys = 直接子级 key（面包屑下拉用） */
export interface MenuNode {
    key: string;
    label: string;
    path: string;
    parents: string[];
    childKeys: string[];
}

/** Layout -> AdminRoutes 传递的外壳上下文（面包屑 / 菜单 / 首跳都由菜单树驱动） */
export interface AdminShellContext {
    /** key -> 菜单节点（含父链与子级，用于面包屑层级与下拉切换） */
    menuMap: Record<string, MenuNode>;
    /** 登录后默认进入的菜单 path（首个叶子），/admin 根路径重定向到它 */
    firstPath: string;
    /** 页面向外壳注册自己的 refresh，供顶栏刷新按钮调用 */
    registerRefresh: (fn: (() => void) | null) => void;
}

/**
 * 路由基表：key 由 pages 目录自动派生（import.meta.glob），等于「src/pages 之后的相对路径」，
 * 须与后台「菜单管理」里配置的 path 逐段一致（大小写敏感）。
 * 例：src/pages/system/AdminList → key 'system/AdminList'。
 * 新增页面只需在 pages/<顶级菜单>/<页面>/ 下建目录 + index.tsx，无需改这里；
 * Login / Layout 为特殊页已排除；非菜单子页（增改表单、弹窗）进入表但无对应菜单 path，不会被路由命中，无害。
 */
const pageModules = import.meta.glob<{ default: React.ComponentType<any> }>('../pages/**/index.tsx');
export const Components: Record<string, AdminPageComponent> = Object.fromEntries(
    Object.entries(pageModules)
        .map(([file, importer]) => [
            file.replace(/^\.\.\/pages\//, '').replace(/\/index\.tsx$/, ''),
            lazy(importer),
        ])
        .filter(([key]) => key !== 'Login' && key !== 'Layout'),
) as Record<string, AdminPageComponent>;
console.log(Components)
/** 单个页面视图：面包屑（占据原页标题的位置）+ 页面组件；把 refresh 注册给外壳顶栏 */
const PageView = ({ node }: { node: { key: string; label: string; path: string } }) => {
    const { menuMap, registerRefresh } = useOutletContext<AdminShellContext>();
    const { token } = theme.useToken();
    const navigate = useNavigate();
    const Child = (Components[node.path] || Page404) as AdminPageComponent;
    const ref = useRef<AdminPageRef | null>(null);

    /** 面包屑：由菜单树父链自动生成完整层级路径，如「系统设置 / 管理员列表」 */
    const crumbs = useMemo(() => {
        const item = menuMap[node.key];
        if (!item) return [{ key: node.key, label: node.label, path: node.path, parents: [], childKeys: [] as string[] }];
        return [...item.parents, node.key].map(k => menuMap[k]).filter(Boolean);
    }, [menuMap, node]);

    /** 所有可选的一级菜单 */
    const topNodes = useMemo(
        () => Object.values(menuMap).filter(item => item.parents.length === 0),
        [menuMap],
    );

    /** 某节点的直接子级 */
    const childNodes = useCallback(
        (key: string) => (menuMap[key]?.childKeys || []).map(k => menuMap[k]).filter(Boolean),
        [menuMap],
    );

    /** 跳转：节点自身不可访问（如存在子菜单的一级）时，自动选中其下第一个可访问子级 */
    const jumpTo = useCallback(
        (key: string) => {
            const visit = (item: MenuNode | undefined): boolean => {
                if (!item) return false;
                if (item.path) {
                    navigate(`/admin/${item.path}`);
                    return true;
                }
                return item.childKeys.some(k => visit(menuMap[k]));
            };
            visit(menuMap[key]);
        },
        [menuMap, navigate],
    );

    useEffect(() => {
        registerRefresh(() => ref.current?.refresh());
        return () => registerRefresh(null);
    }, [registerRefresh]);

    return (
        <div className='app__page'>
            <div className='pageCrumb'>
                <span className='pline' style={{ background: token.colorPrimary }} />
                <Breadcrumb
                    items={crumbs.map((item, i) => {
                        const last = i === crumbs.length - 1;
                        // 一级：hover 下拉所有一级菜单；二级：hover 下拉该一级下所有二级菜单；
                        // 三级及以后（一般从二级页面带参数进入）不做切换
                        if (i > 1) return { title: <span style={last ? { color: token.colorPrimary } : undefined}>{item.label}</span> };
                        const options = i === 0 ? topNodes : childNodes(crumbs[0].key);
                        if (!options.length) return { title: item.label };
                        return {
                            title: (
                                <Dropdown
                                    trigger={['hover']}
                                    menu={{
                                        items: options.map(opt => ({ key: opt.key, label: opt.label })),
                                        onClick: ({ key }) => jumpTo(key),
                                    }}
                                >
                                    <span className='pageCrumb__link' style={last ? { color: token.colorPrimary } : undefined}>
                                        {item.label}
                                        <DownOutlined className='pageCrumb__arrow' />
                                    </span>
                                </Dropdown>
                            ),
                        };
                    })}
                />
            </div>
            <Child ref={ref} />
        </div>
    );
};

/**
 * /admin 的子路由：按后端菜单动态生成。
 * Layout 壳不重建，只有这里的内容随路由切换 —— 切页不再整壳闪烁。
 */
const AdminRoutes = () => {
    const { menuMap, firstPath } = useOutletContext<AdminShellContext>();

    const routes = useMemo(
        () =>
            Object.values(menuMap)
                .filter(node => node.childKeys.length === 0)
                .map(node => ({
                    path: node.path,
                    key: node.key,
                    element: (
                        <Suspense fallback={<Loading />}>
                            <PageView node={node} />
                        </Suspense>
                    ),
                })),
        [menuMap],
    );

    if (!routes.length) return <Loading />;

    return (
        <Routes>
            {firstPath && <Route index element={<Navigate to={`/admin/${firstPath}`} replace />} />}
            {routes.map(r => (
                <Route key={r.key} path={r.path} element={r.element} />
            ))}
            <Route
                path='*'
                element={
                    <Suspense fallback={<Loading />}>
                        <Page404 />
                    </Suspense>
                }
            />
        </Routes>
    );
};

export const getRouter = () => [
    { path: '/', element: <Login /> },
    { path: '/login', element: <Login /> },
    {
        path: '/admin',
        element: <Layout />,
        children: [
            // /admin 根路径与 /admin/xxx 都交给 AdminRoutes（内部按菜单生成页面路由并首跳）
            { index: true, element: <AdminRoutes /> },
            { path: '*', element: <AdminRoutes /> },
        ],
    },
    { path: '*', element: <Page404 /> },
];

const Index = () => useRoutes(getRouter());

export default Index;
