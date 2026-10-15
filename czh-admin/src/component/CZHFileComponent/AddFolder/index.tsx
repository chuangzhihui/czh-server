import {Button, Form, Input, message} from "antd";
import React, {forwardRef, useImperativeHandle} from "react";
import {CZHFileUploadResult} from "../CZHUpload";
import {addFileApi} from "../../../api/set/SetApi";
import {CommonFormProps} from "../../CZHModal/FormModal";
export interface AddFolderProps extends CommonFormProps{
    pid:number;//上级目录ID
    onOk?: ()=>void;
}
const AddFolder=(props:AddFolderProps,ref:any)=>{
    // 保存文件
    const uploadFile=(res:CZHFileUploadResult)=> {
        const data = {
            domain: res.domain,
            type: res.type,
            name:res.name,
            key:res.key,
            url:res.url,
            pid:props.pid,
            fileWidth:res.fileWidth,
            fileHeight:res.fileHeight,
            fileSize:res.fileSize,
            thumb:res.thumb
        }
        props.loading?.(true)
        addFileApi(data).then(res => {
            props.loading?.(false)
            if (res.code == 200) {
                message.success('成功！', 1.2);

               props.onOk?.()
                props.close?.()
            } else {
                message.error(res.msg, 1.2);
            }
        })
    }
    // 创建文件夹
    const onFinish = (data:any) => {

        uploadFile({
            domain: 0,
            type: 8,
            name: data.name,
            key: '',
            url:"",
            fileWidth:0,
            fileHeight:0,
            fileSize:0
        })
    }
    const [form] = Form.useForm();
    useImperativeHandle(ref, () => ({
        form,
    }))
    return(
        <Form
            form={form}
            onFinish={onFinish}
        >
            <Form.Item label='文件夹名称' name='name' rules={[{ required: true, message: '请输入文件夹名称' }]}>
                <Input autoComplete='off' placeholder='请输入文件夹名称' />
            </Form.Item>
        </Form>
    );
}
export default forwardRef(AddFolder);