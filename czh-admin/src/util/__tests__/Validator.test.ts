import { describe, expect, it } from 'vitest';
import { checkEmail, checkRegExp, isCrontab, isPositiveInteger, phoneValidate } from '../Validator';

/**
 * Validator 纯函数契约测试。
 * 这些校验规则是表单层的唯一防线，规则一旦被改弱没有任何编译期提示，故用单测锁定行为。
 */
describe('Validator —— 表单校验规则', () => {
    describe('phoneValidate 手机号', () => {
        it('合法手机号通过', async () => {
            await expect(phoneValidate(null, '13800138000')).resolves.toBeUndefined();
        });

        it.each(['12345678901', '1380013800', '138001380000', 'abcdefghijk', ''])(
            '非法手机号 %s 被拒绝',
            async (value) => {
                await expect(phoneValidate(null, value)).rejects.toThrow('请设置正确的手机号');
            }
        );
    });

    describe('checkEmail 邮箱', () => {
        it('合法邮箱通过', async () => {
            await expect(checkEmail(null, 'czh@example.com')).resolves.toBeUndefined();
        });

        it.each(['not-an-email', 'a@', '@b.com', 'a@b'])('非法邮箱 %s 被拒绝', async (value) => {
            await expect(checkEmail(null, value)).rejects.toThrow('请输入正确的邮箱');
        });
    });

    describe('isPositiveInteger 正整数', () => {
        it('正整数通过', async () => {
            await expect(isPositiveInteger(null, '12')).resolves.toBeUndefined();
        });

        it.each(['0', '-1', '1.5', 'abc'])('非正整数 %s 被拒绝', async (value) => {
            await expect(isPositiveInteger(null, value)).rejects.toThrow('请输入正整数');
        });
    });

    describe('checkRegExp 正则表达式合法性', () => {
        it('合法正则通过', async () => {
            await expect(checkRegExp(null, '^[0-9]{6,}$')).resolves.toBeUndefined();
        });

        it('非法正则（括号不闭合）被拒绝', async () => {
            await expect(checkRegExp(null, '^[0-9(')).rejects.toThrow('请输入正确的正则表达式');
        });
    });

    describe('isCrontab 定时任务规则', () => {
        it('合法 cron 通过', async () => {
            await expect(isCrontab(null, '0 0 * * * ?')).resolves.toBeUndefined();
        });

        it('非法 cron 被拒绝', async () => {
            await expect(isCrontab(null, '每天凌晨两点')).rejects.toThrow('请输入正确的定时任务规则');
        });
    });
});
