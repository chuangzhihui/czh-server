import React, {forwardRef, useEffect, useRef, useState} from "react";
import FileList, { CZHFileItem} from "../CZHFileList";
import {Button, Image, Upload, UploadFile, UploadProps} from "antd";
import Helper from "../../../util/Helper";
import AddImg from "../../../static/img.png"
import {ImgCropProps} from "antd-img-crop";
import CZHRenderThumb from "../CZHRenderThumb";
import "../CZHFile/index.scss"
import CZHFileList from "../CZHFileList";
interface CZHUploadMediaProps {
    value?: CZHFileItem[];//图片集
    max?:number;//最大上传数量
    onChange?: (value: CZHFileItem[]) => void;//change回调
    types?:number[];//文件类型 1图片 2视频 -默认为只选择图片
    crop?:boolean;//上传是否需要裁剪
    cropProps?:ImgCropProps;//裁剪参数
}

const CZHUploadMedia= (props:CZHUploadMediaProps) => {
    const {max=1,value=[],types=[1]}=props;
    const delItem=(index:number)=>{
        const values:CZHFileItem[]=JSON.parse(JSON.stringify(props.value || []));
        values.splice(index,1);
        props.onChange?.(values);
    }
    const openFileList=()=>{
        CZHFileList.open({
            types,
            max,
            crop:props.crop,
            cropProps:props.cropProps,
            onOk:(files:CZHFileItem[]) => {
                let newVals:CZHFileItem[]=JSON.parse(JSON.stringify(props.value || []));
                newVals=newVals.concat(files);
                props.onChange?.(newVals);
            }
        })
    }
    return (
        <div className={"CZHUploadMedia"}>
            {value && value.length>0 && value.map((file:CZHFileItem,index:number)=>{
                return(
                    <CZHRenderThumb key={index} onDel={()=>{
                        delItem(index);
                    }} file={file} />
                );
            })}
            {max>(props.value?.length || 0) &&
                <div className={"item"} key="add-button">
                    <img alt='' onClick={openFileList} src={AddImg} className="addImgBtn cursor" />
                </div>
            }
        </div>
    )
}
export default CZHUploadMedia;
/**
 * CZHFileItem转UploadFile
 */
export const CZHFileItemToUploadFile=(sources:CZHFileItem[]):UploadFile[] => {
    const result:UploadFile[] = [];
    for(let i=0;i<sources.length;i++){
        const item:CZHFileItem = sources[i];
        result.push({
            uid:item.uid,
            name:item.name,
            status:"done",
            url:item.url,
            thumbUrl:item.thumb
        })
    }
    return result;
}
/**
 * 文件数组转字符串 将所有的图片地址用逗号间隔为字符串
 * @param sources
 * @constructor
 */
export const CZHFileItemToString=(sources:CZHFileItem[]):string=>{
    const result:string[] = [];
    for (let i=0;i<sources.length;i++){
        result.push(sources[i].url)
    }
    return result.join(",");
}
/**
 * 字符串转文件数组-和上面个方法相反
 * @param fileString
 * @constructor
 */
export const StringToCZHFileItem=(fileString:string):CZHFileItem[]=>{
    const result:CZHFileItem[] = [];
    const sources:string[] = fileString.split(",");
    for(let i=0;i<sources.length;i++){
        const names=sources[i].split("/");
        const name=names[names.length-1];
        result.push({
            uid:"serverfile"+i+"-"+Helper.getRandomString(4)+"-"+Helper.getRandomString(4),
            type:1,
            name:name,
            url:sources[i],
            width:0,
            height:0,
            thumb:sources[i]
        })
    }
    return result;
}

