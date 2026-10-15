package com.czh.common.dto;

import com.czh.common.utils.OrderByUtil;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class PageDto {
    private int page;
    /**
     * 每页条数上限，防止传入极大值导致一次捞出全表。
     * 注：page 不设 @Min(1)，保留 page/size 为 0 时"不分页查全部"的既有行为。
     */
    @Max(value = 500, message = "每页条数不能超过500")
    private int size;
    /**
     * 排序方式：由前端传入，<b>原文拼接进 SQL</b>，因此读取时一律走白名单（BE-03）。
     * <p>
     * 不要直接读本字段，请调用 {@link #getOrderBy()} 或 {@link #buildOrderBy(String)}。
     */
    private String orderBy;//排序方式

    /**
     * 排序参数的安全出口：任何读取 orderBy 的地方拿到的都是已白名单过滤后的值。
     * <p>
     * 非法值（如 {@code asc,1=(select 1 from (select sleep(5))a)}）会被归一化为空串，
     * 即"不排序"，而不是被转义后拼进 SQL。
     *
     * @return 安全排序串，非法返回空串
     */
    public String getOrderBy() {
        return OrderByUtil.sanitize(orderBy);
    }

    /**
     * 原始入参，仅供日志/审计使用。<b>禁止</b>用它拼 SQL。
     *
     * @return 未经过滤的原始排序串
     */
    public String getOrderByRaw() {
        return orderBy == null ? OrderByUtil.EMPTY_ORDER_BY : orderBy;
    }

    /**
     * 拼接「默认字段名 + 前端传入的排序方向」。
     * <p>
     * 约定：前端只传方向（asc/desc），字段名由后端按各列表的主排序列固定给出，
     * 这样前端无论如何都无法决定参与排序的列名。
     *
     * @param defaultField 该列表的默认（唯一允许的）排序列名
     * @return 安全排序串；前端未传或传入非法值时返回空串（= 不排序）
     */
    public String buildOrderBy(String defaultField) {
        String safe = getOrderBy();
        if (safe.isEmpty()) {
            return OrderByUtil.EMPTY_ORDER_BY;
        }
        // 已自带字段名且通过白名单（如 "adminId desc"）：原样使用
        if (safe.indexOf(' ') > 0) {
            return safe;
        }
        return OrderByUtil.build(defaultField, safe);
    }
}
