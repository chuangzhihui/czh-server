import React, {forwardRef, useEffect, useRef, useState} from "react";
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import { Editor, Toolbar } from '@wangeditor/editor-for-react'
import {DomEditor, IDomEditor, Boot, IToolbarConfig} from '@wangeditor/editor'
import CZHFileList, {CZHFileItem} from "../CZHFileComponent/CZHFileList";
import CZHUeditorButton from "./CZHUeditorButtonMenu";
const menu1Conf = {
    key: 'DrawImage', // 定义 menu key ：要保证唯一、不重复（重要）
    factory() {
        return new CZHUeditorButton("图片绘制","button") // 把 `YourMenuClass` 替换为你菜单的 class
    },
}
Boot.registerMenu(menu1Conf)
export interface  CZHUeditorProps {
    value?:string;//富文本内容
    onChange?: (value:string) => void;
    placeholder?:string;
}
const CZHUeditor= (props:CZHUeditorProps) => {
    const toolBarRef = useRef<any>(null);
    // editor 实例
    const [editor, setEditor] = useState<IDomEditor | null>(null) // TS 语法S 语法
    // 编辑器内容
    const [html, setHtml] = useState(props.value || "")
    const {placeholder="请输入内容"}=props;
    // 工具栏配置
    const toolbarConfig: Partial<IToolbarConfig> = {
    };
    toolbarConfig.toolbarKeys=[
        "headerSelect",
        "blockquote",
        "|",
        "bold",
        "underline",
        "italic",

        "color",
        "bgColor",
        "|",
        "fontSize",
        "fontFamily",
        "lineHeight",
        "|",
        "bulletedList",
        "numberedList",
        "todo",
        {
            "key": "group-justify",
            "title": "对齐",
            "iconSvg": "<svg viewBox=\"0 0 1024 1024\"><path d=\"M768 793.6v102.4H51.2v-102.4h716.8z m204.8-230.4v102.4H51.2v-102.4h921.6z m-204.8-230.4v102.4H51.2v-102.4h716.8zM972.8 102.4v102.4H51.2V102.4h921.6z\"></path></svg>",
            "menuKeys": [
                "justifyLeft",
                "justifyRight",
                "justifyCenter",
                "justifyJustify"
            ]
        },
        {
            "key": "group-indent",
            "title": "缩进",
            "iconSvg": "<svg viewBox=\"0 0 1024 1024\"><path d=\"M0 64h1024v128H0z m384 192h640v128H384z m0 192h640v128H384z m0 192h640v128H384zM0 832h1024v128H0z m0-128V320l256 192z\"></path></svg>",
            "menuKeys": [
                "indent",
                "delIndent"
            ]
        },
        "|",
        "emotion",
        "insertLink",
        {
            "key": "group-image",
            "title": "图片",
            "iconSvg": "<svg viewBox=\"0 0 1024 1024\"><path d=\"M959.877 128l0.123 0.123v767.775l-0.123 0.122H64.102l-0.122-0.122V128.123l0.122-0.123h895.775zM960 64H64C28.795 64 0 92.795 0 128v768c0 35.205 28.795 64 64 64h896c35.205 0 64-28.795 64-64V128c0-35.205-28.795-64-64-64zM832 288.01c0 53.023-42.988 96.01-96.01 96.01s-96.01-42.987-96.01-96.01S682.967 192 735.99 192 832 234.988 832 288.01zM896 832H128V704l224.01-384 256 320h64l224.01-192z\"></path></svg>",
            "menuKeys": [
                // "insertImage",
                "uploadImage",
                "DrawImage"
            ]
        },
        {
            "key": "group-video",
            "title": "视频",
            "iconSvg": "<svg viewBox=\"0 0 1024 1024\"><path d=\"M981.184 160.096C837.568 139.456 678.848 128 512 128S186.432 139.456 42.816 160.096C15.296 267.808 0 386.848 0 512s15.264 244.16 42.816 351.904C186.464 884.544 345.152 896 512 896s325.568-11.456 469.184-32.096C1008.704 756.192 1024 637.152 1024 512s-15.264-244.16-42.816-351.904zM384 704V320l320 192-320 192z\"></path></svg>",
            "menuKeys": [
                // "insertVideo",
                "uploadVideo"
            ]
        },
        "insertTable",
        "codeBlock",
        "divider",
        "|",
        "undo",
        "redo",
        "|",
        "fullScreen"
    ]
    useEffect(() => {


    }, []);
    useEffect(()=>{
        if(!editor) return
        const toolbar = DomEditor.getToolbar(editor)
        if(!toolbar) return
        const curToolbarConfig = toolbar.getConfig()
    },[editor])
    // 监听value变化，更新编辑器内容
    useEffect(() => {
        if (props.value !== undefined && props.value !== html) {
            setHtml(props.value || "");
        }
    }, [props.value]);


    // 编辑器配置
    const editorConfig:any = {
        placeholder,
        MENU_CONF:[]
    }
    editorConfig.MENU_CONF['uploadImage'] = {
        // 自定义选择图片
        customBrowseAndUpload(insertFn: any) {
            CZHFileList.open({
                onOk:(files:CZHFileItem[])=>{
                    const file:CZHFileItem = files[0];
                    insertFn(file.url,file.name,file.type);
                },
                open:true,
                max:10,
                types:[1]
            })
        },
        text:"文件库"
    }
    editorConfig.MENU_CONF['uploadVideo'] = {
        // 自定义选择视频
        customBrowseAndUpload(insertFn: any) {
            CZHFileList.open({
                onOk:(files:CZHFileItem[])=>{
                    const file:CZHFileItem = files[0];
                    insertFn(file.url,file.thumb);
                },
                open:true,
                max:10,
                types:[2]
            })
        },
    }
    // 及时销毁 editor ，重要！
    useEffect(() => {
        return () => {
            if (editor == null) return
            editor.destroy()
            setEditor(null)
        }
    }, [editor])

    return (
        <div style={{ border: '1px solid #ccc', zIndex: 100 }}>
            <Toolbar
                editor={editor}
                defaultConfig={toolbarConfig}
                mode="default"
                style={{ borderBottom: '1px solid #ccc' }}
            />
            <Editor
                defaultConfig={editorConfig}
                value={html}
                onCreated={setEditor}
                onChange={(editor) => {
                    const newHtml = editor.getHtml();
                    setHtml(newHtml);
                    props.onChange?.(editor.getHtml())

                }}
                mode="default"
                style={{ height: '300px', overflowY: 'hidden' }}
            />
        </div>
    )
}
export default CZHUeditor;
