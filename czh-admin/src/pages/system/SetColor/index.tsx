import React, { forwardRef, useEffect, useImperativeHandle, useMemo, useRef, useState } from 'react';
import {
    Button,
    Checkbox,
    ColorPicker,
    ConfigProvider,
    Input,
    Select,
    Switch,
    Table,
    Tag,
    theme
} from 'antd';
import { MenuFoldOutlined } from '@ant-design/icons';
import type { Color } from 'antd/es/color-picker';
import type { CommonFormProps } from '../../../component/CZHModal/FormModal';
import { useThemeSetter } from '../../../util/ThemeContext';
import {
    defaultTheme,
    FONT_STEPS,
    FUNCTION_COLOR,
    getTheme,
    palette,
    presetOf,
    SHADOWS,
    SPACE_STEPS,
    THEMES,
    tokenOf,
    type ThemeToken,
} from '../../../util/ThemeUtil';
import './index.scss';

interface SetColorProps extends CommonFormProps {
    /** 系统名，用于预览区顶栏 */
    systemName?: string;
    /**
     * 保存成功回调：由调用方（主应用）负责提示。
     * 本组件内的弹窗挂在 FormModal.open 的独立 root 上，关闭后 root 会 unmount，
     * 挂在弹窗 App holder 上的 toast 会随之消失，因此提示必须交给主应用的 App 承载。
     */
    onOk?: () => void;
}

/** 预览区迷你表格数据 */
const PREVIEW_ROWS = [
    { key: '1', id: '1', name: '管理员列表', status: '正常' },
    { key: '2', id: '2', name: '角色列表', status: '冻结' },
];

/** 实时预览面板：用 antd 真组件渲染迷你后台，随外层 ConfigProvider 的 draft token 实时变化 */
const PreviewPanel = ({ systemName }: { systemName: string }) => {
    const { token } = theme.useToken();

    const previewBtn = (text: string, type: 'primary' | 'default' | 'dashed' | 'link', danger = false) => (
        <Button size='small' type={type} danger={danger}>{text}</Button>
    );

    return (
        <div className='previewFrame'>
            <div className='previewFrame__top' style={{ background: token.colorPrimary }}>
                <MenuFoldOutlined />
                <span className='previewFrame__name'>{systemName}</span>
            </div>
            <div className='previewFrame__body'>
                <div className='previewMenu'>
                    {['系统设置', '管理员列表', '角色列表', '参数配置'].map((x, i) => (
                        <div className={`previewMenu__item${i === 1 ? ' on' : ''}`} key={x}>
                            <i className='previewMenu__dot' />
                            {x}
                        </div>
                    ))}
                </div>
                <div className='previewMain' style={{ background: token.colorBgLayout }}>
                    <div className='previewMain__row'>
                        {previewBtn('主按钮', 'primary')}
                        {previewBtn('次按钮', 'default')}
                        {previewBtn('虚线按钮', 'dashed')}
                        {previewBtn('文字按钮', 'link')}
                        {previewBtn('危险按钮', 'default', true)}
                    </div>
                    <div className='previewMain__row'>
                        <Tag color='success'>成功 success</Tag>
                        <Tag color='warning'>警告 warning</Tag>
                        <Tag color='error'>错误 error</Tag>
                        <Tag color={token.colorPrimary}>信息 info</Tag>
                    </div>
                    <div className='previewMain__row'>
                        <Input size='small' placeholder='输入框' className='previewMain__ipt' />
                        <Select size='small' placeholder='选择器' className='previewMain__sel' />
                        <Checkbox defaultChecked />
                        <Switch defaultChecked size='small' />
                    </div>
                    <Table
                        size='small'
                        pagination={false}
                        dataSource={PREVIEW_ROWS}
                        scroll={{ x: 'max-content' }}
                        columns={[
                            { title: 'ID', dataIndex: 'id', width: 48 },
                            { title: '名称', dataIndex: 'name' },
                            {
                                title: '状态',
                                dataIndex: 'status',
                                render: (v: string) => (
                                    <span style={{ color: v === '正常' ? token.colorSuccess : token.colorError }}>
                                        {v}
                                    </span>
                                ),
                            },
                        ]}
                    />
                </div>
            </div>
        </div>
    );
};

const Index = (props: SetColorProps, ref: React.Ref<unknown>) => {

    const { setToken, saveToken } = useThemeSetter();

    /** draft = 当前预览态（点卡片即整站生效但未落盘）；entered = 进入弹窗前的主题，取消时还原 */
    const [draft, setDraft] = useState<ThemeToken>(() => getTheme());
    const [customSeed, setCustomSeed] = useState<string>('');
    const entered = useRef<ThemeToken>(getTheme());

    useEffect(() => {
        const cur = getTheme();
        entered.current = cur;
        setDraft(cur);
        if (presetOf(cur) === 'custom') setCustomSeed(cur.colorPrimary);
    }, []);

    /** 当前预览色板：10 级，第 6 级（index 5）为主色 */
    const strip = useMemo(() => palette(draft.colorPrimary), [draft.colorPrimary]);
    const activePreset = useMemo(() => presetOf(draft), [draft]);

    /** 应用主题到整站（不落盘） */
    const apply = (next: ThemeToken) => {
        setDraft(next);
        setToken(next);
    };

    /** 选官方主题：主色 = seed，功能色取官方系统色 */
    const pickPreset = (seed: string, en: string) => apply(tokenOf(seed, en));

    /** 自定义 seed：用 @ant-design/colors 生成色板，保留线上品牌色 */
    const pickCustom = (seed: string) => {
        setCustomSeed(seed);
        apply(tokenOf(seed, 'custom'));
    };

    /** 保存：落盘 -> 关闭弹窗 -> 回调主应用提示。
     *  这里不自己弹 toast：本组件的 App holder 属于弹窗的独立 root，
     *  关闭约 300ms（afterClose）后 root.unmount()，toast 会被一起带走。 */
    const onSave = () => {
        saveToken(draft);
        props.close?.(true);
        props.onOk?.();
    };

    /** 取消 / 关闭：还原进入弹窗前的主题 */
    const onCancel = () => {
        setToken(entered.current);
        setDraft(entered.current);
    };

    useImperativeHandle(ref, () => ({ form: { submit: onSave }, cancel: onCancel }));

    /** 重置回默认主题 */
    const reset = () => apply({ ...defaultTheme });

    return (
        <div className='setColor'>
            {/* ---------------- 左：实时预览 ---------------- */}
            <div className='setColor__preview'>
                <div className='setColor__title'>实时预览（组件级令牌效果）</div>

                {/* 预览区单独包一层 ConfigProvider：draft 一变，预览里的真组件立刻换肤 */}
                <ConfigProvider theme={{ token: draft }}>
                    <PreviewPanel systemName={props.systemName || '管理后台'} />
                </ConfigProvider>

                <div className='setColor__title'>
                    基础色板（{THEMES.find(t => t.en === activePreset)?.n || '自定义'} {draft.colorPrimary} · 10 级，第 6 级为主色）
                </div>
                <div className='strip'>
                    {strip.map((c, i) => (
                        <span className='strip__cell' style={{ background: c }} title={`${i + 1} 级 · ${c}`} key={c + i} />
                    ))}
                </div>
                <div className='strip__scale'>
                    {strip.map((c, i) => (
                        <span key={'n' + i}>{i + 1}</span>
                    ))}
                </div>

                <div className='setColor__tokens'>
                    <div className='tokenCard'>
                        <div className='tokenCard__t'>字阶（主字体 14 / 行高 22）</div>
                        <div className='tokenCard__steps'>
                            {FONT_STEPS.map(s => (
                                <span style={{ fontSize: s, fontWeight: s >= 20 ? 600 : 400 }} key={s}>Aa</span>
                            ))}
                        </div>
                        <div className='tokenCard__tip'>38 / 30 / 24 / 20 / 16 / 14 / 12 · 一个系统内控制在 3–5 种字阶</div>
                    </div>
                    <div className='tokenCard tokenCard--shadow'>
                        <div className='tokenCard__t'>阴影三级 &amp; 间距模度（8 倍数）</div>
                        <div className='tokenCard__shadows'>
                            {SHADOWS.map((s, i) => (
                                <span style={{ boxShadow: s }} key={i}>{i + 1} 级</span>
                            ))}
                        </div>
                        <div className='tokenCard__spaces'>
                            {SPACE_STEPS.map(v => (
                                <span style={{ width: v }} title={String(v)} key={v} />
                            ))}
                        </div>
                        <div className='tokenCard__tip'>4 / 8 / 12 / 16 / 20 / 24 / 32 / 48</div>
                    </div>
                </div>
            </div>

            {/* ---------------- 右：官方主题卡片 ---------------- */}
            <div className='setColor__picker'>
                <div className='setColor__title'>官方主题（12 套）</div>
                <div className='themeGrid'>
                    {THEMES.map(item => {
                        const on = activePreset === item.en;
                        const xp = palette(item.seed);
                        return (
                            <div
                                className={`themeCard${on ? ' on' : ''}`}
                                key={item.en}
                                title={`${item.n}（${item.en}）${item.seed}`}
                                onClick={() => pickPreset(item.seed, item.en)}
                            >
                                <div className='themeCard__strip'>
                                    {xp.map((c, i) => (
                                        <span style={{ background: c }} key={i} />
                                    ))}
                                </div>
                                <div className='themeCard__meta'>
                                    <i className='themeCard__dot' style={{ background: item.seed }} />
                                    <b>{item.n}</b>
                                    <em>{item.seed}</em>
                                    {on && <span className='themeCard__check iconfont icon-gouxuan' />}
                                </div>
                            </div>
                        );
                    })}

                    {/* 自定义 seed：保留线上品牌色（默认 #009B6B 不在官方 12 色内） */}
                    <div
                        className={`themeCard themeCard--custom${activePreset === 'custom' ? ' on' : ''}`}
                        title='自定义主色（用 @ant-design/colors 生成 10 级色板）'
                        onClick={() => customSeed && pickCustom(customSeed)}
                    >
                        <div className='themeCard__strip'>
                            {(customSeed ? palette(customSeed) : palette(defaultTheme.colorPrimary)).map((c, i) => (
                                <span style={{ background: c }} key={i} />
                            ))}
                        </div>
                        <div className='themeCard__meta'>
                            <i
                                className='themeCard__dot'
                                style={{ background: customSeed || defaultTheme.colorPrimary }}
                            />
                            <b>自定义</b>
                            <em>{customSeed || defaultTheme.colorPrimary}</em>
                            {activePreset === 'custom' && <span className='themeCard__check iconfont icon-gouxuan' />}
                        </div>
                        <div className='themeCard__picker' onClick={e => e.stopPropagation()}>
                            <ColorPicker
                                size='small'
                                value={customSeed || defaultTheme.colorPrimary}
                                onChangeComplete={(value: Color) => pickCustom(value.toHexString())}
                            />
                        </div>
                    </div>
                </div>

                <div className='themeNote'>
                    当前：<b>{THEMES.find(t => t.en === activePreset)?.n || '自定义'}</b>
                    （{activePreset}） · 主色 <code>{strip[5]}</code> · Hover <code>{strip[4]}</code> · Active{' '}
                    <code>{strip[6]}</code>
                    <br />
                    选一套即整站生效：主色 10 级色板、功能色派生、中性色明暗均由官方算法自动生成，无需逐项配置。
                    <br />
                    官方功能色：成功 <code>{FUNCTION_COLOR.success}</code> · 警告{' '}
                    <code>{FUNCTION_COLOR.warning}</code> · 错误 <code>{FUNCTION_COLOR.error}</code>
                </div>

                <div className='themeFooter'>
                    <Button onClick={reset}>重 置</Button>
                </div>
            </div>
        </div>
    );
};

export default forwardRef(Index);
