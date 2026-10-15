import React, { useImperativeHandle, forwardRef, useRef, useState, useEffect } from 'react';
import {Button, DatePicker, theme, App, TableColumnsType} from 'antd';
import CZHTable, {CZHTableRef} from "../../../component/CZHTable";
import CZHSelect from "../../../component/CZHSelect";
import CZHTableSearch from "../../../component/CZHTableSearch";
import CZHTableAction from "../../../component/CZHTableAction";
import Helper from "../../../util/Helper";

import {HttpResponse} from "../../../util/request";
import {DbBackupVo} from "../../../api/backup/BackupVo";
import {backUpDbApi, downloadApi, getBackDbListApi, removeBackUpFileApi, restoreDbApi} from "../../../api/backup/BackupApi";
import {PageDto} from "../../../api/ApiTypes";
import type {PageInfoVo} from "../../../api/common/CommonVo";

const Index = (_props: any, ref: any) => {
    const {
        token: { colorPrimary,colorError,colorWarning },
    } = theme.useToken();
    const { message, modal } = App.useApp();
    const tableRef: any = useRef<CZHTableRef | null>(null);
    const [search,setSearch]=useState<any>({});
    // 列表
    const columns:TableColumnsType<DbBackupVo> = [
        {
            title: 'ID',
            align: 'center',
            dataIndex: 'id',
            width: 90,
        }, {
            title: '操作人',
            align: 'center',
            dataIndex: 'adminName',
            width: 150
        }, {
            title: '文件名',
            align: 'center',
            dataIndex: 'fileName',
            width: 150
        }, {
            title: '文件大小',
            align: 'center',
            dataIndex: 'fileSize',
            width: 150,
            render:(size:number)=>{
                return Helper.getFileSize(size);
            }
        }, {
            title: '备份时间',
            align: 'center',
            dataIndex: 'atime',
            width: 150
        }, {
            title: '操作',
            dataIndex: 'id',
            width: 150,
            align: 'center',
            render: (id: number, item) => (
                <CZHTableAction
                    actions={[
                        <p className={"p-primary"} onClick={() => {
                            xiazai(item)
                        }}>下载</p>,
                        <p  className={"p-success"} onClick={() => huifu(item)}>恢复</p>,
                        <p  className={"p-error"} onClick={() => del(item)}>删除</p>
                    ]}
                />
            )
        }
    ]

    const xiazai=(item:DbBackupVo)=>{
        modal.confirm({
            title:"提示",
            content:"确定要下载该备份文件吗?",
            onOk:()=>{
                return new Promise<void>(resolve => {
                    downloadApi(item.id).then((res:any) => {
                        resolve();
                        if(!res.code && res.code!=500)
                        {
                            Helper.saveAs(res,item.fileName)
                        }else{
                            message.success(res.msg);
                        }
                    })
                });
            }
        })
    }
    const huifu=(item:any)=>{
        modal.confirm({
            title:"提示",
            content:"确定要恢复该备份文件吗?",
            onOk:()=>{
                return new Promise<void>(resolve => {
                    restoreDbApi(item.id).then((res:any) => {
                        resolve()
                        if (res.code == 200) {
                            message.success(res.msg);
                            refresh()
                        } else {
                            message.error(res.msg, 1.2);
                        }
                    })
                });
            }
        })
    }
    const del=(item:any)=>{
        modal.confirm({
            title:"提示",
            content:"确定要删除该备份文件吗?",
            onOk:()=>{
                return new Promise<void>(resolve => {
                    removeBackUpFileApi(item.id).then((res:any) => {
                        resolve()
                        if (res.code == 200) {
                            message.success(res.msg);
                            refresh()
                        } else {
                            message.error(res.msg, 1.2);
                        }
                    })
                });
            }
        })
    }
    useEffect(() => {
        refresh()
    }, [search])
    useImperativeHandle(ref, () => ({
        refresh,
    }))
    const refresh = () => {
        tableRef.current.onRefresh()
    }
    // 获取列表数据
    const onRefresh = (info: PageDto, callback: (res:HttpResponse<PageInfoVo<DbBackupVo>>) => void) => {
        getBackDbListApi( {
            page: info.page,
            size: info.size,
            orderBy: '',
            ...search
        }).then(res => {
            callback(res)
        })
    }
    const beifen=()=>{
        modal.confirm({
            title:"提示",
            content:"确定要备份当前数据库?",
            onOk:()=>{
                return new Promise<void>(resolve => {
                    backUpDbApi().then((res:any) => {
                        resolve()
                        if (res.code ===200) {
                            message.success(res.msg);
                            refresh()
                        } else {
                            message.error(res.msg, 1.2);
                        }
                    })
                });
            }
        })
    }
    return (
        <React.Fragment>
            <CZHTableSearch
                onSearch={(data:any)=>{

                    if(data.times)
                    {
                        data.stime=data.times[0].format("YYYY-MM-DD HH:mm:ss")
                        data.etime=data.times[1].format("YYYY-MM-DD HH:mm:ss")
                        delete  data.times;
                    }
                    setSearch(data)
                }}
                items={[
                    {
                        node:  <DatePicker.RangePicker

                            inputReadOnly

                        />,
                        label:"备份时间",
                        name:"times"
                    },
                    {
                        node: <CZHSelect
                            type="alladmin"
                            placeholder='请选择操作人'
                        />,
                        label:"操作人",
                        name:"adminId"
                    }
                ]}
                buttons={[
                    <Button type="primary" onClick={() => {
                       beifen();
                    }}>备份</Button>
                ]}
            />
            <CZHTable<DbBackupVo>
                ref={tableRef}
                columns={columns}
                onRefresh={onRefresh}
                auto={true}
            />
        </React.Fragment>
    )
};

export default forwardRef(Index);
