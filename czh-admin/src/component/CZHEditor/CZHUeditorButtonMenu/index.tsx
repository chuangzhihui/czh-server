import { IButtonMenu, IDomEditor } from '@wangeditor/editor'
import CZHImageDrawer from "../CZHImageDrawer";
export default class CZHUeditorButtonMenu implements IButtonMenu {
    // TS 语法
    // class MyButtonMenu {                       // JS 语法

    constructor(title:string,tag:string) {
        this.title = title // 自定义菜单标题
        this.iconSvg = "<svg t=\"1763567192250\" class=\"icon\" viewBox=\"0 0 1024 1024\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" p-id=\"5350\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"64\" height=\"64\"><path d=\"M555.885714 994.742857h-3.657143c-73.142857-1.828571-128-54.857143-148.114285-146.285714-34.742857-160.914286-96.914286-153.6-182.857143-144.457143-49.371429 5.485714-106.057143 12.8-148.114286-27.428571-34.742857-32.914286-49.371429-85.942857-49.371428-170.057143 0-256 199.314286-462.628571 444.342857-462.628572C665.6 42.057143 841.142857 179.2 896 376.685714c5.485714 21.942857-5.485714 45.714286-27.428571 54.857143-21.942857 7.314286-43.885714-7.314286-51.2-29.257143-43.885714-160.914286-186.514286-272.457143-347.428572-272.457143-199.314286 0-362.057143 168.228571-362.057143 376.685715 0 73.142857 14.628571 98.742857 21.942857 106.057143 14.628571 14.628571 47.542857 9.142857 84.114286 7.314285 87.771429-9.142857 221.257143-25.6 272.457143 210.285715 16.457143 78.628571 54.857143 78.628571 67.657143 78.628571h1.828571c85.942857-1.828571 219.428571-133.485714 261.485715-301.714286 5.485714-21.942857 27.428571-36.571429 51.2-31.085714 21.942857 7.314286 34.742857 29.257143 29.257142 53.028571-49.371429 195.657143-206.628571 365.714286-341.942857 365.714286z\" p-id=\"5351\"></path><path d=\"M171.885714 462.628571c0 40.228571 31.085714 73.142857 71.314286 73.142858 38.4 0 71.314286-32.914286 71.314286-73.142858s-31.085714-73.142857-71.314286-73.142857-71.314286 32.914286-71.314286 73.142857zM393.142857 290.742857c0 31.085714 23.771429 54.857143 53.028572 54.857143 29.257143 0 53.028571-25.6 53.028571-54.857143 0-31.085714-23.771429-54.857143-53.028571-54.857143-29.257143 0-53.028571 25.6-53.028572 54.857143zM621.714286 365.714286c0 25.6 20.114286 45.714286 43.885714 45.714285 23.771429 0 43.885714-20.114286 43.885714-45.714285s-20.114286-45.714286-43.885714-45.714286c-23.771429 0-43.885714 20.114286-43.885714 45.714286zM522.971429 669.257143c-18.285714 0-32.914286-10.971429-40.228572-29.257143-7.314286-21.942857 3.657143-47.542857 25.6-54.857143l448-164.571428c21.942857-7.314286 45.714286 3.657143 53.028572 27.428571 7.314286 21.942857-3.657143 47.542857-25.6 54.857143l-448 162.742857c-3.657143 1.828571-7.314286 3.657143-12.8 3.657143z\" p-id=\"5352\"></path></svg>" // 可选
        this.tag = tag
    }

    title: string
    iconSvg?: string | undefined
    hotkey?: string | undefined
    alwaysEnable?: boolean | undefined
    tag: string
    width?: number | undefined


    // 获取菜单执行时的 value ，用不到则返回空 字符串或 false
    getValue(editor: IDomEditor): string | boolean {
        // TS 语法
        // getValue(editor) {                              // JS 语法
        return ''
    }

    // 菜单是否需要激活（如选中加粗文本，“加粗”菜单会激活），用不到则返回 false
    isActive(editor: IDomEditor): boolean {
        // TS 语法
        // isActive(editor) {                    // JS 语法
        return false
    }

    // 菜单是否需要禁用（如选中 H1 ，“引用”菜单被禁用），用不到则返回 false
    isDisabled(editor: IDomEditor): boolean {
        // TS 语法
        // isDisabled(editor) {                     // JS 语法
        return false
    }

    // 点击菜单时触发的函数
    exec(editor: IDomEditor, value: string | boolean) {
        const editorThis=editor;
        CZHImageDrawer.open({
            open:true,
            onSuccess:(url:string)=>{
                const node = {
                    type: 'paragraph',
                    children: [
                        { text: "" },
                        {type:"image",src:url,alt:"",href:1,children:[{ text: "" }]}
                    ]
                }
                editor.insertNode(node)
                // console.log("上传成功",url,editor,editorThis)
                // editorThis.dangerouslyInsertHtml(`<img src="${url}" />`,true)
                // editor.dangerouslyInsertHtml()
            }
        })
        // let url="https://50yanglao-1333745924.cos.ap-chengdu.myqcloud.com/admin/17569759747445054261.jpg";
        // const node = {
        //     type: 'paragraph',
        //     children: [
        //         { text: "" },
        //         {type:"image",src:url,alt:"",href:1,children:[{ text: "" }]}
        //     ]
        // }
        // editor.insertNode(node)
        // editor.dangerouslyInsertHtml(`<img src="${url}" />`)
        // TS 语法
        // exec(editor, value) {                              // JS 语法
        // if (this.isDisabled(editor)) return
        // editor.insertText(value) // value 即 this.value(editor) 的返回值
    }
}