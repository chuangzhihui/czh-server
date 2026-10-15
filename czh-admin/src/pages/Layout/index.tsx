import React, { Suspense, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
    App,
    Drawer,
    Dropdown,
    Grid,
    Layout,
    Menu,
    Typography,
    type MenuProps,
} from 'antd';
import { Link, Outlet, useLocation } from 'react-router-dom';
import Page404 from '../../component/Page404';
import type { AdminShellContext, MenuNode } from '../../Route';
import { getLoginInfoApi, logoutApi } from '../../api/admin/AdminApi';
import type { AuthMenuVo } from '../../api/admin/AdminVo';
import FormModal from '../../component/CZHModal/FormModal';
import Loading from '../../component/Loading';
import { ThemeContext, useThemeSetter } from '../../util/ThemeContext';
import EditPwd from '../system/EditPwd';
import UserInfo from '../system/UserInfo';
import SetColor from '../system/SetColor';
import defaultAvatar from '../../static/default.png';
import './index.scss';

const { Header, Content, Sider } = Layout;

type MenuMap = Record<string, MenuNode>;

const Index = () => {
    const { message, modal } = App.useApp();
    const themeSetter = useThemeSetter();
    const screens = Grid.useBreakpoint();
    const isMobile = !!screens.xs && !screens.sm;
    const [collapsed, setCollapsed] = useState(false);
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [openKeys, setOpenKeys] = useState<string[]>([]);
    const [menu, setMenu] = useState<MenuProps['items']>([]);
    const [menuMap, setMenuMap] = useState<MenuMap>({});
    const [info, setInfo] = useState({ avatar: defaultAvatar, username: '', systemName: '管理后台' });
    const [username, setUsername] = useState('');
    const [avatar, setAvatar] = useState('');
    const [sysName, setSysName] = useState('管理后台');
    const refreshRef = useRef<(() => void) | null>(null);
    const location = useLocation();

    /** 后端菜单树 -> antd Menu items；叶子节点 label 用 <Link>，点击即路由跳转 */
    const makeMenus = useCallback((sources: AuthMenuVo[], parents: string[] = [], map: MenuMap = {}): MenuProps['items'] =>
        sources.map(source => {
            const key = String(source.id);
            const children = source.child && source.child.length > 0 ? makeMenus(source.child, [...parents, key], map) : undefined;
            map[key] = {
                key,
                label: source.title || '',
                path: source.path || '',
                parents,
                childKeys: (source.child || []).map(item => String(item.id)),
            };
            const icon = parents.length === 0 ? <span className={`iconfont ${source.icon}`} /> : undefined;
            const hasChildren = source.child && source.child.length > 0;
            const label = (!hasChildren && source.path) ? <Link to={`/admin/${source.path}`}>{source.title}</Link> : source.title;
            return { key, icon, label, ...(children ? { children } : {}) };
        }), []);

    const getData = useCallback(() => {
        getLoginInfoApi().then(res => {
            if (res.code !== 200) return;
            setSysName(res.data.name);
            setUsername(res.data.username);
            setAvatar(res.data.avatar);
            setInfo({ avatar: res.data.avatar, username: res.data.username, systemName: res.data.name });
            if (res.data.changePwdType > 0) {
                modal.confirm({
                    title: '警告',
                    content: res.data.changePwdTip,
                    cancelText: '稍后再说',
                    okText: '立即修改',
                    cancelButtonProps: { disabled: res.data.changePwdType === 2 },
                    onOk: () => {
                        FormModal.open({ title: '修改密码', closable: false, children: <EditPwd />, width: 360 });
                    },
                });
            }
            const map: MenuMap = {};
            const items = makeMenus(res.data.menus || [], [], map);
            setMenuMap(map);
            setMenu(items);
        });
    }, [modal, makeMenus]);

    useEffect(() => {
        getData();
    }, [getData]);

    /** 登录后默认进入的页面 path（首个叶子） */
    const firstPath = useMemo(() => {
        const walk = (items: MenuProps['items']): string | undefined => {
            if (!items) return undefined;
            for (const item of items) {
                if (!item) continue;
                const children = (item as { children?: MenuProps['items'] }).children;
                if (children && children.length) {
                    const hit = walk(children);
                    if (hit) return hit;
                } else {
                    const node = menuMap[String(item.key)];
                    if (node?.path) return node.path;
                }
            }
            return undefined;
        };
        return walk(menu);
    }, [menu, menuMap]);

    /** 当前菜单 key：由路由路径派生（location 是唯一事实来源，菜单点击 = 链接跳转） */
    const currentKey = useMemo(() => {
        const full = location.pathname.split('/').slice(2).join('/');
        if (!full) return '';
        return Object.keys(menuMap).find(key => menuMap[key].path === full) ?? '';
    }, [location.pathname, menuMap]);

    /** 选中态跟随路由；打开的分组跟随当前页父链 */
    useEffect(() => {
        const node = menuMap[currentKey];
        if (node?.parents.length) setOpenKeys(node.parents);
    }, [currentKey, menuMap]);

    const onOpenChange = (keys: string[]) => {
        const latestOpenKey = keys.find(key => !openKeys.includes(key));
        setOpenKeys(latestOpenKey ? [latestOpenKey] : []);
    };

    /** 顶栏刷新：当前页面通过外壳上下文注册的 refresh */
    const refresh = () => {
        refreshRef.current?.();
    };

    const loginOut = () => {
        logoutApi();
        localStorage.removeItem('czhToken');
        message.success('再见', 1.2, () => {
            window.location.href = '#/';
        });
    };

    const openEditPwd = () => {
        FormModal.open({ title: '修改密码', children: <EditPwd />, width: 360 });
    };

    const openUserInfo = () => {
        FormModal.open({
            title: '个人信息',
            children: <UserInfo data={info} onOk={() => getData()} />,
            width: 360,
        });
    };

    /** 主题配置：官方 12 套主题卡片 + 实时预览，保存后免刷新换肤。
     *  主题 token 与 antd App 已由 FormModal.open 默认注入（见 component/CZHModal/ModalRoot），
     *  这里只需注入带 setter 的 ThemeContext，否则弹窗内点卡片时 setToken 是空实现、换肤不会生效。
     *  「主题已保存」由这里（主应用的 App holder）提示：弹窗关闭后其独立 root 会被 unmount，
     *  若由 SetColor 内部弹 toast，提示会随弹窗一起消失。 */
    const openSetColor = () => {
        const { token, setToken, saveToken } = themeSetter;
        FormModal.open({
            title: '主题配置',
            width: 1172,
            children: <SetColor systemName={sysName} onOk={() => message.success('主题已保存', 1.2)} />,
            wrap: node => <ThemeContext.Provider value={{ token, setToken, saveToken }}>{node}</ThemeContext.Provider>,
        });
    };

    const userMenuItems = [
        { key: '1', label: <span onClick={openEditPwd}>修改密码</span> },
        { key: '2', label: <span onClick={openUserInfo}>个人信息</span> },
        { key: '3', label: <span onClick={loginOut}>退出登录</span> },
    ];

    const shellContext = useMemo<AdminShellContext>(
        () => ({
            menuMap,
            firstPath: firstPath || '',
            registerRefresh: fn => {
                refreshRef.current = fn;
            },
        }),
        [menuMap, firstPath],
    );

    const siderContent = (
        <>
            <div className='siderCzh__logo'>
                <p className='logoIcon' style={{ background: 'var(--main-colorPrimary)' }}>
                    {sysName ? sysName.substring(0, 2) : ''}
                </p>
                <h1 className='logoName'>{sysName}</h1>
            </div>
            <div className='siderCzh__menus'>
                <Menu
                    mode='inline'
                    selectedKeys={currentKey ? [currentKey] : []}
                    openKeys={openKeys}
                    items={menu}
                    onOpenChange={onOpenChange}
                />
            </div>
        </>
    );

    return (
        <Layout className='app'>
            {!isMobile && (
                <Sider
                    width={240}
                    breakpoint='lg'
                    className='siderCzh'
                    theme='light'
                    trigger={null}
                    collapsible
                    collapsed={collapsed}
                    onBreakpoint={broken => setCollapsed(broken)}
                >
                    {siderContent}
                </Sider>
            )}
            {isMobile && (
                <Drawer
                    open={drawerOpen}
                    placement='left'
                    width={240}
                    onClose={() => setDrawerOpen(false)}
                    styles={{ body: { padding: 0 } }}
                >
                    {siderContent}
                </Drawer>
            )}
            <Layout className='app__body'>
                <Header className='app__header'>
                    <span
                        className={`cursor iconfont ${collapsed ? 'icon-zhankai' : 'icon-shouqi'}`}
                        onClick={() => (isMobile ? setDrawerOpen(true) : setCollapsed(!collapsed))}
                    />
                    <span className='cursor iconfont icon-shuaxin' onClick={refresh} title='刷新' />
                    <div className='headerCzh'>
                        <div className='themeBtn' onClick={openSetColor}>
                            <span className='iconfont icon-zhuti' />
                            <p>主题</p>
                        </div>
                        <img alt='' src={avatar || defaultAvatar} className='avatar' />
                        <Dropdown placement='bottom' menu={{ items: userMenuItems }} arrow>
                            <div className='userBtn cursor'>
                                <Typography.Text className='userName'>{username}</Typography.Text>
                                <span className='iconfont icon-jiantou-shang' />
                            </div>
                        </Dropdown>
                    </div>
                </Header>
                <Content className='app__content'>
                    <div className='app__outlet'>
                        <Suspense fallback={<Loading />}>
                            <Outlet context={shellContext} />
                        </Suspense>
                    </div>
                </Content>
            </Layout>
        </Layout>
    );
};

export default Index;
