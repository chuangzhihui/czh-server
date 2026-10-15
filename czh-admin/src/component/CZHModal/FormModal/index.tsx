import { Button, Modal } from 'antd';
import type { ButtonProps } from 'antd';
import React, { cloneElement, ReactElement, useEffect, useRef, useState } from 'react';
import { createRoot } from 'react-dom/client';
import Title from '../../CZHTitle';
import ModalRoot from '../ModalRoot';
import '../index.scss';

/** 子组件可暴露的能力：submit 供「确定」调用，cancel 供「取消 / 关闭」回滚未保存的改动 */
export interface FormModalChildHandle {
    form?: { submit: () => void };
    cancel?: () => void;
}

/** FormModal 通过 cloneElement 注入给子组件的 props（子组件不要自己实现确定/取消按钮） */
export interface CommonFormProps {
    loading?: (e: boolean) => void;
    /** silent = true：提交成功后关闭，不触发子组件 cancel（避免覆盖刚保存的数据） */
    close?: (silent?: boolean) => void;
}

/** 透传给 antd Button 的 props：children / loading / onClick 由 FormModal 接管，外部不要传 */
export type FormModalButtonProps = Omit<ButtonProps, 'children' | 'loading' | 'onClick'>;

/** 动作上下文：透给 onOk / onCancel / footer / footerButtons，让外部自行提交或关闭 */
export interface FormModalActionContext {
    /** 关闭弹窗；silent = true 不触发子组件 cancel（提交成功后关闭用，避免回滚刚保存的数据） */
    close: (silent?: boolean) => void;
    /** 触发子组件表单提交（等价于点默认「确定」的核心动作） */
    submit: () => void;
    /** 设置默认「确定」按钮的 loading */
    setLoading: (e: boolean) => void;
    /** 子组件实例（暴露 form / cancel），需要直接操作表单时用 */
    childRef: React.RefObject<FormModalChildHandle | null>;
}

/**
 * 按钮回调返回值：
 * - 不返回 / 返回 true：放行，继续执行后续动作（提交或关闭）
 * - 返回 false（或 Promise resolve(false)）：阻止后续动作，弹窗保持打开
 * - 返回 Promise：期间自动接管该按钮的 loading
 */
export type FormModalAction = void | boolean | Promise<void | boolean>;

/** 自定义底部按钮：继承 antd ButtonProps（type / danger / icon / disabled / className … 均可直接用） */
export interface FormModalButton extends FormModalButtonProps {
    /** 唯一标识，同时作为按钮 key */
    key: string;
    /** 按钮文案 */
    text: React.ReactNode;
    /** 外部控制 loading（内部异步回调执行期间也会自动 loading） */
    loading?: boolean;
    /** 是否显示，支持函数动态判断 */
    visible?: boolean | (() => boolean);
    /** 点击回调：返回 false 阻止后续动作；返回 Promise 时自动接管 loading */
    onClick?: (ctx: FormModalActionContext) => FormModalAction;
    /** 点击回调放行后是否关闭弹窗，默认 false */
    closeOnClick?: boolean;
}

export interface FormModalProps {
    /** 弹窗标题 */
    title: string;
    /** 弹窗宽度（360 / 460 / 696 / 900 / 1172） */
    width?: number;
    /** 弹窗内容 */
    children: React.ReactNode;
    open?: boolean;
    /** 是否可关闭：控制右上角 ×；为 false 时默认「取消」按钮与 Esc 一并停用（强制改密码这类弹窗） */
    closable?: boolean;
    /** 是否允许点击遮罩（弹窗外空白区域）关闭，默认跟随 closable（closable === false 时不允许） */
    maskClosable?: boolean;
    /** 是否允许按 Esc 关闭，默认跟随 closable */
    keyboard?: boolean;
    /**
     * 底部按钮区，四种用法：
     * - 不传：默认「取消 + 确定」
     * - `false` / `null`：隐藏底部按钮区
     * - 节点：完全自定义（会被放进统一的 `.customerModalFooter` 容器）
     * - 函数：拿到动作上下文自行渲染
     */
    footer?: false | null | React.ReactNode | ((ctx: FormModalActionContext) => React.ReactNode);
    /** 追加自定义按钮，渲染在「取消 / 确定」之前；要完全接管底部请用 footer */
    footerButtons?: FormModalButton[];
    /** 「确定」文案 */
    okText?: React.ReactNode;
    /** 「取消」文案 */
    cancelText?: React.ReactNode;
    /** 是否渲染「确定」，默认 true */
    showOkButton?: boolean;
    /** 是否渲染「取消」，默认跟随 closable */
    showCancelButton?: boolean;
    /** 透传给「确定」按钮（loading / onClick 由组件接管） */
    okButtonProps?: FormModalButtonProps;
    /** 透传给「取消」按钮（loading / onClick 由组件接管） */
    cancelButtonProps?: FormModalButtonProps;
    /** 外部接管「确定」按钮的 loading（与内部 loading 取或） */
    confirmLoading?: boolean;
    /** 点「确定」是否自动触发子组件 form.submit()，默认 true；要完全接管提交逻辑时置 false */
    autoSubmit?: boolean;
    /**
     * 输入框聚焦时按回车是否自动提交表单（等价点「确定」），默认 true；
     * 多行文本框 / 输入法组合中 / 底部按钮上的回车不会触发
     */
    enterToSubmit?: boolean;
    /** 点「确定」回调：先执行本回调，返回 false 则不提交 */
    onOk?: (ctx: FormModalActionContext) => FormModalAction;
    /** 点「取消 / × / 遮罩 / Esc」回调，返回 false 可阻止关闭 */
    onCancel?: (ctx: FormModalActionContext) => FormModalAction;
    /** 关闭动画结束后回调（此时可安全刷新列表、弹提示） */
    onClose?: () => void;
    /**
     * 外层包裹器：需要额外上下文的弹窗用它注入，放在主题与 App 的内层（见 Layout.openSetColor）
     * 注：主题 token 与 antd App 已由 FormModal.open 默认注入，普通弹窗不需要传 wrap
     */
    wrap?: (children: React.ReactNode) => React.ReactNode;
}

/** 命令式打开的句柄 */
export interface FormModalInstance {
    /** 直接销毁弹窗（不等关闭动画），用于外部需要立刻收回容器的场景 */
    close: () => void;
}

/** 子组件实际收到的 props：自身 props + FormModal 注入的 loading / close / ref */
type InjectedChildProps = FormModalChildHandle & CommonFormProps & { ref?: React.Ref<FormModalChildHandle> };

const isPromiseLike = (value: unknown): value is Promise<unknown> =>
    typeof (value as { then?: unknown } | null | undefined)?.then === 'function';

const FormModal = (props: FormModalProps) => {
    const {
        title = '表单弹窗',
        width = 500,
        closable = true,
        footerButtons,
        okText = '确定',
        cancelText = '取消',
        showOkButton = true,
        autoSubmit = true,
        enterToSubmit = true,
    } = props;

    /** 遮罩 / Esc / 「取消」的可关闭性默认跟随 closable，可各自单独覆盖 */
    const maskClosable = props.maskClosable ?? closable;
    const keyboard = props.keyboard ?? closable;
    const showCancelButton = props.showCancelButton ?? closable;

    const [open, setOpen] = useState<boolean>(props.open || false);
    const [loading, setLoading] = useState<boolean>(false);
    /** 各按钮异步回调执行中的 loading：key 为 'ok' / 'cancel' / 自定义按钮 key */
    const [pending, setPending] = useState<Record<string, boolean>>({});
    const childRef = useRef<FormModalChildHandle>(null);

    /** open 从 props 传入时跟随外部变化；命令式 FormModal.open 场景下恒为 true，不受影响 */
    useEffect(() => {
        if (props.open !== undefined) setOpen(props.open);
    }, [props.open]);

    const setPendingByKey = (key: string, busy: boolean) => {
        setPending(prev => (prev[key] === busy ? prev : { ...prev, [key]: busy }));
    };

    /** 取消 / 关闭时通知子组件回滚（如主题弹窗还原进入前的配色）；
     *  真正的销毁交给 Modal 的 afterClose：关闭动画走完再回调，不再用魔法数字定时 */
    const closeModal = (silent = false) => {
        if (!silent) childRef.current?.cancel?.();
        setOpen(false);
    };

    /** 动作上下文：透给 onOk / onCancel / footer / footerButtons，让外部能自行提交或关闭 */
    const ctx: FormModalActionContext = {
        close: closeModal,
        submit: () => childRef.current?.form?.submit(),
        setLoading,
        childRef,
    };

    /** 执行回调并接管 loading：返回 false（含 Promise resolve(false)）表示阻止后续动作 */
    const runAction = async (action: (() => FormModalAction) | undefined, key: string): Promise<boolean> => {
        if (!action) return true;
        setPendingByKey(key, true);
        try {
            const result = action();
            const passed = isPromiseLike(result) ? await result : result;
            return passed !== false;
        } catch (err) {
            // 业务异常应由回调自行处理（请求层已统一提示），这里只兜底，避免按钮卡在 loading
            console.error('[FormModal] 按钮回调执行异常：', err);
            return false;
        } finally {
            setPendingByKey(key, false);
        }
    };

    /** 统一入口：外部回调都带 ctx，省去调用处重复包一层 */
    const runCallback = (
        callback: ((ctx: FormModalActionContext) => FormModalAction) | undefined,
        key: string,
    ) => runAction(callback ? () => callback(ctx) : undefined, key);

    /** 「确定」：先跑 onOk，放行后再触发子组件表单提交（提交成功后由子组件调 close 关闭） */
    const handleOk = () => {
        void (async () => {
            const passed = await runCallback(props.onOk, 'ok');
            if (!passed) return;
            if (autoSubmit) childRef.current?.form?.submit();
        })();
    };

    /** 输入框聚焦时按回车自动提交（等价点「确定」）：复用 handleOk，不新写提交逻辑 */
    const handleKeyDown = (e: React.KeyboardEvent<HTMLDivElement>) => {
        if (!enterToSubmit) return;
        // 仅处理「回车」且非 shift+回车（shift+回车留给多行输入换行等场景）
        if (e.key !== 'Enter' || e.shiftKey) return;
        // 输入法组合输入中（中文/日文等选词），回车是选词而非提交
        if (e.nativeEvent.isComposing) return;

        const target = e.target as HTMLElement;
        const tagName = target.tagName;
        // 多行文本框要换行、下拉回车是选选项、按钮回车应触发按钮自身 click，均不在此处理
        if (tagName === 'TEXTAREA' || tagName === 'BUTTON' || tagName === 'SELECT') return;
        // 焦点落在底部按钮区（按钮是 type=button 的 BUTTON，一般进不到这里，加此保险）
        if (target.closest('.customerModalFooter')) return;
        // 其余仅处理单行输入框（INPUT）的回车
        if (tagName !== 'INPUT') return;

        e.preventDefault();
        handleOk();
    };

    /** 统一关闭入口：「取消」按钮 / × / 遮罩 / Esc 都走这里，先问 onCancel 是否放行 */
    const handleCancel = () => {
        void (async () => {
            const passed = await runCallback(props.onCancel, 'cancel');
            if (passed) closeModal();
        })();
    };

    const isButtonVisible = (btn: FormModalButton) =>
        typeof btn.visible === 'function' ? btn.visible() : btn.visible !== false;

    /** 自定义按钮：解构出 FormModal 自己的字段，其余原样透传给 antd Button */
    const renderExtraButton = (btn: FormModalButton) => {
        const { key, text, visible: _visible, onClick, closeOnClick, loading: btnLoading, ...buttonProps } = btn;
        return (
            <Button
                key={key}
                {...buttonProps}
                loading={btnLoading || !!pending[key]}
                onClick={() => {
                    void (async () => {
                        const passed = await runCallback(onClick, key);
                        if (!passed) return;
                        if (closeOnClick) closeModal();
                    })();
                }}
            >
                {text}
            </Button>
        );
    };

    const renderFooter = () => {
        const { footer } = props;

        // 隐藏底部按钮区
        if (footer === false || footer === null) return null;
        // 完全自定义：节点会被放进统一的底部容器，保持与默认一致的留白与分隔线
        if (typeof footer === 'function') return <div className='customerModalFooter'>{footer(ctx)}</div>;
        if (footer !== undefined) return <div className='customerModalFooter'>{footer}</div>;

        const extraButtons = (footerButtons || []).filter(isButtonVisible);
        const okButtonLoading = props.confirmLoading || loading || !!pending['ok'];

        return (
            <div className='customerModalFooter'>
                {extraButtons.map(renderExtraButton)}
                {showCancelButton && (
                    <Button
                        {...props.cancelButtonProps}
                        loading={!!pending['cancel']}
                        onClick={handleCancel}
                    >
                        {cancelText}
                    </Button>
                )}
                {showOkButton && (
                    <Button
                        type='primary'
                        {...props.okButtonProps}
                        loading={okButtonLoading}
                        onClick={handleOk}
                    >
                        {okText}
                    </Button>
                )}
            </div>
        );
    };

    const Children = cloneElement<InjectedChildProps>(props.children as ReactElement<InjectedChildProps>, {
        ref: childRef,
        loading: (e: boolean) => setLoading(e),
        close: (silent?: boolean) => closeModal(silent),
    });

    return (
        <Modal
            open={open}
            centered={true}
            // antd 6 起 maskClosable 已废弃，改用 mask.closable（语义等价）
            mask={{ closable: maskClosable }}
            closable={closable}
            keyboard={keyboard}
            destroyOnHidden={true}
            title={<Title title={title} />}
            footer={renderFooter()}
            closeIcon={<p className='iconfont icon-guanbi' />}
            wrapClassName='customerModal'
            width={width}
            onCancel={handleCancel}
            afterClose={() => props.onClose?.()}
        >
            <div className='customerModalFormWrap' onKeyDown={handleKeyDown}>
                {Children}
            </div>
        </Modal>
    );
};

/** 创建脱离当前 React 树的容器，避免弹窗被页面卸载影响 */
const createContainer = () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    return container;
};

FormModal.open = (props: FormModalProps): FormModalInstance => {
    const container = createContainer();
    const root = createRoot(container);

    const close = () => {
        root.unmount();
        container.remove();
    };

    root.render(
        <ModalRoot wrap={props.wrap}>
            <FormModal
                {...props}
                open={true}
                onClose={() => {
                    // 关闭动画被禁用时（prefers-reduced-motion / 关掉 motion）afterClose 会同步触发，
                    // 此时直接 unmount 会撞上 React「渲染中同步卸载 root」的告警，故把销毁推到微任务；
                    // props.onClose 保持同步执行，调用方语义不变
                    queueMicrotask(() => close());
                    props.onClose?.();
                }}
            />
        </ModalRoot>,
    );

    return { close };
};

export default FormModal;
