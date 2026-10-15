import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './css/main.scss'
import App from './App'
import { applyCssVars, getTheme } from './util/ThemeUtil'

const root = createRoot(document.getElementById('root') as HTMLElement);

// 主题色同步到 CSS 变量，供 SCSS 用 var(--main-colorPrimary) 取值
applyCssVars(getTheme());

root.render(
    <StrictMode>
        <App />
    </StrictMode>
);
