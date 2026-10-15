import React, { useImperativeHandle, forwardRef, useRef, useState, useEffect } from 'react';
import {Button, Input, theme, App, Tooltip, DatePicker, TableColumnsType} from 'antd';
import Title from '../../../component/CZHTitle';
import CZHTable  from "../../../component/CZHTable";
import CZHSelect from "../../../component/CZHSelect";
import CZHTableSearch from "../../../component/CZHTableSearch";
import {adminLogApi} from "../../../api/admin/AdminApi";
import {PageDto} from "../../../api/ApiTypes";
import type {PageInfoVo} from "../../../api/common/CommonVo";
import {HttpResponse} from "../../../util/request";
import {AdminActionLog} from "../../../api/admin/AdminVo";

const Index = (_props: any, ref: any) => {
    const {
        token: { colorPrimary },
    } = theme.useToken();
    const tableRef: any = useRef(null);
    const [search,setSearch]=useState<any>({});
    // 列表
    const columns:TableColumnsType<AdminActionLog> = [
        {
            title: '序号',
            align: 'center',
            dataIndex: 'key',
            width: 90,
        }, {
            title: '操作人',
            align: 'center',
            width: 120,
            dataIndex: 'userName',
            render:(name:string)=>(
                name || "-"
            )
        }, {
            title: '操作内容',
            align: 'center',
            width: 150,
            dataIndex: 'operation'
        }, {
            title: '请求方法',
            align: 'center',
            width: 200,
            dataIndex: 'method',
            render: (method: string) => {
                const arr=method.split(".")
                return(
                    <Tooltip title={method}>
                        <p className={"line1"}>{arr[arr.length-1]}</p>
                    </Tooltip>
                );
            }
        }, {
            title: '请求参数',
            align: 'center',
            width: 200,
            dataIndex: 'params',
            render: (method: string) => {
                return(
                    <Tooltip title={method}>
                        <p className={"line1"}>{method}</p>
                    </Tooltip>
                );
            }
        }, {
            title: '响应内容',
            align: 'center',
            width: 200,
            dataIndex: 'bake',
            render: (method: string) => {
                return(
                    <Tooltip title={method}>
                        <p className={"line1"}>{method}</p>
                    </Tooltip>
                );
            }
        }, {
            title: '操作IP',
            align: 'center',
            width: 150,
            dataIndex: 'ip',
            render: (ip: string) => `${ip || '-'}`
        }, {
            title: '操作时间',
            align: 'center',
            width: 180,
            fixed:"right",
            dataIndex: 'createTime'
        }
    ]
    useEffect(() => {
        refresh()
    }, [search])
    useImperativeHandle(ref, () => ({
        refresh,
    }))
    const refresh = () => {
        tableRef.current.onRefresh(1)
    }
    // 首次进入页面初始化
    const onRefresh = (info: PageDto, callback: (res:HttpResponse<PageInfoVo<AdminActionLog>>) => void) => {
        adminLogApi({
            page: info.page,
            size: info.size,
            orderBy: info.orderBy,
            ...search
        }).then(res => {
            callback(res)
        })
    }
    return (
        <React.Fragment>
            <CZHTableSearch
                onSearch={(data:any)=>{
                    if(data.times) {
                        data.stime = data.times[0].format('YYYY-MM-DD 00:00:00');
                        data.etime = data.times[1].format('YYYY-MM-DD 23:59:59');
                        delete data.times;
                    }
                    setSearch(data)
                }}
                items={[
                    {
                        node: <Input
                            placeholder='请输入操作内容关键词'
                        />,
                        label:"操作内容",
                        name:"desc"
                    },
                    {
                        node: <CZHSelect
                            type="alladmin"
                            placeholder='请选择操作人'
                        />,
                        label:"操作人",
                        name:"adminId"
                    },
                    {
                        node:  <Input
                            placeholder='请输入操作IP'
                        />,
                        label:"操作IP",
                        name:"ip"
                    }
                    ,
                    {
                        node:  <DatePicker.RangePicker />,
                        col:6,
                        label:"操作时间",
                        name:"times"
                    }
                ]}
            />
            <CZHTable<AdminActionLog>
                ref={tableRef}
                columns={columns}
                onRefresh={onRefresh}
            />
        </React.Fragment>
    )
};

export default forwardRef(Index);
