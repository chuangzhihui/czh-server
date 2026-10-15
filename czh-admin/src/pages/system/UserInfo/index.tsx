import React, {forwardRef, useRef, useState, useEffect, useImperativeHandle} from 'react';
import {Button, Form, Input, App} from 'antd';
import CZHFileList, { CZHFileItem} from '../../../component/CZHFileComponent/CZHFileList';
import {editAvatarApi} from "../../../api/admin/AdminApi";
import {CommonFormProps} from "../../../component/CZHModal/FormModal";
import defaultAvatar from '../../../static/default.png';
import './index.scss';

interface types {
    value?: string;
    onChange?: (value: string) => void;
}
// 上传图片组件
const CustomUpload: React.FC<types> = ({ value = '', onChange }) => {
    const triggerChange = (url: string) => {
        onChange?.(url);
    };

    const chooseFile=()=>{
        CZHFileList.open({
            types:[1],
            crop:true,
            cropProps:{
                aspect:1/1,
                showGrid:true,
                children:<></>
            },
            onOk:(files:CZHFileItem[])=>{
                triggerChange(files[0].url)
            }
        })
    }
    return (
        <React.Fragment>
            <div className='editavatar cursor' onClick={chooseFile}>
                <img className='avatarImg' alt='' src={value || defaultAvatar} />
                <span className='avatarTip'>修改头像</span>
            </div>
        </React.Fragment>
    )
}

export interface UserInforProps extends CommonFormProps{
    data:any;
    onOk:()=>void;
}
const Index = (_props: UserInforProps, ref: any) => {
    const { message } = App.useApp();
    const [form] = Form.useForm();
    useImperativeHandle(ref, () => ({
        form,
    }))
    useEffect(() => {
        form.setFieldsValue(_props.data)
    }, [])
    const onFinish = (data: any) => {
        _props.loading?.(true)
        editAvatarApi(data).then(res => {
            _props.loading?.(false)
            if (res.code == 200) {
                message.success(res.msg, 1.2)
                _props.close?.();
                _props.onOk()
            } else {
                message.error(res.msg, 1.2)
            }
        })
    }
    return (
        <Form
            form={form}
            onFinish={onFinish}
            autoComplete='off'
        >
            <Form.Item name='avatar'>
                <CustomUpload />
            </Form.Item>
            <Form.Item label='我的昵称' name='username' rules={[{ required: true, message: '请输入昵称' }]}>
                <Input placeholder='请输入昵称' />
            </Form.Item>
        </Form>
    )
};

export default forwardRef(Index);
