import React, { forwardRef, useEffect, useImperativeHandle, useState } from 'react';
import { Button, Form, Input, InputNumber, App, Switch } from 'antd';
import { getAuthConfigApi, setAuthConfigApi } from '../../../api/set/SetApi';
import { checkRegExp, isCrontab, isPositiveInteger } from '../../../util/Validator';
import './index.scss';

const Index = (_props: any, ref: any) => {
    const [form] = Form.useForm();
    const { message } = App.useApp();
    const [loading, setLoading] = useState(false);

    useImperativeHandle(ref, () => ({
        refresh,
    }));

    const refresh = () => {
        getAuthConfig();
    };

    useEffect(() => {
        getAuthConfig();
    }, []);

    const getAuthConfig = () => {
        getAuthConfigApi().then((res: any) => {
            if (res.code === 200) {
                res.data.autoBackup = res.data.autoBackup === 1;
                form.setFieldsValue(res.data);
            } else {
                message.error(res.msg);
            }
        });
    };

    const onFinish = (data: any) => {
        setLoading(true);
        data.autoBackup = data.autoBackup ? 1 : 0;
        setAuthConfigApi(data).then((res: any) => {
            setLoading(false);
            if (res.code === 200) {
                message.success(res.msg);
            } else {
                message.error(res.msg);
            }
        }).catch(() => {
            setLoading(false);
        });
    };

    return (
        <div className='authSet'>
            <Form
                form={form}
                name='authConfig'
                layout='vertical'
                autoComplete='off'
                onFinish={onFinish}
            >
                <div className='authSet__body'>
                    {/* 密码策略 */}
                    <section className='authSet__section'>
                        <div className='authSet__section-title'>密码策略</div>
                        <div className='authSet__grid'>
                            <Form.Item label='登录密码正则' name='pwdReg' tooltip='用于校验新密码是否符合规则' rules={[{ required: true, message: '请输入登录密码正则' }, { validator: checkRegExp }]}>
                                <Input allowClear placeholder='请输入登录密码正则规则' />
                            </Form.Item>
                            <Form.Item label='登录密码规则描述' name='pwdRegDesc' rules={[{ required: true, message: '请输入登录密码规则描述' }]}>
                                <Input allowClear placeholder='请输入登录密码规则描述' />
                            </Form.Item>
                            <Form.Item label='密码强制更换周期' name='passMax' rules={[{ required: true, message: '请输入密码多少天后必须强制更换' }, { validator: isPositiveInteger }]}>
                                <InputNumber min={1} precision={0} addonAfter='天' placeholder='请输入天数' />
                            </Form.Item>
                            <Form.Item label='密码到期提醒' name='passWran' tooltip='提前多少天开始提示用户修改密码' rules={[{ required: true, message: '请输入多少天未修改密码开始提示用户修改密码' }, { validator: isPositiveInteger }]}>
                                <InputNumber min={1} precision={0} addonAfter='天' placeholder='请输入天数' />
                            </Form.Item>
                        </div>
                    </section>

                    {/* 登录与会话 */}
                    <section className='authSet__section'>
                        <div className='authSet__section-title'>登录与会话</div>
                        <div className='authSet__grid'>
                            <Form.Item label='连续密码错误冻结' name='failNum' rules={[{ required: true, message: '请输入连续密码错误多少次数后冻结账号' }, { validator: isPositiveInteger }]}>
                                <InputNumber min={1} precision={0} addonAfter='次' placeholder='请输入次数' />
                            </Form.Item>
                            <Form.Item label='错误记录保留时长' name='failNumTime' tooltip='超过该时长后自动清除密码错误记录' rules={[{ required: true, message: '请输入清除密码错误记录的时间(秒)' }, { validator: isPositiveInteger }]}>
                                <InputNumber min={1} precision={0} addonAfter='秒' placeholder='请输入秒数' />
                            </Form.Item>
                            <Form.Item label='静默登录失效' name='timeOut' tooltip='无操作超过该时长后登录失效' rules={[{ required: true, message: '请输入静默多久后登录失效(秒)' }, { validator: isPositiveInteger }]}>
                                <InputNumber min={1} precision={0} addonAfter='秒' placeholder='请输入秒数' />
                            </Form.Item>
                        </div>
                    </section>

                    {/* 数据备份 */}
                    <section className='authSet__section'>
                        <div className='authSet__section-title'>数据备份</div>
                        <div className='authSet__grid'>
                            <Form.Item label='备份数据库规则' name='bakupDbCron' tooltip='cron 表达式，如 0 30 1 * * ?' rules={[{ required: true, message: '请输入备份数据库时机的 cron 表达式' }, { validator: isCrontab }]}>
                                <Input allowClear placeholder='请输入 cron 表达式' />
                            </Form.Item>
                            <Form.Item label='是否开启自动备份' name='autoBackup' valuePropName='checked' rules={[{ required: true, message: '请选择' }]}>
                                <Switch checkedChildren='开启' unCheckedChildren='关闭' />
                            </Form.Item>
                        </div>
                    </section>
                </div>
                <div className='authSet__foot'>
                    <Button loading={loading} type='primary' htmlType='submit'>确定</Button>
                </div>
            </Form>
        </div>
    );
};

export default forwardRef(Index);
