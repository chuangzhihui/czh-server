import React, {forwardRef, ReactNode, useMemo} from 'react';
import {Dropdown, theme} from 'antd';
import {DownOutlined} from '@ant-design/icons';
import './index.scss';

interface CZHTableActionProps extends React.HTMLAttributes<HTMLDivElement> {
    actions: ReactNode[];//操作按钮元素
    maxCount?: number;//最多渲染的按钮数量（含"更多"），超出部分收进"更多"下拉菜单，默认3
    moreText?: ReactNode;//"更多"按钮文案，默认"更多"
}

const CZHTableAction = (_props: CZHTableActionProps, ref: any) => {
    const {
        token: {colorPrimary}
    } = theme.useToken();
    const {actions, maxCount = 3, moreText = '更多', className, ...rest} = _props;
    //过滤条件渲染产生的空元素（false/null/undefined）
    const actionList = useMemo(() => actions.filter(Boolean), [actions]);
    //是否需要"更多"下拉菜单（"更多"本身占一个按钮位）
    const hasMore = actionList.length > maxCount;
    //直接展示的按钮
    const showActions = useMemo(() => actionList.slice(0, hasMore ? maxCount - 1 : maxCount), [actionList, maxCount, hasMore]);
    //收进"更多"下拉菜单的按钮
    const moreActions = useMemo(() => hasMore ? actionList.slice(maxCount - 1) : [], [actionList, maxCount, hasMore]);
    return (
        <div className={`CZHTableAction ${className || ''}`} {...rest} ref={ref}>
            {showActions.map((action, index) => (
                <React.Fragment key={index}>{action}</React.Fragment>
            ))}
            {moreActions.length > 0 && (
                <Dropdown
                    trigger={['click']}
                    menu={{
                        items: moreActions.map((action, index) => ({
                            key: String(index),
                            label: action
                        }))
                    }}
                >
                    <p className='CZHTableActionMore' style={{color: colorPrimary}}>
                        {moreText}
                        <DownOutlined/>
                    </p>
                </Dropdown>
            )}
        </div>
    );
};

export default forwardRef(CZHTableAction);
