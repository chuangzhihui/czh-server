import React, { forwardRef, useEffect, useImperativeHandle, useRef, useState } from 'react';
import { App, Button, Form, Input, Switch } from 'antd';
import type { FormInstance } from 'antd/es/form';
import {
    getUploadConfigApi,
    saveAliossApi,
    saveLocalApi,
    saveQiniuApi,
    saveTosApi,
    saveTxcosApi
} from '../../../api/set/SetApi';
import './index.scss';

/** 渠道字段定义：label + name（name 为相对渠道 key 的字段名） */
interface ChannelField {
    label: string;
    name: string;
    placeholder?: string;
}

/** 上传渠道定义：五个渠道结构完全同构，数据驱动渲染 */
interface ChannelDef {
    key: string;
    title: string;
    /** 保存时写入后端的渠道编号（与后端 visible 约定一致） */
    visibleValue: number;
    save: (data: any) => Promise<any>;
    fields: ChannelField[];
}

const CHANNELS: ChannelDef[] = [
    {
        key: 'qiniu',
        title: '七牛云配置',
        visibleValue: 1,
        save: saveQiniuApi,
        fields: [
            { label: 'AccessKey', name: 'accessKey', placeholder: '请输入 AccessKey' },
            { label: 'SecretKey', name: 'secretKey', placeholder: '请输入 SecretKey' },
            { label: '仓库名称', name: 'bucket', placeholder: '请输入仓库名称' },
            { label: '上传节点', name: 'endpoint', placeholder: '请输入上传节点' },
            { label: '七牛域名', name: 'domain', placeholder: '请输入七牛域名' },
        ],
    },
    {
        key: 'alioss',
        title: '阿里 OSS 配置',
        visibleValue: 2,
        save: saveAliossApi,
        fields: [
            { label: 'AccessKeyID', name: 'ak', placeholder: '请输入 AccessKeyID' },
            { label: 'AccessKey', name: 'sk', placeholder: '请输入 AccessKey' },
            { label: '仓库名称', name: 'bucket', placeholder: '请输入仓库名称' },
            { label: 'endpoint', name: 'endpoint', placeholder: '请输入 endpoint' },
            { label: '自定义域名', name: 'domain', placeholder: '请输入自定义域名' },
        ],
    },
    {
        key: 'txcos',
        title: '腾讯云配置',
        visibleValue: 3,
        save: saveTxcosApi,
        fields: [
            { label: 'AccessKeyID', name: 'ak', placeholder: '请输入 AccessKeyID' },
            { label: 'AccessKey', name: 'sk', placeholder: '请输入 AccessKey' },
            { label: '仓库名称', name: 'bucketName', placeholder: '请输入仓库名称' },
            { label: 'bucket', name: 'bucket', placeholder: '请输入 bucket' },
            { label: '自定义域名', name: 'domain', placeholder: '请输入自定义域名' },
        ],
    },
    {
        key: 'tos',
        title: '火山云配置',
        visibleValue: 5,
        save: saveTosApi,
        fields: [
            { label: 'AccessKey', name: 'accessKey', placeholder: '请输入 AccessKey' },
            { label: 'SecretKey', name: 'secretKey', placeholder: '请输入 SecretKey' },
            { label: '仓库名称', name: 'bucket', placeholder: '请输入仓库名称' },
            { label: '仓库地域', name: 'region', placeholder: '请输入仓库地域' },
            { label: '自定义域名', name: 'domain', placeholder: '请输入自定义域名' },
        ],
    },
    {
        key: 'local',
        title: '本地配置',
        visibleValue: 4,
        save: saveLocalApi,
        fields: [
            { label: '上传路径', name: 'path', placeholder: '请输入上传路径' },
            { label: '上传地址', name: 'host', placeholder: '请输入上传地址' },
            { label: '自定义域名', name: 'domain', placeholder: '请输入自定义域名' },
        ],
    },
];

const Index = (_props: any, ref: any) => {
    const { message } = App.useApp();
    const [info, setInfo] = useState<Record<string, any>>({ qiniu: {} });
    /** 每个渠道一个 Form 实例，统一管理（旧代码 tos/local 错用 txFormRef，已修正） */
    const formRefs = useRef<Record<string, FormInstance | null>>({});

    useEffect(() => {
        refresh();
    }, []);

    useImperativeHandle(ref, () => ({
        refresh,
    }));

    const refresh = () => {
        getUploadConfigApi().then(res => {
            if (res.code === 200) {
                const data = res.data as Record<string, any>;
                // 各渠道启用态由后端 visible 决定，仅用于头部开关展示
                CHANNELS.forEach(ch => {
                    if (data[ch.key] && typeof data[ch.key] === 'object') {
                        data[ch.key].visible = false;
                    }
                });
                if (data.visible === 1) data.qiniu.visible = true;
                else if (data.visible === 2) data.alioss.visible = true;
                else if (data.visible === 3) data.txcos.visible = true;
                else if (data.visible === 4) data.local.visible = true;
                else if (data.visible === 5) data.tos.visible = true;

                setInfo(data);
                CHANNELS.forEach(ch => {
                    formRefs.current[ch.key]?.setFieldsValue({
                        [ch.key]: data[ch.key],
                    });
                });
            }
        });
    };

    const onFinish = (data: any) => {
        const key = Object.keys(data)[0];
        const obj: any = data[key];
        const channel = CHANNELS.find(ch => ch.key === key);
        if (!channel) return message.error('配置错误');
        obj.visible = obj.visible ? channel.visibleValue : 0;
        channel.save(obj).then((res: any) => {
            if (res.code === 200) {
                message.success(res.msg);
                refresh();
            } else {
                message.error(res.msg);
            }
        });
    };

    /** 重置：恢复接口下发的原始值 */
    const reset = (key: string) => {
        formRefs.current[key]?.setFieldsValue({ [key]: info[key] });
    };

    return (
        <div className='uploadSet'>
            <div className='uploadSet__grid'>
                {CHANNELS.map(ch => (
                    <Form
                        key={ch.key}
                        autoComplete='off'
                        layout='vertical'
                        ref={(inst: FormInstance | null) => { formRefs.current[ch.key] = inst; }}
                        name={ch.key}
                        onFinish={onFinish}
                        initialValues={{ [ch.key]: info[ch.key] }}
                    >
                        <div className='upCard'>
                            <div className='upCard__head'>
                                <span className='upCard__title'>{ch.title}</span>
                                <Form.Item
                                    className='upCard__switch'
                                    name={[ch.key, 'visible']}
                                    valuePropName='checked'
                                    noStyle
                                >
                                    <Switch checkedChildren='启用' unCheckedChildren='关闭' />
                                </Form.Item>
                            </div>
                            <div className='upCard__body'>
                                {ch.fields.map(f => (
                                    <Form.Item
                                        key={f.name}
                                        label={f.label}
                                        name={[ch.key, f.name]}
                                    >
                                        <Input allowClear placeholder={f.placeholder} />
                                    </Form.Item>
                                ))}
                            </div>
                            <div className='upCard__foot'>
                                <Button onClick={() => reset(ch.key)}>重置</Button>
                                <Button type='primary' htmlType='submit'>保存</Button>
                            </div>
                        </div>
                    </Form>
                ))}
            </div>
        </div>
    );
};

export default forwardRef(Index);
