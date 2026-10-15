import React, { useImperativeHandle, forwardRef, useRef, useState, useEffect } from 'react';
import {Button, App, theme, TableColumnsType} from 'antd';
import Title from '../../../../component/CZHTitle';
import CustomerTable from "../../../../component/CZHTable";
import AddMenu from '../AddMenu';
import CZHTableSearch from "../../../../component/CZHTableSearch";
import CZHModal from "../../../../component/CZHModal";
import Text from "../../../../component/CZHText";
import {HttpResponse} from "../../../../util/request";
import {GetMenuListVo} from "../../../../api/menu/MenuVo";
import {delMenuApi, menuListApi} from "../../../../api/menu/MenuApi";
import {PageDto} from "../../../../api/ApiTypes";
import type {PageInfoVo} from "../../../../api/common/CommonVo";
import FormModal from "../../../../component/CZHModal/FormModal";  // 添加、编辑菜单
import CZHTableAction from "../../../../component/CZHTableAction";

const levelTxt = ['', '一级菜单', '二级菜单', '三级菜单']

const Index = (_props: any, ref: any) => {
	const { message, modal } = App.useApp();
    const [datas,setDatas] = useState<GetMenuListVo[]>([]);
	const {
		token: { colorPrimary, colorWarning, colorError, colorSuccess },
	} = theme.useToken();
	const tableRef: any = useRef(null);
	const colorArr = ['', colorSuccess, colorWarning, colorError];
	useImperativeHandle(ref, () => ({
		refresh,
	}))
	const refresh = () => {
		tableRef.current.onRefresh(1)
	}
	// 列表
	const columns:TableColumnsType<GetMenuListVo> = [

		{
			title: '菜单名称',
			align: 'left',
			width: 200,
			dataIndex: 'name'
		}, 		{
			title: '前端路由',
			align: 'center',
			width: 120,
			dataIndex: 'path',
			render: (_path: string, item: GetMenuListVo) => item.display === 1 ? `${item.fullPath || '-'}` : '-'
		}, {
			title: '后端路由',
			align: 'center',
			dataIndex: 'route',
			width: 180,
			render: (route: string) => `${route || '-'}`
		}, {
			title: '菜单等级',
			align: 'center',
			width: 180,
			dataIndex: 'level',
			render: (level: number) => {
				return <Text type={"success"} style={{color:colorArr[level]}}>{levelTxt[level]}</Text>
			}
		}, {
			title: '显示状态',
			align: 'center',
			width: 120,
			dataIndex: 'display',
			render:(display: number,item) => {
				return(
					<Button className='font12' size='small' type='primary' danger={display !== 1}>{display === 1 ? '显示' : '隐藏'}</Button>
				);
			}
		}, {
			title: '操作',
			dataIndex: 'id',
			width: 250,
			align: 'center',
			render: (id: number, item) => (
				<CZHTableAction
					actions={[
						item.level<3 &&
							<p className={"p-success"} onClick={()=>{
								let fid,
									sid;
								if (item.level == 1) {
									fid = item.id;
								} else if (item.level == 2) {
									fid = item.pid;
									sid = item.id;
								}
								FormModal.open({
									title:"添加子菜单",
									children:<AddMenu
										type={"add"}
										level={item.level+1}
										data={{fid,sid}}
										onOk={refresh}
									/>,
									width:460

								})
							}}>添加子菜单</p>,
						<p className={"p-primary"} onClick={() => {
							let fid,
								sid;
							if (item.level == 2) {
								fid = item.pid;
								sid = item.id;
							} else if (item.level == 3) {
								const parent=findParent(item.pid);
								if(parent) {
									fid=parent.pid;
								}else {
									fid = id;
								}
								sid = item.pid;
							}
							FormModal.open({
								title:"编辑菜单",
								children:<AddMenu
									type={"edit"}
									level={item.level}
									data={{...item,fid,sid}}
									onOk={refresh}
								/>,
								width:460

							})

						}}>编辑</p>,
						<p  className={"p-error"} onClick={() => del(item)}>删除</p>
					]}
				/>
			)
		}
	]
    const findParent=(id:number)=>{
        for(let i=0;i<datas.length;i++){
            if(datas[i].id===id)
            {
                return datas[i]
            }
            const childrens=datas[i].child;
            for(let j=0;j<childrens.length;j++)
            {
                const child=childrens[j];
                if(child.id===id)
                {
                    return child;
                }
            }
        }
        return null;
    }
	// 首次进入页面初始化
	const fillFullPath = (nodes: GetMenuListVo[], parentPath = '') => {
		nodes.forEach(n => {
			n.fullPath = parentPath ? `${parentPath}/${n.path}` : (n.path || '');
			if (n.child && n.child.length) fillFullPath(n.child, n.fullPath);
		});
	};
	const onRefresh = (info: PageDto, callback: (res:HttpResponse<PageInfoVo<GetMenuListVo>>) => void) => {
        menuListApi({
            page: info.page,
            size: info.size,
            orderBy: '',
            // ...search
        }).then(res => {
            if(res.code == 200) {
                const list = res.data.list || [];
                fillFullPath(list);
                setDatas(list);
            }
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
				return delMenuApi(data.id).then(res => {
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
			buttons={[<Button type="primary" onClick={() => {
                FormModal.open({
                    title:"添加菜单",
                    children:<AddMenu
                        type={"add"}
                        level={1}
                        data={{pid:0}}
                        onOk={refresh}
                    />,
                    width:460

                })
			}}>添加菜单</Button>]}
			/>
			<CustomerTable<GetMenuListVo>
				ref={tableRef}
				columns={columns}
				onRefresh={onRefresh}
				expandable={{
					childrenColumnName:"child",
					expandedRowClassName:"expandedRowStyle",
					indentSize:30
				}}
				auto={true}
				scroll={{ x:1010 }}
			/>
		</React.Fragment>
	)
};

export default forwardRef(Index);
