import React, {forwardRef, useEffect, useRef, useState} from "react";
import {Select, SelectProps} from 'antd';
import {getAdminSelectListApi, getRoleSelectListApi} from "../../api/common/CommonApi";
type SelectType = 'allrole' | 'alladmin';
interface CZHSelectProps extends SelectProps{
    type:SelectType;

}
const CZHSelect=(props:CZHSelectProps, ref:any)=>{
    const [options,setOptions]=useState<any>([]);
    const [url,setUrl]=useState<string>("")
    useEffect(()=>{
        let api:any=null;
        if(props.type=="allrole")
        {
            api=getRoleSelectListApi;
        }else if(props.type=="alladmin")
        {
           api=getAdminSelectListApi;
        }
        if(api===null) return;
        api().then((res:any)=>{
            if (res.code === 200) {
                setOptions(res.data)
            }
        })
    },[])


    return(
        <Select
            placeholder='请选择'
            suffixIcon={(<span className='iconfont icon-xia'></span>)}
            allowClear
            {...props}
            className={`pubSelt ${props.className}`}
            options={options}
        />
    );
}

export default forwardRef(CZHSelect);
