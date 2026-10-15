import React, {ForwardedRef, forwardRef, useEffect, useImperativeHandle, useRef, useState} from "react";
import { message, Select, SelectProps, Table, TableProps } from 'antd';
import { HttpResponse } from "../../util/request";
import Helper from "../../util/Helper";
import "./index.scss"
import {SorterResult} from "antd/es/table/interface";
import {PageDto} from "../../api/ApiTypes";
import type {PageInfoVo} from "../../api/common/CommonVo";

export interface CustomerTableProps<T> extends TableProps<T> {
    data?: T[],
    total?: number;
    page?: number;
    scrollX?: number;
    pageSize?: number;
    auto?: boolean;//是否自动加载数据
    onRefresh: (pageRequest: PageDto, callBack: (res: HttpResponse<PageInfoVo<T>>) => void) => void;
    hidePagination?: boolean;//是否隐藏分页
}
export interface CZHTableRef {
    onRefresh: () => void;
}
type CZHTableComponent<T> = (props: CustomerTableProps<T>, ref: ForwardedRef<CZHTableRef>) => React.ReactElement;
const CZHTableInner:CZHTableComponent<any>=<T,>(props: CustomerTableProps<T>, ref: ForwardedRef<CZHTableRef>)=>{
    const [loading, setLoading] = useState<boolean>(false);
    const [pageSize, setPageSize] = useState<number>(props.pageSize || 10);//每页展示条数
    const [orderBy, setOrderBy] = useState<string>("");//排序
    const [page, setPage] = useState<number>(props.page || 1);//页码
    const [total, setTotal] = useState<number>(props.total || 0);//数据总长度-包含未加载的
    const [data, setData] = useState<any[]>(props.data || []);//数据
    const [isInit, setIsInit] = useState<boolean>(true);
    const [auto, setAuto] = useState<boolean>(props.auto || false);
    const tableRef = useRef<any>(null);
    const [scrollY, setScrollY] = useState<number>(0);
    const updateWidth = () => {
        if (tableRef.current) {
            let height = tableRef.current.offsetHeight - 55 - 16
            if (!props.hidePagination) {
                height -= 32;
            }
            setScrollY(height)
        }
    }
    useEffect(() => {
        updateWidth();
        // 监听窗口大小变化
        window.addEventListener('resize', updateWidth);
        // 清理函数
        return () => {
            window.removeEventListener('resize', updateWidth);
        };
    }, [])
    useEffect(() => {
        if (isInit) {
            if (auto) {
                getList();
            }
        } else {
            getList();
        }

    }, [page, pageSize, orderBy]);
    //重载当前页数据
    const getList = () => {
        setIsInit(false);
        setLoading(true);
        props.onRefresh?.({ page, size: pageSize, orderBy }, (res: HttpResponse<PageInfoVo<T>>) => {
            setLoading(false);
            if (res.code === 200) {
                setTotal(res.data.total);
                setData(initData(res.data.list, res.data.total))
            } else {
                message.error(res.msg)
            }
        });
    }
    const initData = (arry: any[], total: number, pkey = ""): any[] => {
        const arryNew: any[] = []
        arry.map((item, index) => {
            let key = Helper.getNum(index, total, pageSize, page, orderBy)

            if (pkey != "") {
                key = pkey + "-" + key
            }
            if (item.child && item.child.length > 0) {
                item.child = initData(item.child, item.child.length, key)
            }
            arryNew.push(Object.assign({}, item, { key: key }))
        })
        return arryNew
    }
    const onRefresh = (refreshPage: number=1) => {
        if (refreshPage === page) {
            getList();
        } else {
            setPage(refreshPage);
        }

    }
    useImperativeHandle(ref, () => ({
        onRefresh
    }))
    return (
        <div className={"CZHTableContainer"} style={props.style}>
            <Table<T>
                ref={tableRef}
                className='CZHTableContent'
                loading={loading}
                pagination={{
                    position: ["bottomRight"],
                    pageSize,
                    current: page,
                    total: total,
                    showSizeChanger: true,
                    showTotal: (total: number, range: [number, number]): string => {
                        const num: number = range[0],
                            num1: number = range[1]
                        const numStr: string = num < 10 ? ('0' + num) : String(num);
                        const numStr1: string = num1 < 10 ? ('0' + num1) : String(num1);
                        return `共${total}条记录，本页展示${numStr}-${numStr1}条记录`
                    }
                }}
                dataSource={data}
                bordered={true}
                virtual
                onChange={(page, filters, sorter:SorterResult<T> | SorterResult<T>[]) => {
                    let orderByStr: string = "";
                    let sort:SorterResult<T>;
                    if(Array.isArray(sorter))
                    {
                        sort = sorter[0];
                    }else{
                        sort = sorter;
                    }
                    if (sort.order) {
                        if (sort.order === "ascend") {
                            orderByStr = "asc";
                        } else if (sort.order === "descend") {
                            orderByStr = "desc";
                        }
                    }
                    setOrderBy(orderByStr);
                    setPage(page.current || 1);
                    setPageSize(page.pageSize || 10)
                }}

                {...props}
                scroll={{ y: scrollY, x: props.scrollX }}
            />
        </div>
    );
}


// 关键3：正确包装泛型forwardRef，并导出（这是让组件支持<T>泛型的核心）
export const CZHTable = forwardRef(CZHTableInner) as <T>(
    props: CustomerTableProps<T> & { ref?: ForwardedRef<CZHTableRef> }
) => React.ReactElement;
export default CZHTable;
