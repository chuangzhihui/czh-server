import { describe, expect, it } from 'vitest';
import Helper from '../Helper';

/** Helper 纯函数契约测试：表格序号、数字过滤、文件大小格式化 */
describe('Helper —— 通用工具方法', () => {
    describe('getNum 表格序号', () => {
        it('正序（asc）：按分页偏移累加', () => {
            expect(Helper.getNum(0, 100, 10, 1, 'asc')).toBe('01');
            expect(Helper.getNum(4, 100, 10, 2, 'asc')).toBe('15');
        });

        it('倒序（desc）：从总数往回算', () => {
            // 第 1 页第 1 行：100 - 0 - (1-1) = 100
            expect(Helper.getNum(0, 100, 10, 1, 'desc')).toBe('100');
            // 第 2 页第 1 行：100 - 10 - (1-1) = 90
            expect(Helper.getNum(0, 100, 10, 2, 'desc')).toBe('90');
        });

        it('序号小于 10 时补零', () => {
            expect(Helper.getNum(0, 5, 10, 1, 'asc')).toBe('01');
            expect(Helper.getNum(8, 20, 10, 1, 'asc')).toBe('09');
        });
    });

    describe('getNums 仅保留数字', () => {
        it('过滤非数字字符', () => {
            expect(Helper.getNums('a1b2c3')).toBe('123');
            expect(Helper.getNums('138-0013-8000')).toBe('13800138000');
        });
    });

    describe('getFileSize 文件大小格式化', () => {
        it('小于 1M 显示为 K', () => {
            expect(Helper.getFileSize(2048)).toBe('2.00K');
        });

        it('小于 1G 显示为 M', () => {
            expect(Helper.getFileSize(5 * 1024 * 1024)).toBe('5.00M');
        });

        it('大于 1G 显示为 G', () => {
            expect(Helper.getFileSize(2 * 1024 * 1024 * 1024)).toBe('2.00G');
        });
    });

    describe('regCheck 正则校验', () => {
        it('命中返回 true，未命中返回 false', () => {
            expect(Helper.regCheck('^[0-9]+$', '123')).toBe(true);
            expect(Helper.regCheck('^[0-9]+$', 'abc')).toBe(false);
        });

        it('空值直接返回 false', () => {
            expect(Helper.regCheck('^[0-9]+$', '')).toBe(false);
        });
    });

    describe('getRandomString 随机串', () => {
        it('长度符合预期且只含字母数字', () => {
            const str = Helper.getRandomString(12);
            expect(str).toHaveLength(12);
            expect(str).toMatch(/^[A-Za-z0-9]+$/);
        });
    });
});
