import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { message } from 'antd';
import { DEFAULT_TIMEOUT, RequestError, httpGet, httpPost } from '../request';

/**
 * FE-01 回归测试：请求层的 settle 契约。
 *
 * 缺陷原貌：`new Promise((resolve) => {...})` 只声明了 resolve，
 * catch 分支既不 resolve 也不 reject —— 任何进 catch 的路径都会让 Promise 永久挂起
 * （页面 loading 不结束、Modal.confirm 的 onOk 卡死）。
 *
 * 本文件的核心断言：**所有异常路径都必须 settle（reject）**。
 */

/** 构造一个最小可用的 Response 对象 */
const jsonResponse = (body: unknown): Response =>
    new Response(JSON.stringify(body), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
    });

/** 构造网关错误页：HTTP 502 + HTML 正文 */
const htmlResponse = (): Response =>
    new Response('<html><head><title>502 Bad Gateway</title></head><body>nginx</body></html>', {
        status: 502,
        headers: { 'Content-Type': 'text/html' },
    });

describe('request.ts —— FE-01 Promise settle 契约', () => {
    beforeEach(() => {
        vi.spyOn(message, 'error').mockImplementation((() => undefined) as never);
    });

    afterEach(() => {
        vi.unstubAllGlobals();
        vi.restoreAllMocks();
        vi.useRealTimers();
    });

    it('正常 JSON 响应：resolve 并保留业务 code', async () => {
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ code: 200, msg: 'ok', data: { a: 1 } })));

        const res = await httpGet<{ a: number }>('/admin/admin/adminList');
        expect(res.code).toBe(200);
        expect(res.data.a).toBe(1);
    });

    it('【FE-01 主体】fetch 网络异常（reject）：Promise 必须 reject 而不是永久挂起', async () => {
        vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')));

        await expect(httpPost('/admin/admin/addAdmin', {})).rejects.toBeInstanceOf(RequestError);
    });

    it('【FE-01 主体】超时：AbortController 触发后必须 reject（kind=timeout）', async () => {
        vi.useFakeTimers();
        // 模拟一个永不返回的请求，只有 abort 才能让它结束
        vi.stubGlobal(
            'fetch',
            vi.fn().mockImplementation((_url: string, options: RequestInit) =>
                new Promise((_resolve, reject) => {
                    options.signal?.addEventListener('abort', () => {
                        reject(new DOMException('The operation was aborted.', 'AbortError'));
                    });
                })
            )
        );

        // 先挂上 catch 再推进时间，避免"rejection 被异步处理"的告警
        const settled = httpGet('/admin/admin/adminList', {}, DEFAULT_TIMEOUT).catch((e: unknown) => e);
        await vi.advanceTimersByTimeAsync(DEFAULT_TIMEOUT + 10);

        const error = await settled;
        expect(error).toBeInstanceOf(RequestError);
        expect((error as RequestError).kind).toBe('timeout');
    });

    it('【FE-01 配套】非 2xx 但返回合法 JSON：不再 throw，按响应体 code 交给调用方', async () => {
        // HTTP 状态码层恒 200 是团队约定；即便上游返回 502，只要 body 是 JSON 就应正常解析
        vi.stubGlobal(
            'fetch',
            vi.fn().mockResolvedValue(
                new Response(JSON.stringify({ code: 500, msg: '服务异常,traceId=abc123', data: null }), {
                    status: 502,
                    headers: { 'Content-Type': 'application/json' },
                })
            )
        );

        const res = await httpGet('/admin/admin/adminList');
        expect(res.code).toBe(500);
        expect(res.msg).toContain('traceId');
    });

    it('【FE-01 配套】网关返回 HTML：res.json() 解析失败必须被守卫，不能只是把挂起面平移到「非 JSON」', async () => {
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue(htmlResponse()));

        const error = await httpGet('/admin/admin/adminList').catch((e: unknown) => e);
        expect(error).toBeInstanceOf(RequestError);
        expect((error as RequestError).kind).toBe('parse');
    });

    it('空响应体：同样必须 settle，不能挂起', async () => {
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('', { status: 200 })));

        await expect(httpGet('/admin/admin/adminList')).rejects.toBeInstanceOf(RequestError);
    });

    it('403 无权限：code 归零，调用方按失败处理', async () => {
        vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ code: 403, msg: '无权限!', data: null })));

        const res = await httpGet('/admin/admin/adminList');
        expect(res.code).toBe(0);
    });

    it('requestBlob：网络异常时 reject 为 RequestError(kind=network)，与 httpRequest 错误模型统一（B-04）', async () => {
        const { httpBlob } = await import('../request');
        vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')));

        const error = await httpBlob('/admin/backup/downLoad', {}).catch((e: unknown) => e);
        expect(error).toBeInstanceOf(RequestError);
        expect((error as RequestError).kind).toBe('network');
    });
});
