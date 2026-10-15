import { App, ConfigProvider } from 'antd';
import React, { useEffect, useState } from 'react';
import locale from 'antd/locale/zh_CN';
import { getActiveTheme, THEME_CHANGE_EVENT, toAntdToken, type ThemeToken } from '../../../util/ThemeUtil';

export interface ModalRootProps {
    /** 调用方自定义包裹器，包在最内层，用于注入业务上下文（如带 setter 的 ThemeContext） */
    wrap?: (children: React.ReactNode) => React.ReactNode;
    children: React.ReactNode;
}

/**
 * 独立 root 的根节点：给 createRoot 渲染的内容补齐主应用的上下文
 * 脱离主 React 树的渲染（FormModal.open / CZHFileList.open / CZHImageDrawer.open）拿不到
 * App.tsx 里的 ConfigProvider 与 antd App，直接用会导致组件色回到 antd 默认蓝、
 * App.useApp() 拿到空对象，所以三处统一走这里
 *
 * 1. ConfigProvider：locale + 运行期主题 token（含主题弹窗「预览未落盘」的临时值）
 * 2. App：提供 message / modal / notification 上下文，App.useApp() 才有实例
 * 3. wrap：调用方自定义注入，包在最内层（见 Layout.openSetColor）
 * 主题变化（czh:theme-change）时自动跟随，无需重新打开
 */
const ModalRoot = (props: ModalRootProps) => {
    const { wrap, children } = props;

    /** 主题取自运行期当前主题，与页面内同源 */
    const [token, setToken] = useState<ThemeToken>(() => getActiveTheme());

    /** 监听换肤广播：主应用 setToken / saveToken 后同步更新，卸载时移除监听 */
    useEffect(() => {
        const onThemeChange = (e: Event) => {
            const next = (e as CustomEvent<ThemeToken>).detail;
            if (next) setToken(next);
        };
        window.addEventListener(THEME_CHANGE_EVENT, onThemeChange);
        return () => window.removeEventListener(THEME_CHANGE_EVENT, onThemeChange);
    }, []);

    return (
        <ConfigProvider locale={locale} theme={{ token: toAntdToken(token) }}>
            <App>{wrap ? wrap(children) : children}</App>
        </ConfigProvider>
    );
};

export default ModalRoot;
