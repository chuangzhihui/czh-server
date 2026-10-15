import React, { useImperativeHandle, forwardRef, useRef, useEffect, useState } from 'react';
import {App, Button, Col, Form, Input, message, Row, Select, Switch} from 'antd';

import Helper from '../../../../util/Helper';
import CZHEditor from "../../../../component/CZHEditor";
import {addSettingApi, editSettingApi} from "../../../../api/set/SetApi";
import CZHUploadMedia, {CZHFileItemToString, StringToCZHFileItem} from '../../../../component/CZHFileComponent/CZHUploadMedia';
import {CommonFormProps} from "../../../../component/CZHModal/FormModal";
import {HttpResponse} from "../../../../util/request";
const typeList = [
    { value: 1, label: '文本' },
    { value: 2, label: '数字' },
    { value: 3, label: '图片' },
    { value: 4, label: '图文' },
    { value: 5, label: '开/关' },
]
export interface AddBasicProps extends CommonFormProps{
    type: 'add' | 'edit',
    data?: any,
    onOk?: () => void
}
const Index = (_props: AddBasicProps, ref: any) => {

    const [form]=Form.useForm();
    useEffect(() => {
        if (_props.type === 'edit' && _props.data) {
            let value = _props.data.value;
            if (_props.data.type == 5) {
                value = value == 1;
            }
            if(_props.data.type==3)
            {
                value=StringToCZHFileItem(value)
            }
            form.setFieldsValue({
                type: _props.data.type,
                title: _props.data.title,
                value,
                canDel: _props.data.canDel == 1,
            })
        }
    }, [])
    // 监听数据改变
    const onValuesChange = (res: any, values: any) => {
        const key = Object.keys(res)[0];
        if (key === 'type') {
            let value = undefined;
            if (values.type === 5) {
                value = false;
            }
            form.setFieldsValue({
                value,
            })
        } else if (key === 'value') {
            if (values.type === 2) {  // 只能输入数字
                const value = Helper.getNums(res[key]);
                form.setFieldsValue({
                    value,
                })
            }
        }
    }
    // 提交
    const onFinish = (data: any) => {
       _props.loading?.(true)
        if (data.type === 4) {
            // data.value = data.value.toHTML();
        } else if (data.type === 5) {
            data.value = data.value ? 1 : 0;
        }else if(data.type===3)
        {
            data.value=CZHFileItemToString(data.value);
        }
       let api:any=addSettingApi;
        if (_props.type == 'edit') {
            api=editSettingApi;
            data.id = _props.data.id;
        } else {
            data.canDel = data.canDel ? 1 : 0;
        }
        api(data).then((res:HttpResponse<any>) => {
            if (res.code == 200) {
                message.success(res.msg, 1.2)
                _props.close?.();
                _props.onOk?.()
            } else {
                message.error(res.msg, 1.2);
            }
        }).finally(()=>{
          _props.loading?.(false)
        })
    }

    useImperativeHandle(ref, () => ({
        form,
    }))
    return (
        <Form
            form={form}
            initialValues={{
                // type: 1,
            }}
            onFinish={onFinish}
            onValuesChange={onValuesChange}
        >
            <Row gutter={12}>
                <Col span={12}>
                    <Form.Item  label='配置名称' name='title' rules={[{ required: true, message: '请输入配置名称' }]}>
                        <Input autoComplete='off' placeholder='请输入配置名称' />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item  initialValue={1} label='值类型' name='type' required>
                        <Select
                            placeholder='请选择'
                            options={typeList}
                        />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label='允许删除' name='canDel' valuePropName='checked'>
                        <Switch disabled={_props.type === 'edit'} checkedChildren='是' unCheckedChildren='否' />
                    </Form.Item>
                </Col>
                <Col span={24}>
                    <Form.Item noStyle shouldUpdate={(prev, cur) => prev.type != cur.type}>
                        {({ getFieldValue }) => (
                            <React.Fragment>
                                {/* 文本 */}
                                {getFieldValue('type') === 1 && <Form.Item className='row10' label='配置值' name='value' rules={[{ required: true, message: '请输入内容' }]}>
                                    <Input.TextArea rows={8} placeholder='请输入' />
                                </Form.Item>}
                                {/* 数字 */}
                                {getFieldValue('type') === 2 && <Form.Item className='row10' label='配置值' name='value' rules={[{ required: true, message: '请输入内容' }]}>
                                    <Input autoComplete='off' placeholder='请输入' />
                                </Form.Item>}
                                {/* 图片 */}
                                {getFieldValue('type') === 3 &&
                                    <Form.Item className='row10' label='配置值' name='value' rules={[{ required: true, message: '请输入内容' }]}>
                                        <CZHUploadMedia types={[1]} max={1} />
                                    </Form.Item>
                                }
                                {/* 图文 */}
                                {getFieldValue('type') === 4 && <Form.Item className='row10' label='配置值' name='value' rules={[{ required: true, message: '请输入内容' }]}>
                                    <CZHEditor  />
                                </Form.Item>}
                                {/* 开关 */}
                                {getFieldValue('type') === 5 && <Form.Item className='row10' label='配置值' name='value' required valuePropName='checked'>
                                    <Switch checkedChildren='开' unCheckedChildren='关' />
                                </Form.Item>}
                            </React.Fragment>
                        )}
                    </Form.Item>
                </Col>
            </Row>

        </Form>
    )
};

export default forwardRef(Index);


