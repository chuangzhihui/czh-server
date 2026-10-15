import React, { useState, useEffect } from 'react';
import {Button, Input, Select, Pagination, App, Checkbox, Tooltip,} from 'antd';
import CZHModal from '../../CZHModal';
import Title from '../../CZHTitle'
import CZHUpload, {CZHFileUploadResult} from "../CZHUpload";
import {addFileApi, delFileApi, getFileListApi} from "../../../api/set/SetApi";
import {ImgCropProps} from "antd-img-crop";
import Helper from "../../../util/Helper";
import CZHRenderThumb from "../CZHRenderThumb";
import {createRoot} from "react-dom/client";
import {UploadFiles} from "../../../api/set/SetVo";
import FormModal from "../../CZHModal/FormModal";
import ModalRoot from "../../CZHModal/ModalRoot";
import AddFolder from "../AddFolder";
import './index.scss';
const fileType = [
    { value: 1, label: '图片' },
    { value: 2, label: '视频文件' },
    { value: 3, label: 'Execl文件' },
    { value: 4, label: 'Word文件' },
    { value: 5, label: 'PDF文件' },
    { value: 6, label: '压缩文件' },
    { value: 7, label: '未知类型文件' },
];
export interface CZHFileItem{
    uid:string;//文件ID
    type: number;//文件类型1图片 2视频 3Excel 4word 5pdf 6zip 7未知文件
    name:string;//文件名称
    width:number;//文件宽度
    height:number;//文件高度
    url:string;//文件地址
    thumb?:string;//缩略图
}
export interface fileListProps {
    types?:number[];//文件类型 1图片 2视频 3Excel 4word 5pdf 6zip 7未知文件
    max?:number;//最大数量
    onOk?: (files:CZHFileItem[]) => void;
    crop?:boolean;//上传是否需要裁剪
    cropProps?:ImgCropProps;//裁剪参数
    open?:boolean;//
}




const CZHFileList = (props:fileListProps) => {

    /** 用 App.useApp() 拿实例：静态 message/modal 不消费 ConfigProvider 上下文（主题与前缀都会失效） */
    const { message, modal } = App.useApp();
    const [open, setOpen] = useState<boolean>(props.open || false);
    const [list, setList] = useState<UploadFiles[]>([]);
    const [total, setTotal] = useState(0);
    const [page, setPage] = useState(1);
    const [pid, setPid] = useState(0);
    const [types, setTypes] = useState<number[]>(props.types || []);
    const [name, setName] = useState('');
    const [fileName, setFileName] = useState([{ id: 0, name: '根目录' }])
    const [createVisible, setCreateVisible] = useState<boolean>(false);
    const [percent, setPercent] = useState(0);
    const [loading, setLoading] = useState(false);
    const [value, setValue] = useState<number[]>([]);
    const [dir,setDir] = useState<string>("admin");
    const [accept,setAccept] = useState<string>("");
    const {max=1} = props;
    useEffect(() => {
        makeAccepts();
    }, [types]);
    const makeAccepts=()=>{
        const typeArr:string[]=[];
        if(types.length==0 || types.includes(7)){
            setAccept("*")
            return
        }
        for (let i=0;i<types.length;i++){
            const type:number = types[i];
            if(type==1){
                typeArr.push("image/*")
            }else if(type==2){
                typeArr.push("video/*")
            }else if(type==3){
                typeArr.push(".xls")
                typeArr.push(".xlsx")
                typeArr.push(".xlsm")
                typeArr.push(".xlsb")
                typeArr.push(".xltx")
                typeArr.push(".xltm")
            }else if(type==4){
                typeArr.push(".doc")
                typeArr.push(".docx")
                typeArr.push(".docm")
                typeArr.push(".dot")
                typeArr.push(".dotx")
                typeArr.push(".dotm")
            }else if(type==5){
                typeArr.push("application/pdf")
                typeArr.push(".pdf")
            }else if(type==6){
                typeArr.push(".zip")
                typeArr.push(".rar")
                typeArr.push(".7z")
                typeArr.push(".tar")
                typeArr.push(".gz")
                typeArr.push(".bz2")
                typeArr.push(".xz")
                typeArr.push(".iso")
            }
        }
        setAccept(typeArr.join(","));
    }
    useEffect(() => {
        getList()
    }, [page, types, name, pid])
    useEffect(()=>{
        let dirName="admin";
        for(let i=1;i<fileName.length;i++)
        {
            dirName+=fileName[i].name
        }
    },[fileName])
    // 获取文件列表
    const getList = () => {
        const obj = {
            page,
            size: 24,
            orderBy: '',
            pid,
            name,
            types: types.length>0?types.join(","):"",
        }
        getFileListApi(obj).then(res => {
            if (res.code === 200) {
                setList(res.data.list);
                setTotal(res.data.total);
            }
        })
    }
    // 关闭文件库
    const onCancel = () => {
        setOpen(false);
    }
    // 双击打开文件夹
    const openFolder = (data:any, index = -1) => {
        setPid(data.id);
        setPage(1);
        if (index > -1) {
            if (index != fileName.length - 1) {
                const newArr = fileName.slice(0, (index + 1));
                setFileName(newArr)
            }
        } else {
            if (data.type === 8) {
                setFileName([...fileName, { id: data.id, name: `/${data.name}` }]);
            }
        }
    }


    // 确认选中文件
    const onOk = () => {
        if (value.length === 0) {
           return;
        }
        const files: CZHFileItem[] = [];
        for (let i = 0; i < value.length; i++) {
            const item:any = findListItemById(Number(value[i]));
            files.push({
                uid:"uuid-"+Helper.getRandomString(4)+"-"+item.id,
                type:item.type,
                url:item.url,
                width:item.fileWidth,
                height:item.fileHeight,
                thumb:item.thumb,
                name:item.name,
            });
        }
        props.onOk?.(files);
        onCancel();
    }
    const findListItemById = (id:number) => {
        for (let i = 0; i < list.length; i++) {
            const item:any = list[i];
            if(item.id === id) {
                return list[i];
            }
        }
        return null;
    }
    // 删除
    const del = (data:any) => {
        modal.confirm({
            title: '警告提示',
            content: '您要删除该项数据吗？删除后将无法恢复！',
            centered: true,
            maskClosable: true,
            onOk: () => {
                return new Promise<void>((resolve)=>{
                    delFileApi(data.id).then(res => {
                        resolve();
                        if (res.code == 200) {
                            getList();
                        } else {
                            message.error(res.msg, 1.2);
                        }
                    })
                })
            }
        })
    }
    // 保存文件
    const uploadFile=(res:CZHFileUploadResult)=> {
        const data = {
            domain: res.domain,
            type: res.type,
            name:res.name,
            key:res.key,
            url:res.url,
            pid,
            fileWidth:res.fileWidth,
            fileHeight:res.fileHeight,
            fileSize:res.fileSize,
            thumb:res.thumb
        }
        const fileType=res.type;
        addFileApi(data).then(res => {
            if (res.code == 200) {
                getList();
                message.success('成功！', 1.2);
                if (fileType === 8) {
                    setLoading(false);
                    setCreateVisible(false);
                }
            } else {
                message.error(res.msg, 1.2);
            }
        })
    }
    const onChange = (e:any) => {
        if(e.length>max)
        {
            e.splice(0,1);
        }
        setValue(e)
    }
    return (
        <React.Fragment>
            <CZHModal
                open={open}
                title={(
                    <div className='flexCenter'>
                        <Title title='文件库:' />
                        {fileName.map((item, index) => (
                            <span className={`color9 ${index < fileName.length - 1 ? ' cursor' : ''}`} key={String(item.id)} onClick={() => openFolder(item, index)}>{item.name}</span>
                        ))}
                    </div>
                )}
                width={824}
                onCancel={onCancel}
                footer={<div className={"customerModalFooter"}>
                    <Button onClick={()=>{
                        onCancel();
                    }}>取消</Button>
                    <Button loading={loading} onClick={onOk} disabled={value.length===0} type={"primary"}>确定</Button>
                </div>}
            >
                <div className='fileLibToolbar'>
                    <Input
                        placeholder='请输入文件名称'
                        style={{ width: 200 }}
                        allowClear
                        onChange={(e) => {
                            setName(e.target.value);
                        }}
                    />
                    <Select
                        placeholder='请选择文件类型'
                        options={fileType}
                        mode='multiple'
                        value={types}
                        style={{ width: 280 }}
                        allowClear={types.length===0}
                        disabled={types.length>0}
                        onChange={(values) => {
                            setTypes(values);
                            setPage(1);
                        }}
                    />
                    <div className='fileLibToolbarActions'>
                        <CZHUpload
                            dir={dir}
                            cropProps={props.cropProps}
                            crop={props.crop}
                            multiple={!props.crop}
                            accept={accept}
                            onPercent={(num) => {
                                setPercent(num);
                                if (num > 100) {
                                    setPercent(0);
                                }
                            }}
                            onError={(e) => {
                                setPercent(0);
                                message.error(e);
                            }}
                            onOk={(res:CZHFileUploadResult) => {
                                setPercent(0);
                                uploadFile(res)
                            }}
                        >
                            <Button type='primary'>上传文件</Button>
                            {percent > 0 && <span className='jdttt' style={{ width: percent }} onClick={(e) => {
                                e.preventDefault()
                                e.stopPropagation()
                            }}></span>}
                        </CZHUpload>
                        <Button type='primary' danger onClick={() =>{
                            FormModal.open({
                                title:`创建文件夹`,
                                children:<AddFolder pid={pid} onOk={()=>{
                                    getList();
                                }} />,
                                width:360
                            })
                        }}>创建文件夹</Button>
                    </div>
                </div>
                {/* 列表 */}
                <div className='uploadlist'>
                    <Checkbox.Group value={value} onChange={onChange}>
                    {list.map((item:any) => (
                        <div
                            className='item'
                            key={String(item.id)}
                            onDoubleClick={() => openFolder(item)}

                        >
                            <CZHRenderThumb width={80} height={80} file={{
                                uid:"uuid-"+Helper.getRandomString(4)+"-"+item.id,
                                type:item.type,
                                url:item.url,
                                width:item.fileWidth,
                                height:item.fileHeight,
                                thumb:item.thumb,
                                name:item.name,
                            }} onDel={()=>{
                                del(item)
                            }}
                            />
                            <p className={"line1 cursor"}>
                                {item.type===8?
                                    <span>{item.name}</span>
                                :
                                    <Tooltip title={item.name}>
                                        <Checkbox
                                            // disabled={types && item.type!=props.type}
                                            value={item.id}>{item.name}</Checkbox>
                                    </Tooltip>
                                }
                            </p>
                        </div>
                    ))}
                    </Checkbox.Group>
                    {list.length === 0 && <div className='fileLibEmpty'>暂无文件</div>}
                </div>
                {/* 页码 */}
                {total > 0 && <Pagination
                    style={{ marginBottom: 12 }}
                    size={'small'}
                    current={page}
                    pageSize={24}
                    total={total}
                    showTotal={(total, range) => {
                        return `共${total}条记录，本页展示${range[0]}-${range[1]}条记录`
                    }}
                    showSizeChanger={false}
                    onChange={(page) => {
                        setPage(page)
                    }}
                />}

            </CZHModal>
        </React.Fragment>
    )
}
// 创建一个全局容器用于挂载modal
const createContainer = () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    return container;
};
CZHFileList.open=(props:fileListProps)=>{
    // 创建容器
    const container = createContainer();
    const root = createRoot(container);

    // 用于关闭modal的方法
    const close = () => {
        root.unmount();
        container.remove();
    };

    // 渲染组件：ModalRoot 注入主应用的主题 token 与 antd App 上下文
    root.render(
        <ModalRoot>
            <CZHFileList
                {...props}
                open={true}
                onOk={(files:CZHFileItem[]) => {
                    root.unmount();
                    container.remove();
                    props.onOk?.(files);
                }}
            />
        </ModalRoot>
    );

    // 返回关闭方法
    return { close };
}
export default CZHFileList;
