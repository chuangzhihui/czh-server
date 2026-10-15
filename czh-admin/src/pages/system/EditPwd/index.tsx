import React, {forwardRef, useEffect, useImperativeHandle, useState} from 'react';
import {Button, Form, Input, App, message} from 'antd';
import Helper from "../../../util/Helper";
import {editPwdApi, getPwdRuleApi} from "../../../api/admin/AdminApi";
import {CommonFormProps} from "../../../component/CZHModal/FormModal";

const Index = (props:CommonFormProps,ref:any) => {
    const [form] = Form.useForm();
    const [pwdReg,setPwdReg]=useState<string>("");
    const [pwdRegTip,setPwdRegTip]=useState<string>("");
    useEffect(()=>{
        //获取密码正则表达式
       getPwdRuleApi().then((res:any) => {
            if (res.code === 200) {
                setPwdReg(res.data.pwdReg)
                setPwdRegTip(res.data.pwdRegDesc)
            } else {
                message.error(res.msg, 1.2)
            }
        })
    },[])
    useImperativeHandle(ref, () => ({
        form,
    }))
    const onFinish = (data: any) => {
        props.loading?.(true)
        editPwdApi(data).then((res:any) => {
            props.loading?.(false)
            if (res.code === 200) {
                localStorage.removeItem('czhToken')
                message.success('修改成功，请重新登录！', 1.2, () => {
                    window.location.href = ''
                })
            } else {
                message.error(res.msg, 1.2)
            }
        })
    }
    return (
        <Form
            form={form}
            name="validateOnly"
            autoComplete="off"
            labelCol={{ flex: '82px' }}
            onFinish={onFinish}
        >
            <Form.Item label='原密码' name='oldPwd' rules={[{ required: true, message: '请输入原密码' }]}>
                <Input.Password placeholder='请输入6-18位原密码' />
            </Form.Item>
            <Form.Item extra={pwdRegTip} label='新密码' name='password' rules={[{ required: true, message: '请输入新密码' },( { getFieldValue }) => ({
                validator(_, value) {
                // var myreg = /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,20}$/;
                if (!value || Helper.regCheck(pwdReg,value)) {
                    return Promise.resolve();
                }
                return Promise.reject(new Error(pwdRegTip));
            }
            })]}>
                <Input.Password placeholder='请设置新密码' />
            </Form.Item>
            <Form.Item label='确认密码' name='pwd1' rules={[{ required: true,message: '请再次输入新密码' }, ({ getFieldValue }) => ({
                validator(_, value) {
                    if (!value || getFieldValue('password') === value) {
                        return Promise.resolve();
                    }
                    return Promise.reject(new Error('两次密码不一致!'));
                }
            })]} >
                <Input.Password placeholder='请再次输入新密码' />
            </Form.Item>
        </Form>
    )
}

export default forwardRef(Index);
