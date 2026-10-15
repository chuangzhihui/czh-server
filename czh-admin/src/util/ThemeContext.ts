import { createContext, useContext } from 'react';
import { defaultTheme, type ThemeToken } from './ThemeUtil';

/** 主题上下文：切换主题时动态更新 ConfigProvider token，避免整页刷新 */
export interface ThemeContextValue {
    token: ThemeToken;
    /** 仅应用到整站（预览态，不落盘），用于主题弹窗实时换肤 */
    setToken: (token: ThemeToken) => void;
    /** 持久化到本机并应用，用于主题弹窗点「确定」 */
    saveToken: (token: ThemeToken) => void;
}

export const ThemeContext = createContext<ThemeContextValue>({
    token: defaultTheme,
    setToken: () => undefined,
    saveToken: () => undefined,
});

/** 供「主题配色」弹窗调用：预览 / 保存即时生效 */
export const useThemeSetter = () => useContext(ThemeContext);
