import React, {forwardRef, useEffect, useRef, useState} from "react";
import { EyeOutlined ,PlayCircleOutlined,DeleteOutlined} from '@ant-design/icons';
import {CZHFileItem} from "../CZHFileList";
import ExcelThumb from "../../../static/excel.png"
import WordThumb from "../../../static/word.png"
import PDFThumb from "../../../static/PDF.png"
import ZIPThumb from "../../../static/ysb.png"
import OtherThumb from "../../../static/other.png"
import DirThumb from "../../../static/wjj.png"
import "../CZHFile/index.scss"
import {Image} from "antd";
interface CZHRenderThumbProps{
    file:CZHFileItem;
    onDel:()=>void;//点击删除的回调
    onOpenDir?:()=>void;//点击打开目录回调
    width?:number;//宽
    height?:number;//高
}

const CZHRenderThumb = (props: CZHRenderThumbProps) => {

    const [imgOpen,setImgOpen] = useState<boolean>(false);
    const [videoOpen,setVideoOpen] = useState<boolean>(false);
    const {file,width=102,height=102}=props;

    return(
        <div onClick={()=>{
            if(file.type===1) {
                setImgOpen(true);
            }else if(file.type===2) {
                setVideoOpen(true);
            }else if(file.type===8){
        // 无额外副作用，占位以满足 no-empty
    }else {
                window.open(file.url);
            }
        }} className={"prevContainer"} style={{width:width,height:height}} >
            {props.file.type===1 && <img className={"prevImg"}  src={props.file.thumb || props.file.url} />}
            {props.file.type===2 && <img className={"prevImg"} src={props.file.thumb} />}
            {props.file.type===3 && <img className={"prevImg"} src={ExcelThumb} />}
            {props.file.type===4 && <img className={"prevImg"} src={WordThumb} />}
            {props.file.type===5 && <img className={"prevImg"} src={PDFThumb} />}
            {props.file.type===6 && <img className={"prevImg"} src={ZIPThumb} />}
            {props.file.type===7 && <img className={"prevImg"} src={OtherThumb} />}
            {props.file.type===8 && <img className={"prevImg"} src={DirThumb} />}
            <div className={"maskView"}>
                {file.type===2 ?  <PlayCircleOutlined style={{color:"#fff",fontSize:18}}  />
                    : <>
                    {file.type!=8 && <EyeOutlined style={{color:"#fff",fontSize:18,lineHeight:24}}  />}
                    </>
                }
                <DeleteOutlined onClick={(e)=>{
                    e.preventDefault()
                    e.stopPropagation()
                    props.onDel();
                }} style={{color:"#fff",fontSize:18,lineHeight:24}} />
            </div>
            <Image
                wrapperStyle={{ display: 'none' }}
                preview={{
                    visible: imgOpen,
                    onVisibleChange: (visible) => setImgOpen(visible)
                }}
                src={file.url}
            />
            <Image
                wrapperStyle={{ display: 'none' }}
                preview={{
                    visible: videoOpen,
                    onVisibleChange: (visible) => setVideoOpen(visible),
                    imageRender: () => (
                        <video
                            muted
                            width="80%"
                            height={"80%"}
                            controls
                            src={file.url}
                        />
                    ),
                }}
            />
        </div>
    );
}
export default forwardRef(CZHRenderThumb);
