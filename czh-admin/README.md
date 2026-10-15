# CZH 管理后台前端（czh-admin）

依据《前端开发规范（模板标准版）v2.0》与《现有后台改造清单》重构的管理后台前端。

## 技术栈

| 项 | 版本 |
| --- | --- |
| react / react-dom | 19.3.0 |
| antd | 6.6.6 |
| @ant-design/icons | 6.x（显式声明） |
| 构建 | Vite 7（root = public，输出 ../dist） |
| 语言 | TypeScript 5（strict） |
| 样式 | Sass：全局唯一入口 `src/css/main.scss` + 页面/组件就近同名 `.scss` |
| 路由 | react-router-dom 7（HashRouter，路由表在 `src/pages/Route.tsx`） |
| 请求 | 原生 fetch 封装（`src/util/request.ts`） |

## 脚本

```bash
yarn install        # 安装依赖
yarn dev            # 本地开发
yarn typecheck      # tsc --noEmit
yarn build          # tsc --noEmit + vite build
yarn lint           # ESLint（覆盖 ts/tsx）
yarn preview        # 预览构建产物
```

## 目录速览

```
src/
├── api/<模块>/{XxxApi.ts, XxxDto.ts, XxxVo.ts}   # 接口三件套（无 models/ 目录）
├── pages/          # 页面 + 同名 .scss
├── component/      # CZH* 通用组件 + 就近 .scss
├── css/main.scss   # 全局唯一样式入口（reset/骨架/响应式/冻结通用类/antd 覆盖）
├── css/_variables.scss
└── util/           # request.ts / Helper.ts / Validator.ts / ThemeUtil.ts / ThemeContext.ts
```

## 关键约定（红线摘要）

1. 接口成功判断只认 `res.code === 200`；无权限只认 `403`；字段一律小驼峰。
2. 页面组件必须 `forwardRef` 并暴露 `refresh()`；新页面注册进 `pages/Route.tsx` 的 `Components`（key = 后台菜单 path）。
3. 表单弹窗统一 `FormModal.open`；危险操作 `modal.confirm` 二次确认。
4. 禁止写死颜色（用 `theme.useToken()` / `var(--main-colorPrimary)`）；禁止在全局堆业务样式。
5. Flex 必须带防压缩三件套（`min-width:0` / `min-height:0` / `flex-shrink:0`）；新增页面需过 6 档视口（1920/1440/1280/992/768/375）。
6. **不引入前端 Mock**：本工程全栈同源开发，接口直连本地后端（`VITE_API_URL`），没有"后端未就绪"的场景，MSW 与 `src/mocks/` 已整体移除。单测需要拦截请求时用 `vi.stubGlobal('fetch', ...)` 就地打桩（见 `src/util/__tests__/request.test.ts`）。

完整规范见 `../md/前端开发规范.md`。
