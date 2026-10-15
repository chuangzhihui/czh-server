import Title from "../../CZHTitle";
import "./index.scss"
import React, {useEffect, useRef, useState} from "react";
import CZHModal from "../../CZHModal";
import ModalRoot from "../../CZHModal/ModalRoot";
import {createRoot} from "react-dom/client";
import {App, Button} from "antd";
import { Stage, Layer, Line, Text, Rect, Transformer } from 'react-konva'
import DrawerMenuItem from "../DrawerMenuItem";

import LayerSet from "../DrawerMenuItem/icons/setLayer.png";
import CycleIcon from "../DrawerMenuItem/icons/cycle.png";
import Easer from "../DrawerMenuItem/icons/easer.png";
import Huabi from "../DrawerMenuItem/icons/huabi.png";
import LineIcon from "../DrawerMenuItem/icons/line.png";
import RectIcon from "../DrawerMenuItem/icons/rect.png";
import TextIcon from "../DrawerMenuItem/icons/text.png";
import {getUploadTokenApi} from "../../../api/set/SetApi";
import Helper from "../../../util/Helper";
import CZHUpload, {customerUpload} from "../../CZHFileComponent/CZHUpload";

export interface CZHImageDrawerProps{
    open:boolean;
    onSuccess:(url:string)=>void;
}
enum LayerMode{
    LayerSet,
    HUABI,
    Easer,
    RECT,
    CYCLE,
    LINE,
    TEXT
}
const CZHImageDrawer=(props:CZHImageDrawerProps)=>{
    /** 用 App.useApp() 拿实例：静态 message 不消费 ConfigProvider 上下文（主题与前缀都会失效） */
    const { message } = App.useApp();
    const [layerMode,setLayerMode] = useState<LayerMode>(LayerMode.LayerSet);
    const [open, setOpen] = React.useState<boolean>(props.open);
    const [mainLayerSize,setMainLayerSize] = useState({width:0, height:0});//画布父窗口大小
    const [layerSize,setLayerSize] = useState<{
        x: number;
        y: number;
        width: number;
        height: number;
    }>({
        x: 100, // 左上角x坐标
        y: 100, // 左上角y坐标
        width: 800, // 宽度
        height: 600, // 高度
    });//画布大小
    const [tool, setTool] = useState<"pen"|"eraser">('pen');//画笔类型
    const [lines, setLines] = useState<any[]>([]);
    const [loading,setLoading] = useState<boolean>(false);//确认按钮加载状态

    const transformerRef=useRef<any>(null);
    const layerRef=useRef<any>(null);//画布层
    const mainLayerRef=useRef<HTMLDivElement>(null);//画布窗口
    const isDrawing = useRef(false);
    const stageRef=useRef(null);
    useEffect(() => {
        if(mainLayerRef.current){
            setLayerSize({
                ...layerSize,
                x: (mainLayerRef.current.clientWidth-layerSize.width)/2,
                y: (mainLayerRef.current.clientHeight-layerSize.height)/2,
            })
            setMainLayerSize({
                width:mainLayerRef.current.clientWidth,
                height:mainLayerRef.current.clientHeight
            })

        }
    }, []);
    useEffect(() => {
        setTool(layerMode==LayerMode.Easer?'eraser':'pen')
        if (layerMode===LayerMode.LayerSet && transformerRef.current && mainLayerSize.width>0) {
            transformerRef.current.attachTo(layerRef.current);
            transformerRef.current.getLayer().batchDraw(); // 重新绘制以更新位置
        }
    }, [layerMode,mainLayerSize]);

    const handleMouseDown = (e:any) => {
        if (layerMode!==LayerMode.HUABI && layerMode!==LayerMode.Easer) return;
        isDrawing.current = true;
        const pos = e.target.getStage().getPointerPosition();
        setLines([...lines, { tool, points: [pos.x, pos.y] }]);
    };

    const handleMouseMove = (e:any) => {
        if (layerMode!==LayerMode.HUABI && layerMode!==LayerMode.Easer) return;
        // no drawing - skipping
        if (!isDrawing.current) {
            return;
        }
        const stage = e.target.getStage();
        const point = stage.getPointerPosition();
        const lastLine = lines[lines.length - 1];
        // add point
        lastLine.points = lastLine.points.concat([point.x, point.y]);

        // replace last
        lines.splice(lines.length - 1, 1, lastLine);
        setLines(lines.concat());
    };

    const handleMouseUp = () => {
        if (layerMode!==LayerMode.HUABI && layerMode!==LayerMode.Easer) return;
        isDrawing.current = false;
    };


    const uploadImage=async (dataUrl:string)=>{
        //获取上传配置
        setLoading(true);
        const res=await  getUploadTokenApi();
        if(res.code!=200)
        {
            setLoading(false);
            message.error(res.msg);
            return
        }
        const fileName = 'drawer'+Helper.getRandomString(8)+'.png';
        const file=base64ToFile(dataUrl,fileName);
        let action="";
        if (res.data.visible === 1) {  // 七牛
            action = 'https://'+res.data.endpoint;
        } else if (res.data.visible === 2) {  //阿里oss
            action = res.data.domain ?? '';
        } else if (res.data.visible === 3) {  //腾讯
            action = res.data.path ?? '';
        } else if (res.data.visible === 4) {  // 本地服务器
            action = res.data.uploadUrl ?? '';
        }
        const data:any={
            action,
            uploadType:res.data.visible,
            key:fileName,
            name:fileName,
            domain:res.data.domain,
        }
        if(res.data.visible === 1 || res.data.visible===4) {  // 七牛
            data.token=res.data.token;
        }else if(res.data.visible === 2) {  //阿里oss
            data.policy=res.data.policy;
            data.OSSAccessKeyId=res.data.OSSAccessKeyId;
            data.signature=res.data.signature;
        }else if(res.data.visible === 3) {  //腾讯
            data.TmpSecretId=res.data.TmpSecretId;
            data.TmpSecretKey=res.data.TmpSecretKey;
            data.SecurityToken=res.data.SecurityToken;
            data.StartTime=res.data.StartTime;
            data.ExpiredTime=res.data.ExpiredTime;
            data.Bucket=res.data.Bucket;
            data.Region=res.data.Region;
        }
        customerUpload({
            file,
            data,
            onProgress:(e:any)=>{
            },
            onSuccess:(res:any)=>{
                setLoading(false);
                setOpen(false);
                props.onSuccess(res.url);
                // message.success('上传成功');

            },
            onError:(err:any)=>{
                setLoading(false);
                message.error('上传失败');
            }
        });
    }

    const base64ToFile=(base64Str:string,fileName:string)=>{

        //  1. 拆分 Base64 字符串，提取 MIME 类型和纯编码数据
        const matchResult = base64Str.match(/^data:(.+?);base64,(.+)$/);
        if (!matchResult) {
            throw new Error('无效的 Base64 字符串（缺少 data: 前缀或 base64 标识）');
        }

        const [, mime = 'application/octet-stream', base64Data] = matchResult;

        // 2. 解码 Base64 数据（atob 解码 base64 为 ASCII 格式的二进制数据）
        const decodedData = atob(base64Data);

        // 3. 将解码后的 ASCII 数据转为 Uint8Array（二进制字节数组）
        const byteLength = decodedData.length;
        const uint8Array = new Uint8Array(byteLength);
        for (let i = 0; i < byteLength; i++) {
            uint8Array[i] = decodedData.charCodeAt(i); // 每个字符转成对应的字节码
        }

        // 4. 创建 Blob 对象（二进制容器）
        const blob = new Blob([uint8Array], { type: mime });

        // 5. 包装为 File 对象（File 是 Blob 的子类，多了文件名和最后修改时间）
        return new File([blob], fileName, {
            type: mime,
            lastModified: Date.now() // 可选：设置文件最后修改时间（当前时间）
        });
    }
    return(
        <CZHModal
            open={open}
            title={(
                <div className='flexCenter'>
                    <Title title='图片绘制' />
                </div>
            )}
            width={"80vw"}
            onCancel={() => setOpen(false)}
        >
            <div className={"drawerContainer"}>
                <div className={"menu"}>
                    <div className={"menuItems"}>
                        <DrawerMenuItem title={"画布设置"} active={layerMode===LayerMode.LayerSet} icon={LayerSet} onClick={()=>{setLayerMode(LayerMode.LayerSet)}} />
                        <DrawerMenuItem title={"画笔"} active={layerMode===LayerMode.HUABI} icon={Huabi}  onClick={()=>{setLayerMode(LayerMode.HUABI)}} />
                        <DrawerMenuItem title={"橡皮"} active={layerMode===LayerMode.Easer} icon={Easer}  onClick={()=>{setLayerMode(LayerMode.Easer)}} />
                        {/*<DrawerMenuItem title={"矩形"} active={layerMode===LayerMode.RECT} icon={RectIcon}  onClick={()=>{setLayerMode(LayerMode.RECT)}} />*/}
                        {/*<DrawerMenuItem title={"圆形"} active={layerMode===LayerMode.CYCLE} icon={CycleIcon}  onClick={()=>{setLayerMode(LayerMode.CYCLE)}} />*/}
                        {/*<DrawerMenuItem title={"直线"} active={layerMode===LayerMode.LINE} icon={LineIcon}  onClick={()=>{setLayerMode(LayerMode.LINE)}} />*/}
                        {/*<DrawerMenuItem title={"文字"} active={layerMode===LayerMode.TEXT} icon={TextIcon}  onClick={()=>{setLayerMode(LayerMode.TEXT)}} />*/}
                    </div>
                    <Button onClick={()=>{
                        const stage:any | null = stageRef.current;
                        if (!stage) return;
                        const dataURL = stage.toDataURL({
                            x: layerSize.x,        // 区域左上角 X 坐标（相对于 Stage 原点）
                            y: layerSize.y,        // 区域左上角 Y 坐标
                            width: layerSize.width,   // 区域宽度
                            height: layerSize.height,  // 区域高度
                            format: 'png',
                            quality: 1,
                        });
                        // setOpen(false)
                        // props.onSuccess("dfsdfsd");
                        uploadImage(dataURL);
                        // console.log(dataURL)
                        // // 下载图片
                        // const link = document.createElement('a');
                        // link.href = dataURL;
                        // link.download = 'konva-custom-area.png';
                        // document.body.appendChild(link);
                        // link.click();
                        // document.body.removeChild(link);
                    }} type="primary" loading={loading} className=' block' >确定</Button>
                </div>
                <div ref={mainLayerRef} className={`layer ${layerMode===LayerMode.HUABI?"penLayer":""}  ${layerMode===LayerMode.Easer && "eraserLayer"} `}>
                    {mainLayerSize.width>0 &&
                        <Stage
                            ref={stageRef}
                            style={{flex:1}}
                            width={mainLayerSize.width}
                            height={mainLayerSize.height}
                            onMouseDown={handleMouseDown}
                            onMousemove={handleMouseMove}
                            onMouseup={handleMouseUp}
                            onTouchStart={handleMouseDown}
                            onTouchMove={handleMouseMove}
                            onTouchEnd={handleMouseUp}
                        >
                            <Layer>
                                <Rect
                                    ref={layerRef}
                                    x={layerSize.x}
                                    y={layerSize.y}
                                    width={layerSize.width}
                                    height={layerSize.height}
                                    // 允许拖拽（可选，如需同时支持拖拽位置）
                                    draggable={layerMode===LayerMode.LayerSet}
                                    fill="#fff" // 背景色（灰色示例）
                                    stroke={"transparent"} // 边框（未选中时透明）
                                    strokeWidth={0}
                                    onDragEnd={(e) => {
                                        setLayerSize({
                                            ...layerSize,
                                            x: e.target.x(),
                                            y: e.target.y(),
                                        });
                                    }}
                                />
                                {layerMode===LayerMode.LayerSet && (
                                    <Transformer
                                        ref={transformerRef}
                                        // 禁用旋转（只保留缩放功能）
                                        rotateEnabled={false}
                                        // 缩放时是否保持宽高比（false则允许自由缩放）
                                        keepRatio={false}
                                        // 缩放结束后更新尺寸状态
                                        onTransformEnd={(e) => {
                                            const node = e.target;
                                            const visualWidth = node.width() * Math.abs(node.scaleX());
                                            const visualHeight = node.height() * Math.abs(node.scaleY());
                                            // 2. 固化到原始宽高，重置缩放比例
                                            node.width(visualWidth);
                                            node.height(visualHeight);
                                            node.scaleX(1);
                                            node.scaleY(1);
                                            // 3. 更新状态（此时 node.width() 就是缩放后尺寸）
                                            setLayerSize({
                                                x: node.x(),
                                                y: node.y(),
                                                width: node.width(),
                                                height: node.height(),
                                            });
                                            // 重要：更新 Transformer 适配新的宽高
                                            transformerRef.current?.attachTo(node);
                                            transformerRef.current?.getLayer()?.batchDraw();
                                        }}
                                    />
                                )}
                            </Layer>
                            <Layer >
                                {/*<Rect*/}
                                {/*    ref={rectRef}*/}
                                {/*    {...rectProps}*/}
                                {/*    onClick={handleRectClick}*/}
                                {/*    // 选中时显示边框，增强视觉反馈*/}
                                {/*    stroke={isSelected ? 'blue' : 'transparent'}*/}
                                {/*    // 允许拖拽（可选，如需同时支持拖拽位置）*/}
                                {/*    draggable={isSelected}*/}
                                {/*    // 拖拽结束后更新位置状态*/}
                                {/*    onDragEnd={(e) => {*/}
                                {/*        setRectProps({*/}
                                {/*            ...rectProps,*/}
                                {/*            x: e.target.x(),*/}
                                {/*            y: e.target.y(),*/}
                                {/*        });*/}
                                {/*    }}*/}
                                {/*/>*/}
                                {/* 变换控制器（仅在选中时显示） */}


                                {lines.map((line, i) => (
                                    <Line
                                        key={i}
                                        points={line.points}
                                        stroke="#df4b26"
                                        strokeWidth={line.tool === 'eraser' ?20:3}
                                        tension={0.5}
                                        lineCap="round"
                                        lineJoin="round"
                                        globalCompositeOperation={
                                            line.tool === 'eraser' ? 'destination-out' : 'source-over'
                                        }
                                    />
                                ))}
                            </Layer>
                        </Stage>
                    }
                </div>

            </div>

        </CZHModal>
    );
}
// 创建一个全局容器用于挂载modal
const createContainer = () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    return container;
};
CZHImageDrawer.open=(props:CZHImageDrawerProps)=>{
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
            <CZHImageDrawer
                {...props}
            />
        </ModalRoot>
    );

    // 返回关闭方法
    return { close };
}
export default CZHImageDrawer;