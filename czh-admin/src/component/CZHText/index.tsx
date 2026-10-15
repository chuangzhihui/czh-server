import React, {useImperativeHandle, forwardRef, ReactNode} from 'react';
import { Button, theme } from 'antd';
type TextTpes= "primary" | "success" | "warning" | "error";
interface TextProps extends React.ParamHTMLAttributes<any>{
    type?:TextTpes;
    children:ReactNode,
}
const CZHText = (_props: TextProps, ref:any) => {
    const {
        token: { colorPrimary, colorSuccess, colorWarning, colorError }
    } = theme.useToken();
   const {type="primary"}=_props;
    return (
        <p
            style={{
                color: type === 'primary' ? colorPrimary : (
                    type === 'success' ? colorSuccess : (
                        type === 'warning' ? colorWarning : (
                            type === 'error' ? colorError : '#333'
                        )
                    )
                )
            }}
            {..._props}
        >{_props.children}</p>
    )
};

export default forwardRef(CZHText);
