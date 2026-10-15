import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'
import { defineConfig, globalIgnores } from 'eslint/config'

/**
 * 覆盖 ts / tsx（只配 js/jsx 等于没配，见《前端开发规范》17.2）
 */
export default defineConfig([
  // 第三方静态资源（iconfont / jquery / three）不参与检查
  globalIgnores(['dist', 'node_modules', 'public/js/**', 'public/font/**']),
  {
    files: ['**/*.{js,mjs,jsx,ts,tsx}'],
    extends: [
      js.configs.recommended,
      ...tseslint.configs.recommended,
      reactHooks.configs['recommended-latest'],
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
      parserOptions: {
        ecmaVersion: 'latest',
        ecmaFeatures: { jsx: true },
        sourceType: 'module',
      },
    },
    rules: {
      // TS 自身已做类型与作用域检查，避免与 TS 语义重复报错
      'no-undef': 'off',
      'no-redeclare': 'off',
      'no-unused-expressions': ['error', { allowShortCircuit: true, allowTernary: true, allowTaggedTemplates: true }],
      '@typescript-eslint/no-unused-expressions': ['error', { allowShortCircuit: true, allowTernary: true, allowTaggedTemplates: true }],
      'no-unused-vars': 'off',
      '@typescript-eslint/no-unused-vars': ['warn', { varsIgnorePattern: '^[A-Z_]' }],
      '@typescript-eslint/no-explicit-any': 'warn',
      'react-hooks/exhaustive-deps': 'warn',
      // 工程内既有范式：组件文件同时导出工具方法（如 customerUpload、路由基表）
      'react-refresh/only-export-components': 'warn',
    },
  },
])
