import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import CZHTitle from '../index';

/**
 * CZHTitle 组件冒烟测试。
 * 选它的原因：它是依赖最少的通用组件（只用 antd theme.useToken + 一个 title prop），
 * 不需要 Router / ConfigProvider / 后端数据，适合作为组件层测试的第一个落点。
 */
describe('CZHTitle 组件', () => {
    it('渲染传入的标题文案', () => {
        render(<CZHTitle title="基础配置" />);
        expect(screen.getByText('基础配置')).toBeInTheDocument();
    });

    it('标题使用 h2 语义标签', () => {
        render(<CZHTitle title="角色管理" />);
        const heading = screen.getByText('角色管理');
        expect(heading.tagName.toLowerCase()).toBe('h2');
    });

    it('不同实例之间互不串数据', () => {
        const { unmount } = render(<CZHTitle title="第一个" />);
        expect(screen.getByText('第一个')).toBeInTheDocument();
        unmount();

        render(<CZHTitle title="第二个" />);
        expect(screen.queryByText('第一个')).not.toBeInTheDocument();
        expect(screen.getByText('第二个')).toBeInTheDocument();
    });
});
