import { lazy, Suspense, useEffect, useState } from 'react';
import { App, Button, Form, Input, theme } from 'antd';
import { useNavigate } from 'react-router-dom';
import { getCaptchaApi, getSystemNameApi, loginApi } from '../../api/login/LoginApi';
import type { HttpResponse } from '../../util/request';
import type { LoginDto } from '../../api/login/LoginDto';
import type { AdminLoginVo } from '../../api/login/LoginVo';
import './index.scss';

/** WebGL 背景按需加载：three 单独成 chunk，不拖慢首屏 */
const ThreeBackground = lazy(() => import('../../component/ThreeBackground'));

const Login = () => {
    const navigate = useNavigate();
    const { token: { colorPrimary } } = theme.useToken();
    const { message, modal } = App.useApp();
    const [codeUrl, setCodeUrl] = useState('');
    const [uuid, setUuid] = useState('');
    const [name, setName] = useState('管理后台');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (localStorage.getItem('czhToken')) {
            navigate('/admin', { replace: true });
            return;
        }
        getSystemName();
        getCodeUrl();
    }, [navigate]);

    /** 获取系统名称 */
    const getSystemName = () => {
        getSystemNameApi().then(res => {
            if (res.code === 200) {
                document.title = res.data.name;
                setName(res.data.name);
            }
        });
    };

    /** 获取图形验证码（验证码一次性消费，失败必须重新拉取） */
    const getCodeUrl = () => {
        getCaptchaApi().then(res => {
            if (res.code === 200) {
                setCodeUrl(res.data.img);
                setUuid(res.data.uuid);
            }
        });
    };

    const onFinish = (data: LoginDto) => {
        setLoading(true);
        loginApi({ ...data, uuid }).then((res: HttpResponse<AdminLoginVo>) => {
            if (res.code === 200) {
                localStorage.setItem('czhToken', res.data.token);
                message.success('登录成功', 1.2);
                navigate('/admin', { replace: true });
            } else {
                getCodeUrl();
                // 后端 msg 以 ALERT 开头时走确认弹窗（冻结 / 强制改密码等）
                if (res.msg.startsWith('ALERT')) {
                    modal.confirm({ title: '提示', content: res.msg.replace('ALERT', '') });
                } else {
                    message.error(res.msg, 1.2);
                }
            }
            setLoading(false);
        });
    };

    return (
        <div className='loginPage'>
            <Suspense fallback={null}>
                <ThreeBackground color={colorPrimary} />
            </Suspense>
            <div className='loginBox'>
                <h1>登录到<br />{name}</h1>
                <p className='line' style={{ background: `linear-gradient(to right,${colorPrimary},transparent)` }} />
                <Form autoComplete='off' onFinish={onFinish}>
                    <Form.Item name='username' rules={[{ required: true, message: '请输入账号' }]}>
                        <Input prefix={<span className='iconfont icon-yonghu' />} placeholder='请输入账号' />
                    </Form.Item>
                    <Form.Item name='password' rules={[{ required: true, message: '请输入密码' }]}>
                        <Input.Password
                            prefix={<span className='iconfont icon-mima'  />}
                            type='password'
                            placeholder='请输入密码'
                            autoComplete='new-password'
                        />
                    </Form.Item>
                    <div className='flex'>
                        <Form.Item name='code' className='rowFlex' rules={[{ required: true, message: '请输入图形验证码' }]}>
                            <Input
                                prefix={<span className='iconfont icon-a-yanzhengmaanquan' />}
                                type='text'
                                placeholder='请输入图形验证码'
                                autoComplete='off'
                            />
                        </Form.Item>
                        <div className='code'>
                            <img onClick={getCodeUrl} className='verifyCodeImg' src={codeUrl} alt='验证码' />
                        </div>
                    </div>
                    <Button loading={loading} className='dlbtn' type='primary' htmlType='submit'>登录</Button>
                </Form>
            </div>
        </div>
    );
};

export default Login;
