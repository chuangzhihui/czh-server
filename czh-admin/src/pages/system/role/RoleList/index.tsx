import React, { useImperativeHandle, forwardRef, useState, useEffect, useRef } from 'react';
import {App, Button, Input, theme} from 'antd';
import CZHTable, {CZHTableRef} from "../../../../component/CZHTable";
import AddRole from '../AddRole';  // 添加/编辑角色
import CZHTableSearch from "../../../../component/CZHTableSearch";
import {HttpResponse} from "../../../../util/request";
import {delRoleApi, roleListApi} from "../../../../api/role/RoleApi";
import FormModal from "../../../../component/CZHModal/FormModal";
import CZHTableAction from "../../../../component/CZHTableAction";
import {Role} from "../../../../api/role/RoleVo";
import type { TableColumnsType } from 'antd';
import {PageDto} from "../../../../api/ApiTypes";
import type {PageInfoVo} from "../../../../api/common/CommonVo";
const Index = (_props: any, _ref: any) => {
	const {
		token: { colorPrimary },
	} = theme.useToken();
	const { message, modal } = App.useApp();
	const tableRef: any = useRef<CZHTableRef | null>(null);
	const columns:TableColumnsType<Role> = [
		{
			title: '序号',
			align: 'center',
			dataIndex: 'key',
			width: 80,
		}, {
			title: '角色名称',
			align: 'center',
			dataIndex: 'roleName',
			width: 120,
		}, {
			title: '角色描述',
			align: 'center',
			dataIndex: 'describe',
			width: 180,
		}, {
			title: '添加时间',
			align: 'center',
			width: 180,
			dataIndex: 'atime'
		}, {
			title: '操作',
			dataIndex: 'id',
			align: 'center',
			width: 150,
			render: (id: number, item: Role) => (
				<CZHTableAction
					actions={[
						<p className={"p-primary"} onClick={() => {
							const data = JSON.parse(JSON.stringify(item));
							data.ids = JSON.parse(data.ids);
							FormModal.open({
								title:"编辑角色",
								children:	<AddRole type={"edit"} data={data}
								                          onOk={() => {
								                              refresh();
								                          }}
								/>,
								width: 700
							})
						}}>编辑</p>,
						<p onClick={() => del(item)}  className={"p-error"}>删除</p>
					]}
				/>
			)
		}
	]
	useImperativeHandle(_ref, () => ({
		refresh,
	}))
	// 手动刷新页面  参数page 非必传 默认当前页码
	const refresh = () => {
		tableRef.current?.onRefresh();
	}
	// 首次进入页面初始化
	const onRefresh = (info: PageDto, callback: (res:HttpResponse<PageInfoVo<Role>>) => void) => {
        roleListApi({
            page: info.page,
            size: info.size,
            orderBy: '',
        }).then(res => {
            callback(res)
        })
	}

	// 删除
	const del = (data: any) => {
		modal.confirm({
			title: '警告提示',
			content: '您要删除该项数据吗？删除后将无法恢复！',
			centered: true,
			maskClosable: true,
			onOk: () => {
				return delRoleApi(data.roleId).then(res => {
					if (res.code == 200) {
						refresh();
					} else {
						message.error(res.msg, 1.2);
					}
				})
			}
		})
	}
	return (
		<React.Fragment>
			<CZHTableSearch
				buttons={[
					<Button type="primary" onClick={() => {
						FormModal.open({
                            title:"添加角色",
                            children:	<AddRole type={"add"}
                                            onOk={() => {
                                                refresh();
                                            }}
                                        />,
                            width: 700
                        })
					}}>添加角色</Button>
				]}
			/>

			<CZHTable<Role>
				ref={tableRef}
				columns={columns}
				onRefresh={onRefresh}
				scroll={{ y: window.innerHeight - 368,x:710 }}
				auto={true}
			/>
		</React.Fragment>
	)
};

export default forwardRef(Index);
