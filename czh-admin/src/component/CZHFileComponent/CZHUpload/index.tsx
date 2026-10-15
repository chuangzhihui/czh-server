import React, { useState, forwardRef, useImperativeHandle, useEffect } from 'react';
import {Upload, App, Modal, UploadProps, message} from 'antd';
import ImgCrop, {ImgCropProps} from 'antd-img-crop';
import COS from 'cos-js-sdk-v5';
import {getUploadTokenApi} from "../../../api/set/SetApi";
import {HttpResponse} from "../../../util/request";

const key = '';
interface fileReadResponse {
    width:number;
    height:number;
}
export interface CZHFileUploadResult {
    domain:number;//文件保存在哪里的 0虚拟文件夹 1七牛 2阿里oss 3腾讯cos 4本地服务器 5火山云
    type:number;//文件类型 1图片 2视频 3 Excel 4 word 5 pdf 6 zip 7 未知类型文件 8文件夹
    name:string;//文件名
    key:string;//上传到第三方的key或者本地真实路径
    url:string;//文件URL地址
    fileWidth:number;//图片或者视频宽其它为0
    fileHeight:number;//图片或者视频宽其它为0
    fileSize:number;//文件大小kb
    thumb?:string;//缩略图
}
interface CZHUploadProps extends  UploadProps  {
    dir:string;//上传目录 如 admin/file
    onPercent?:(percent:number) => void;//进度回调
    onOk?:(res:CZHFileUploadResult)=>void;
    onError?:(error:string) => void;//上传错误回调
    crop?:boolean;//是否需要裁剪
    cropProps?:ImgCropProps;//裁剪参数

}
export  const customerUpload=async (row:any)=> {

    const res=await getUploadTokenApi();
    const uploadConfig=res.data;
    const action=uploadConfig.host ?? '';
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    const file=row.file;
    xhr.open('POST',action, true);
    xhr.setRequestHeader('token', uploadConfig.token ?? '');
    formData.append('file',file);
    formData.append('dir',row.data.dir);
    formData.append('type',row.data.type);
    // 进度事件
    xhr.upload.addEventListener('progress', (e)=> {

        if (e.lengthComputable) {
            const percentComplete = (e.loaded / e.total) * 100;
            row.onProgress?.({ percentComplete });
        }
    }, false);
    // 上传完成
    xhr.onload = ()=> {
        if (xhr.status >= 200 && xhr.status < 300) {
            const response:HttpResponse<CZHFileUploadResult>=JSON.parse(xhr.responseText);
            if(response.code===200)
            {
                row.onSuccess?.(response.data);
            }else{
                return row.onError?.("上传失败:"+response.msg);
            }
        } else {
            row.onError?.("上传失败")
        }
    };
    // 错误处理
    xhr.onerror = (err)=>{
        row.onError("上传失败")
    }
    // 发送文件
    xhr.send(formData);
}
const CZHUpload = (props:CZHUploadProps, _ref:any) => {

    const [fileList, setFileList] = useState(props.fileList || []);  // 上传文件
    const {dir="",crop=false}=props;
    // 获取上传文件类型
    const getFileType=(file:any):number=> {
        let type = 7, houz = '';
        const nameList = file.name && file.name.split('.');
        houz = nameList[nameList.length - 1];
        if (file.type == 'application/vnd.ms-excel' || file.type == 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || houz == 'xls' || houz == 'xlsx') {
            type = 3
        } else if (file.type == 'application/zip' || file.type == 'application/rar' || houz == 'rar' || houz == 'rar4') {
            type = 6;
        } else if (file.type == 'application/pdf') {
            type = 5;
        } else if (houz == 'doc' || houz == 'docx') {
            type = 4;
        } else if (file.type == 'video/mp4' || houz == 'avi' || houz == 'flv' || file.type == 'audio/mpeg' || houz == 'mp3') {
            type = 2;
        } else if (houz == 'jpg' || houz == 'jpeg' || houz == 'png' || houz == 'gif') {
            type = 1;
        }
        return type;
    }
    const renderUpload=()=>{
        return(
            <Upload
                showUploadList={false}  //是否展示文件列表
                action={""}
                fileList={fileList}
                data={async (file) => {
                   return {
                       dir:dir,
                       type:getFileType(file)
                   }
                }}
                onChange={async (e:any) => {
                    setFileList(e.fileList);
                    // 上传中
                    if (e.file.status == 'uploading' && e.event?.percentComplete) {
                        const percent=e.file.percent || e.event?.percentComplete
                        props.onPercent?.(Number(percent))
                    }
                    //上传失败
                    if(e.file.status==="error") {
                        props.onError?.(e.file.error);
                    }
                    // 上传完成
                    if (e.file.status == 'done') {
                        props.onOk?.(e.file.response)
                    }
                    return true;
                }}
                customRequest={customerUpload}
                {...props}
            ></Upload>
        );
    }
    return (
        <>
            {crop?
                <ImgCrop {...props.cropProps}>
                    {renderUpload()}
                </ImgCrop>
                :
                <>
                    {renderUpload()}
                </>
            }
        </>

    )
}
export default forwardRef(CZHUpload);
