import React, { useImperativeHandle, forwardRef, useRef, useState } from 'react';
import {App, Button, Image, Switch, TableColumnsType, theme} from 'antd';
import Title from '../../../../component/CZHTitle';
import CZHTable, {CZHTableRef} from "../../../../component/CZHTable";
import CZHModal from '../../../../component/CZHModal';
import AddBasic from '../AddBasic';  // 添加配置
import Text from '../../../../component/CZHText';
import CZHTableSearch from "../../../../component/CZHTableSearch";
import {deleteSettingApi, settingListApi} from "../../../../api/set/SetApi";
import {HttpResponse} from "../../../../util/request";
import {GetSettingListVo} from "../../../../api/set/SetVo";
import FormModal from "../../../../component/CZHModal/FormModal";
import CZHTableAction from "../../../../component/CZHTableAction";
import {PageDto} from "../../../../api/ApiTypes";
import type {PageInfoVo} from "../../../../api/common/CommonVo";

const typeList = ['', '文本', '数字', '图片', '图文', '开/关'];

const Index = (_props: any, ref: any) => {
	const {
		token: { colorPrimary, colorWarning, colorInfo, colorSuccess },
	} = theme.useToken();
	/** 用 App.useApp() 拿实例：静态 message/modal 不消费 ConfigProvider 上下文（主题会失效） */
	const { message, modal } = App.useApp();
	const tableRef: any = useRef<CZHTableRef | null>(null);
	const columns:TableColumnsType<GetSettingListVo> = [
        {
		title: '配置ID',
		align: 'center',
		dataIndex: 'id',
		width: 90,
	}, {
		title: '配置名称',
		align: 'center',
		dataIndex: 'title',
		ellipsis: true,
	}, {
		title: '配置值',
		align: 'center',
		dataIndex: 'value',
		ellipsis: true,
		render: (value: string, item) => (
			<React.Fragment>
				{(item.type == 1 || item.type == 2) && value}
				{item.type == 3 && <Image src={value} width={40} height={40} />}
				{item.type == 4 && '图文内容请在详情中查看'}
				{item.type == 5 && <Switch disabled checked={value === '1'} checkedChildren='开' unCheckedChildren='关' />}
			</React.Fragment>
		)
	}, {
		title: '值类型',
		align: 'center',
		dataIndex: 'type',
		render: (type: number) => (
			<Button
				type='primary'
				size='small'
				style={{
					background: type == 2 ? colorInfo : (type == 3 ? colorSuccess : (type == 1 ? colorPrimary : colorWarning))
				}}
			>{typeList[type]}</Button>
		)
	}, {
		title: '操作',
		dataIndex: 'id',
		align: 'center',

		width: 150,
		render: (id: number, item: GetSettingListVo) => (
			<CZHTableAction
				actions={[
					<p className={"p-primary"} onClick={() => {
						FormModal.open({
							title:"编辑配置",
							children:<AddBasic type={"edit"} data={item}  onOk={refresh}/>,
							width:696
						})
					}}>编辑</p>,
					item.canDel == 1 && <p className={"p-error"} onClick={() => del(id)}>删除</p>
				]}
			/>
		)
	}]
	useImperativeHandle(ref, () => ({
		refresh,
	}))
	const refresh = () => {
		tableRef.current?.onRefresh()
	}
	// 首次进入页面初始化
	const onRefresh = (info:PageDto, callback: (res:HttpResponse<PageInfoVo<GetSettingListVo>>) => void) => {
        settingListApi({
            page: info.page,
            size: info.size,
            orderBy: ''
        }).then(res => {
            callback(res);
        })
	}

	// 删除
	const del = (id: number) => {
		modal.confirm({
			title: '警告提示',
			content: '您要删除该项数据吗？删除后将无法恢复！',
			centered: true,
			maskClosable: true,
			onOk: () => {
				return deleteSettingApi(id).then(res => {
					if (res.code == 200) {
						message.success(res.msg, 1.2);
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
			buttons={[<Button type="primary" onClick={() => {
                FormModal.open({
                    title:"添加配置",
                    children:<AddBasic type={"add"}  onOk={refresh}/>,
                    width:696
                })
			}}>添加配置</Button>]}
			/>

			<CZHTable<GetSettingListVo>
				ref={tableRef}
				columns={columns}
				onRefresh={onRefresh}
				auto={true}
			/>
		</React.Fragment>
	)
};

export default forwardRef(Index);
