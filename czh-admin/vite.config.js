import {defineConfig, loadEnv} from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path';
import { copyFileSync, existsSync, mkdirSync, readdirSync } from 'fs';
import { fileURLToPath } from 'url';
// https://vite.dev/config/
// 递归复制目录及文件
function copyDir(src, dest) {
  // 确保目标目录存在
  if (!existsSync(dest)) {
    mkdirSync(dest, { recursive: true });
  }

  // 读取源目录内容
  const entries = readdirSync(src, { withFileTypes: true });

  for (const entry of entries) {
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);

    // 跳过index.html，因为Vite会单独处理
    if (entry.name === 'index.html') continue;

    if (entry.isDirectory()) {
      // 如果是目录，递归复制
      copyDir(srcPath, destPath);
    } else {
      // 如果是文件，直接复制
      copyFileSync(srcPath, destPath);
      console.log(`Copied: ${srcPath} -> ${destPath}`);
    }
  }
}

// 复制public目录内容到dist
function copyPublicAssets() {
  const __dirname = path.dirname(fileURLToPath(import.meta.url));
  const publicDir = path.resolve(__dirname, 'public');
  const distDir = path.resolve(__dirname, 'dist');

  copyDir(publicDir, distDir);
}
export default defineConfig(({ mode,command }) => {
  // 从项目根目录加载环境变量
  const env = loadEnv(mode, process.cwd(), 'VITE_');

  // 转换环境变量为客户端可访问的格式
  const envWithProcessPrefix = Object.entries(env).reduce(
      (prev, [key, val]) => {
        return {
          ...prev,
          [`import.meta.env.${key}`]: JSON.stringify(val),
        };
      },
      {}
  );
  return {
    plugins: [react(),
      // 在构建完成后执行复制操作
      {
        name: 'copy-public-assets',
        closeBundle() {
          if (command === 'build') {
            copyPublicAssets();
          }
        }
      }],
    base:"./",
    root:path.resolve(__dirname, 'public'),
    assetsInclude: ['assets'],
    // 配置路径别名，解决 src 目录引用问题
    resolve: {
      alias: {
        '/src': path.resolve(__dirname, 'src')
      }
    },
    // 配置构建输出目录
    build: {
      outDir: '../dist',
      // 确保资源路径正确
      assetsDir: 'assets',
      // 确保所有静态资源都被正确处理
      assetsInclude: [path.resolve(__dirname, 'public/**/*')]
    },
    // 开发服务器配置
    server: {
      // 确保根目录正确
      root: 'public',
      // 【仅本地联调用】把 /devapi 代理到本地后端，规避 417x 端口不在 CORS 白名单的问题
      proxy: {
        '/devapi': {
          target: 'http://127.0.0.1:18081',
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/devapi/, ''),
        },
      },
    },
    // preview 同样生效（dist 冒烟测试走同源代理）
    preview: {
      root: 'public',
      proxy: {
        '/devapi': {
          target: 'http://127.0.0.1:18081',
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/devapi/, ''),
        },
      },
    },
    // 直接定义环境变量
    define: envWithProcessPrefix,
    // ------------------------------------------------------------------
    // 测试配置（Vitest）
    // 选型理由：工程已是 Vite 7，Vitest 直接复用上面的 vite.config 与同一套 transform 管线；
    // 且 src/Route/index.tsx 用了 import.meta.glob（Vite 专有 API），Jest 需要额外 mock。
    // ------------------------------------------------------------------
    test: {
      // 工程 vite root 指向 public/（历史原因），测试必须以项目根目录为基准才能扫到 src
      root: __dirname,
      globals: true,
      environment: 'jsdom',
      setupFiles: [path.resolve(__dirname, 'src/test/setup.ts')],
      include: ['src/**/*.{test,spec}.{ts,tsx}'],
      // scss 不参与单测处理，避免为样式编译付出无意义的耗时
      css: false,
      coverage: {
        reporter: ['text', 'html'],
        include: ['src/util/**', 'src/component/**'],
      },
    },
  };
});
