import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { TextDecoder, TextEncoder } from 'node:util';
import { afterEach } from 'vitest';

// jsdom 环境不保证暴露 TextEncoder/TextDecoder，而 Response / fetch 相关链路依赖它们
if (typeof globalThis.TextEncoder === 'undefined') {
    Object.assign(globalThis, { TextEncoder, TextDecoder });
}

afterEach(() => {
    cleanup();
});
