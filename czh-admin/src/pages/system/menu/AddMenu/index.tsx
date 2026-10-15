import React, {forwardRef, useState, useEffect, useImperativeHandle, useMemo} from 'react';
import {Button, Form, Input, Select, Switch, App, message} from 'antd';
import icon from '../icon.json';
import {addMenuApi, editMenuApi, getMenusByPidApi} from "../../../../api/menu/MenuApi";
import {GetMenusByPidVo} from "../../../../api/menu/MenuVo";
import {HttpResponse} from "../../../../util/request";
import {CommonFormProps} from "../../../../component/CZHModal/FormModal";

const { Option } = Select;
const levelList = [
    { value: 1, label: '一级菜单' },
    { value: 2, label: '二级菜单' },
    { value: 3, label: '三级菜单' },
];
export interface AddMenuProps extends CommonFormProps{
    type: 'add' | 'edit',
    data?:any,
    level: number,
    onOk?: () => void,
}
const Index = (_props: AddMenuProps, ref: any) => {

    const [firMenu, setFirMenu] = useState<GetMenusByPidVo[]>([]);
    const [secMenu, setSecMenu] = useState<GetMenusByPidVo[]>([]);
    const [form] = Form.useForm();
    const watchPath = Form.useWatch('path', form) || '';
    const watchFid = Form.useWatch('fid', form);
    const watchSid = Form.useWatch('sid', form);
    const watchLevel = Form.useWatch('level', form);
    const watchDisplay = Form.useWatch('display', form);
    const preview = useMemo(() => {
        if (!watchDisplay) return '隐藏菜单可不配置前端路由';
        let parentPath = '';
        if (watchLevel === 2) parentPath = firMenu.find(m => m.id === watchFid)?.path || '';
        else if (watchLevel === 3) {
            const fir = firMenu.find(m => m.id === watchFid)?.path || '';
            const sec = secMenu.find(m => m.id === watchSid)?.path || '';
            parentPath = [fir, sec].filter(Boolean).join('/');
        }
        const full = [parentPath, watchPath].filter(Boolean).join('/');
        return full ? `最终访问地址：/#/admin/${full}` : '请填写前端路由';
    }, [watchDisplay, watchLevel, watchFid, watchSid, watchPath, firMenu, secMenu]);
    useEffect(() => {
        if (_props.level > 1) {
            getMenus(1, 0);
            if (_props.level > 2) {
                getMenus(2, _props.data.fid)
            }
        }
    }, []);
    // 通过pid获取目录
    const getMenus = (type: number, pid: number = 0) => {
        getMenusByPidApi(pid).then(res => {
            if (res.code === 200) {
                if (type === 1) {
                    setFirMenu(res.data);
                } else if (type === 2) {
                    setSecMenu(res.data);
                }
            }
        })
    }
    const onFinish = (data: any) => {
       _props.loading?.(true)
        if (data.level === 2) {
            data.pid = data.fid;
            data.icon = '';
        } else if (data.level === 3) {
            data.pid = data.sid;
            data.icon = '';
        } else {
            data.pid = 0;
        }
        data.display = data.display ? 1 : 0;
        data.path = data.path || '';
        data.route = data.route || '';
        let api:any=addMenuApi;
        if (_props.type == 'edit') {  //编辑
            api=editMenuApi;
            data.id = _props.data.id;
        }
        api(data).then((res:HttpResponse<any>) => {
            if (res.code === 200) {
                message.success(res.msg, 1.2)
                _props.close?.()
                _props.onOk && _props.onOk();
            } else {
                message.error(res.msg, 1.2)
            }
        }).finally(() => {
            _props.loading?.(false);
        });
    }
    useImperativeHandle(ref, () => ({
        form,
    }))
    return (
        <Form
            onFinish={onFinish}
            form={form}
            autoComplete="off"
            labelCol={{ flex: '82px' }}
            initialValues={{
                level: _props.level,
                fid: _props.data?.fid,
                sid: _props.data?.sid,
                display: true,
                icon: _props.data?.icon,
                name: _props.data?.name,
                path: _props.data?.path,
                route: _props.data?.route,
                sort: _props.data?.sort || 1,
            }}
        >
            <Form.Item label='菜单等级' name='level' required>
                <Select
                    options={levelList}
                    placeholder='请选择菜单等级'
                    disabled
                />
            </Form.Item>
            <Form.Item noStyle shouldUpdate={(prev, cur) => prev.level != cur.level}>
                {({ getFieldValue }) => (
                    <React.Fragment>
                        {getFieldValue('level') === 1 && <Form.Item label='图标' name='icon' required>
                            <Select placeholder='请选择图标'>
                                {icon.map((item, index) => (
                                    <Option value={item.value} key={String(index)}>
                                        <span className={`iconfont ${item.value}`}></span>
                                    </Option>
                                ))}
                            </Select>
                        </Form.Item>}
                        {getFieldValue('level') > 1 && <Form.Item label='一级菜单' name='fid' required>
                            <Select options={firMenu} placeholder='请选择一级菜单' fieldNames={{ label: 'name', value: 'id' }}
                            disabled={getFieldValue('level') >1} />
                        </Form.Item>}
                        {getFieldValue('level') > 2 && <Form.Item label='二级菜单' name='sid' required>
                            <Select options={secMenu} placeholder='请选择二级菜单' fieldNames={{ label: 'name', value: 'id' }} />
                        </Form.Item>}
                    </React.Fragment>
                )}
            </Form.Item>
            <Form.Item label="菜单名称" name="name" rules={[{ required: true, message: '请输入菜单名称！' }]}>
                <Input placeholder='请输入菜单名称' />
            </Form.Item>
            {/* BE-32：path 决定侧边栏链接，route 决定权限串，两者含义完全不同，
                过去 route 只有「请输入后端路由」六个字，填错不会有任何提示。 */}
            <Form.Item
                label="前端页面路由（决定侧边栏链接）"
                name="path"
                dependencies={['display']}
                rules={[
                    ({ getFieldValue }) => ({
                        validator(_, value) {
                            if (getFieldValue('display') && (!value || !value.trim())) {
                                return Promise.reject(new Error('显示菜单必须配置前端路由'));
                            }
                            if (value && value.trim() && !/^[a-zA-Z0-9_-]+(\/[a-zA-Z0-9_-]+)*$/.test(value.trim())) {
                                return Promise.reject(new Error('仅允许字母、数字、-、_，多段用 / 分隔'));
                            }
                            return Promise.resolve();
                        },
                    }),
                ]}
                extra={preview || undefined}
            >
                <Input placeholder='可多段，如 admin/AdminList（不要带前导 /，也不要填接口地址）' />
            </Form.Item>
            <Form.Item
                label="后端接口路径（决定权限，须与接口 URL 完全一致）"
                name="route"
                rules={[
                    // BE-32：这里刻意用 warningOnly（黄色警告，不阻塞提交）而不是强制正则。
                    // 查证结果（sql/czh.sql 的 czh_menu 种子数据）：
                    //   27 条非空 route 全部以 /admin/ 开头，但 id=1「系统设置」与 id=30「数据库备份」
                    //   两条一级/分组菜单的 route 是空串 —— 空串是合法取值（权限由其子菜单承载）。
                    // 因此"必须以 /admin/ 开头"的强制规则会阻塞合法录入；且本模板会被 fork，
                    // 下游完全可能出现 /api/ 前缀（仓库里就有 api 模块），硬编码 /admin/ 更不合适。
                    {
                        warningOnly: true,
                        validator(_, value) {
                            const v = (value || '').trim();
                            if (!v) {
                                return Promise.resolve();
                            }
                            if (!/^\/[A-Za-z0-9_\-/]+$/.test(v)) {
                                return Promise.reject(
                                    new Error('接口路径应以 / 开头，且只含字母、数字、-、_、/（如 /admin/role/roleList）')
                                );
                            }
                            if (!v.startsWith('/admin/')) {
                                return Promise.reject(
                                    new Error('当前项目菜单 route 均以 /admin/ 开头；前缀不一致会导致权限串匹配不上，接口返回 403')
                                );
                            }
                            return Promise.resolve();
                        },
                    },
                ]}
                extra="对应后端 Controller 的实际路径，如 /admin/role/roleList；一级分组菜单可留空"
            >
                <Input placeholder='如 /admin/role/roleList（分组菜单可留空）' />
            </Form.Item>
            <Form.Item label="排序" name="sort">
                <Input placeholder='值越小越靠前' />
            </Form.Item>
            <div className='flexCenter'>
                <Form.Item name='display' label='显示状态：' valuePropName="checked" style={{ flex: 1 }}>
                    <Switch checkedChildren="显示" unCheckedChildren="隐藏" />
                </Form.Item>
            </div>
            {/*<Button loading={loading} type="primary" htmlType='submit' className='marglauto block margt20'>确定</Button>*/}
        </Form>
    )
};

export default forwardRef(Index);
