import { message } from 'antd';
import 'isomorphic-fetch'

/** 统一响应结构 */
export interface HttpResponse<T> {
    /**
     * 业务码（前端唯一判据）：
     * 200 成功 / 400 参数或业务规则拒绝 / 401 登录失效 / 403 无权限 / 404 请求地址不存在 / 500 服务异常。
     * ⚠️ 分支判断只看本字段，不要看 HTTP 状态码（res.ok）——全项目 HTTP 状态码层恒 200。
     */
    code: number;
    /** 提示信息 */
    msg: string;
    /** 数据包 */
    data: T;
}

type Method = 'POST' | 'GET';

/** 请求失败的分类，便于调用方区分处理（重试 / 提示 / 忽略） */
export type RequestErrorKind = 'network' | 'timeout' | 'abort' | 'parse';

/**
 * 请求层统一错误。
 * <p>
 * FE-01 的根因是「进 catch 后既不 resolve 也不 reject」，Promise 永不 settle；
 * 现在所有异常路径都会 reject 出一个本类型，调用方可以用 catch / try-catch 正常收口。
 */
export class RequestError extends Error {
    readonly kind: RequestErrorKind;

    constructor(kind: RequestErrorKind, msg: string) {
        super(msg);
        this.name = 'RequestError';
        this.kind = kind;
    }
}

/** 默认请求超时时间（毫秒）：弱网下没有超时保护会让 loading 永久转圈 */
export const DEFAULT_TIMEOUT = 15000;

const API_URL = import.meta.env.VITE_API_URL;

const commonHeaders = () => ({
    'Content-Type': 'application/json;',
    Accept: 'application/json',
    token: localStorage.getItem('czhToken') || '',
});

/**
 * 统一码值处理
 * 401：清 token 后跳登录页；403：提示无权限并把 code 置 0（业务层按失败处理）
 * 400/404：本轮后端尚未引入业务 400（依赖 BE-09），且 404 的 msg 已由后端写好
 *          （ErrController 现在会返回"请求地址不存在"），统一走调用方的 res.code !== 200 分支，
 *          此处不再重复弹提示，避免与调用方的 message.error(res.msg) 双重 Toast。
 */
const handleCode = <T,>(res: HttpResponse<T>): HttpResponse<T> => {
    if (res.code === 401) {
        localStorage.removeItem('czhToken');
        message.error(res.msg, 1.2, () => {
            window.location.href = '#/';
        });
        res.code = 0;
        return res;
    }
    if (res.code === 403) {
        message.error(res.msg);
        res.code = 0;
        return res;
    }
    return res;
};

/** 是否为「超时导致的 abort」与「调用方主动取消」分别打标记 */
const isAbortError = (error: unknown): boolean =>
    error instanceof DOMException && error.name === 'AbortError';

export function httpPost<T>(url: string, data: unknown, timeout?: number): Promise<HttpResponse<T>> {
    return httpRequest<T>(url, data, 'POST', timeout);
}

export function httpGet<T>(url: string, data: Record<string, unknown> = {}, timeout?: number): Promise<HttpResponse<T>> {
    return httpRequest<T>(url, data, 'GET', timeout);
}

export function httpBlob(url: string, data: unknown): Promise<HttpResponse<unknown> | Blob> {
    return requestBlob(url, data, 'POST');
}

function httpRequest<T>(url: string, data: unknown, method: Method, timeout: number = DEFAULT_TIMEOUT): Promise<HttpResponse<T>> {
    let finalUrl = API_URL + url;
    if (method === 'GET' && data) {
        const query = new URLSearchParams(data as Record<string, string>);
        if (query.toString()) {
            finalUrl += '?' + query.toString();
        }
    }
    // 超时与取消：AbortController。没有它，弱网下 Promise 会一直挂在 pending。
    const controller = typeof AbortController === 'undefined' ? null : new AbortController();
    let timedOut = false;
    const timer = controller && timeout > 0
        ? setTimeout(() => {
            timedOut = true;
            controller.abort();
        }, timeout)
        : null;

    // ② 关键：必须声明 reject（原实现只声明了 resolve，导致 catch 里无法收口 → Promise 永不 settle）
    return new Promise<HttpResponse<T>>((resolve, reject) => {
        const fail = (kind: RequestErrorKind, text: string) => {
            message.error(text);
            reject(new RequestError(kind, text));
        };
        const clear = () => {
            if (timer) {
                clearTimeout(timer);
            }
        };

        fetch(finalUrl, {
            method,
            headers: commonHeaders(),
            body: method === 'GET' ? undefined : JSON.stringify(data),
            signal: controller ? controller.signal : undefined,
        })
            // 统一先取文本再解析：这样"非 JSON 响应"可以被显式守卫，而不是让 res.json() 抛错后掉进 catch
            .then((res) => res.text().then((text) => text))
            .then((text: string) => {
                // 不再依赖 res.ok：HTTP 状态码层恒 200，业务结果只看响应体 code。
                // 但必须同时守卫解析失败 —— 网关 502/504 返回的是 HTML 错误页，
                // 若不做这层守卫，挂起面只是从"非 2xx"平移到"非 JSON"，等于没修。
                if (!text || !text.trim()) {
                    throw new RequestError('parse', '响应体为空');
                }
                try {
                    resolve(handleCode(JSON.parse(text) as HttpResponse<T>));
                } catch {
                    throw new RequestError('parse', '响应不是合法 JSON，网关可能返回了 HTML 错误页');
                }
            })
            .catch((error: unknown) => {
                if (error instanceof RequestError) {
                    // 解析失败/空响应：属于本层已知错误
                    fail(error.kind, '请求网络失败!');
                    return;
                }
                if (timedOut) {
                    fail('timeout', '请求超时,请稍后重试!');
                    return;
                }
                if (isAbortError(error)) {
                    // 调用方主动取消：不弹错误提示，但仍必须 settle
                    reject(new RequestError('abort', '请求已取消'));
                    return;
                }
                fail('network', '请求网络失败!');
            })
            .finally(clear);
    });
}

/**
 * 二进制请求（导出/下载）。
 * B-04：补齐与 httpRequest 一致的超时与取消能力——原实现没有 AbortController，
 * 弱网挂起时 Promise 同样会永久 pending，FE-01 的「超时必 settle」契约在导出链路不生效。
 * 同时把 !res.ok 的 throw 换成 RequestError：网关 502/504 的 HTML 错误页
 * 不会再以 blob 形式 resolve 给调用方（那会把错误页当文件下载）。
 */
const requestBlob = (url: string, data: unknown, method: Method, timeout: number = DEFAULT_TIMEOUT) => {
    const finalUrl = API_URL + url;
    const controller = typeof AbortController === 'undefined' ? null : new AbortController();
    const timer = controller && timeout > 0
        ? setTimeout(() => controller.abort(), timeout)
        : null;
    return new Promise<HttpResponse<unknown> | Blob>((resolve, reject) => {
        const fail = (kind: RequestErrorKind, text: string) => {
            message.error(text);
            reject(new RequestError(kind, text));
        };
        fetch(finalUrl, {
            method,
            headers: commonHeaders(),
            body: JSON.stringify(data),
            signal: controller ? controller.signal : undefined,
        })
            .then((res) => {
                if (!res.ok) {
                    // 二进制链路没有业务码可判：非 2xx（网关 502/504 等）直接拒绝，
                    // 不落进 blob 分支（否则错误页会被当成下载内容保存）。
                    throw new RequestError('network', '接口请求异常');
                }
                const contentType = res.headers.get('Content-Type') || '';
                if (contentType.includes('application/json')) {
                    return res.json().then((res2: HttpResponse<unknown>) => {
                        resolve(handleCode(res2));
                    });
                }
                return res.blob().then((blob: Blob) => resolve(blob));
            })
            .catch((error: unknown) => {
                if (error instanceof RequestError) {
                    fail(error.kind, '接口请求异常');
                    return;
                }
                if (isAbortError(error)) {
                    // 调用方主动取消：不弹错误提示，但仍必须 settle
                    reject(new RequestError('abort', '请求已取消'));
                    return;
                }
                fail('network', '请求网络失败!');
            })
            .finally(() => {
                if (timer) {
                    clearTimeout(timer);
                }
            });
    });
};
