import { blue, cyan, generate, geekblue, gold, green, lime, magenta, orange, purple, red, volcano, yellow } from '@ant-design/colors';

/** 主题配置：单机持久化 + CSS 变量注入（详见《前端开发规范》11.3） */

export interface ThemeToken {
    colorPrimary: string;
    colorSuccess: string;
    colorWarning: string;
    colorError: string;
    /** 选中的官方主题英文名；custom = 自定义 seed */
    preset?: string;
}

/** 官方主题条目：中文名 / 英文名 / seed 主色 */
export interface ThemePreset {
    n: string;
    en: string;
    seed: string;
}

/**
 * 官方 12 套主题（Ant Design 基础色板：12 主色 × 10 衍生色 = 120 色）
 * 官方建议取第 6 级作为主色，即色板 index 5 —— 见《规范》11.1
 */
export const THEMES: ThemePreset[] = [
    { n: '薄暮', en: 'red', seed: '#F5222D' },
    { n: '火山', en: 'volcano', seed: '#FA541C' },
    { n: '日暮', en: 'orange', seed: '#FA8C16' },
    { n: '金盏花', en: 'gold', seed: '#FAAD14' },
    { n: '柠檬黄', en: 'yellow', seed: '#FADB14' },
    { n: '青葱', en: 'lime', seed: '#A0D911' },
    { n: '极光绿', en: 'green', seed: '#52C41A' },
    { n: '明青', en: 'cyan', seed: '#13C2C2' },
    { n: '拂晓蓝', en: 'blue', seed: '#1677FF' },
    { n: '极客蓝', en: 'geekblue', seed: '#2F54EB' },
    { n: '酱紫', en: 'purple', seed: '#722ED1' },
    { n: '法式洋红', en: 'magenta', seed: '#EB2F96' },
];

/** 官方功能色（系统级；信息色 info 跟随品牌主色，由算法派生） */
export const FUNCTION_COLOR = { success: '#52C41A', warning: '#FAAD14', error: '#FF4D4F' };

/** 官方三级阴影：一级卡片 / 二级下拉浮层 / 三级弹窗 */
export const SHADOWS = [
    '0 1px 2px 0 rgba(0,0,0,.03), 0 1px 6px -1px rgba(0,0,0,.02), 0 2px 4px 0 rgba(0,0,0,.02)',
    '0 6px 16px 0 rgba(0,0,0,.08), 0 3px 6px -4px rgba(0,0,0,.12), 0 9px 28px 8px rgba(0,0,0,.05)',
    '0 6px 16px 0 rgba(0,0,0,.08), 0 3px 6px -4px rgba(0,0,0,.12), 0 9px 28px 8px rgba(0,0,0,.05)',
];

/** 字阶：一个系统内控制在 3–5 种 */
export const FONT_STEPS = [38, 30, 24, 20, 16, 14, 12];

/** 间距模度：8 的倍数 */
export const SPACE_STEPS = [4, 8, 12, 16, 20, 24, 32, 48];

const PALETTES: Record<string, string[]> = {
    red, volcano, orange, gold, yellow, lime, green, cyan, blue, geekblue, purple, magenta,
};

/** localStorage 键名，固定勿改 */
export const THEME_KEY = 'themeConfig_honghu';

/** 默认主题（线上现用品牌绿，不在官方 12 色内，走自定义 seed） */
export const defaultTheme: ThemeToken = {
    colorPrimary: '#009b6b',
    colorSuccess: '#0081ff',
    colorWarning: '#ffb800',
    colorError: '#FF4B24',
    preset: 'custom',
};

/**
 * seed -> 10 级色板
 * index 0 = 1 级（最浅）… index 5 = 6 级（主色）… index 9 = 10 级（最深）
 */
export function palette(seed: string): string[] {
    const hit = THEMES.find(item => item.seed === seed.toUpperCase());
    return (hit && PALETTES[hit.en]) || generate(seed);
}

/** 由 seed 生成完整 token：功能色取官方系统色，避免调出违反色板规律的组合 */
export function tokenOf(seed: string, preset?: string): ThemeToken {
    return {
        colorPrimary: seed,
        colorSuccess: FUNCTION_COLOR.success,
        colorWarning: FUNCTION_COLOR.warning,
        colorError: FUNCTION_COLOR.error,
        preset: preset || 'custom',
    };
}

/** 反查当前 token 命中的官方主题英文名（未命中返回 custom） */
export function presetOf(token: ThemeToken): string {
    const upper = (token.colorPrimary || '').toUpperCase();
    return THEMES.find(item => item.seed === upper)?.en || 'custom';
}

/** 读取当前主题，缺省时兜底默认值 */
export function getTheme(): ThemeToken {
    try {
        const raw = localStorage.getItem(THEME_KEY);
        if (!raw) return defaultTheme;
        const parsed = { ...defaultTheme, ...JSON.parse(raw) } as ThemeToken;
        return { ...parsed, preset: presetOf(parsed) };
    } catch {
        return defaultTheme;
    }
}

/** 主题变化事件名：换肤（预览 / 保存）后广播，通知所有独立 root 的弹窗实时跟随 */
export const THEME_CHANGE_EVENT = 'czh:theme-change';

/** antd ConfigProvider 接受的 token 形状：preset 是业务字段（自定义主题标识），不是 antd 设计令牌 */
export type AntdThemeToken = Omit<ThemeToken, 'preset'>;

/**
 * 运行期当前生效的主题（含「预览但未落盘」的临时值）
 * 由 applyCssVars 统一维护，确保它与注入到根节点的 CSS 变量始终同源
 */
let activeTheme: ThemeToken = getTheme();

/**
 * 读取运行期当前生效的主题
 * 供脱离主 React 树的独立 root（如 FormModal.open）注入 ConfigProvider，
 * 使弹窗内的 antd 组件色与列表页保持一致
 */
export function getActiveTheme(): ThemeToken {
    return activeTheme;
}

/** ThemeToken -> antd ConfigProvider token，剔掉非 antd 令牌的业务字段 preset */
export function toAntdToken(token: ThemeToken): AntdThemeToken {
    return {
        colorPrimary: token.colorPrimary,
        colorSuccess: token.colorSuccess,
        colorWarning: token.colorWarning,
        colorError: token.colorError,
    };
}

/** 广播主题变化：独立 root 的弹窗监听后更新自身 ConfigProvider，实现免刷新实时换肤 */
export function emitThemeChange(token: ThemeToken): void {
    window.dispatchEvent(new CustomEvent<ThemeToken>(THEME_CHANGE_EVENT, { detail: token }));
}

/** 持久化主题到本地 */
export function saveTheme(token: ThemeToken): void {
    localStorage.setItem(THEME_KEY, JSON.stringify(token));
}

/** #rrggbb -> #rrggbbaa；非 6 位（已带 alpha）原样返回 */
function withAlpha(hex: string, alpha: string): string {
    return /^#[0-9a-fA-F]{6}$/.test(hex) ? hex + alpha : hex;
}

/**
 * 把主题色写成 CSS 变量注入到根节点
 * soft = 主色 8% 底（选中态浅底）；border = 主色 40%（浅描边）
 * SCSS 统一取 var()，禁止再写死 #009b6b / #333 这类硬编码色
 */
export function applyCssVars(token: ThemeToken): void {
    // 同步运行期主题：主题弹窗「预览但未保存」的值也要能被独立 root 读到
    activeTheme = token;
    const el = document.documentElement;
    el.style.setProperty('--main-colorPrimary', token.colorPrimary);
    el.style.setProperty('--main-colorSuccess', token.colorSuccess);
    el.style.setProperty('--main-colorWarning', token.colorWarning);
    el.style.setProperty('--main-colorError', token.colorError);
    el.style.setProperty('--main-colorPrimary-soft', withAlpha(token.colorPrimary, '14'));
    el.style.setProperty('--main-colorPrimary-border', withAlpha(token.colorPrimary, '66'));
    // 广播给独立 root 的弹窗，使其与整站同步换肤
    emitThemeChange(token);
}
