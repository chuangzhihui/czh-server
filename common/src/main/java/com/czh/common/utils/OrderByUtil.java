package com.czh.common.utils;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 排序参数白名单工具（缺陷 BE-03 的公共防线）。
 * <p>
 * 背景：PageHelper 的 {@code orderBy} 是<b>原文拼接</b>到 SQL 尾部，不做任何转义。
 * 因此在把前端传入的排序串交给 {@code PageHelper.startPage(page, size, orderBy)} 之前，
 * 必须先用本工具做白名单校验。
 * <p>
 * 白名单规则（只做白名单，绝不做黑名单过滤或字符转义）：
 * <ul>
 *   <li>字段名：{@code [A-Za-z_][A-Za-z0-9_]{0,63}}</li>
 *   <li>排序方向：{@code asc} / {@code desc}（大小写不敏感）</li>
 *   <li>合法形态只有两种：{@code "direction"} 或 {@code "field direction"}，其余一律丢弃</li>
 * </ul>
 * <p>
 * 典型攻击载荷 {@code asc,1=(select 1 from (select sleep(5))a)} 会被 {@link #sanitize(String)}
 * 判定为非法并返回空串，从而彻底无法进入 SQL。
 */
public final class OrderByUtil {

    /** 排序字段名白名单：字母/下划线开头，仅含字母、数字、下划线，长度 1~64 */
    private static final Pattern FIELD_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]{0,63}$");

    /** 排序方向白名单：仅 asc / desc，大小写不敏感 */
    private static final Pattern DIRECTION_PATTERN = Pattern.compile("^asc$|^desc$", Pattern.CASE_INSENSITIVE);

    /** 非法排序串的归一化结果（空串 = 不排序） */
    public static final String EMPTY_ORDER_BY = "";

    private OrderByUtil() {
        // 工具类禁止实例化
    }

    /**
     * 判断字段名是否合法
     *
     * @param field 待校验的字段名，允许为 null
     * @return 命中白名单返回 true
     */
    public static boolean isValidField(String field) {
        return field != null && FIELD_PATTERN.matcher(field).matches();
    }

    /**
     * 判断排序方向是否合法
     *
     * @param direction 待校验的方向，允许为 null
     * @return 命中白名单返回 true
     */
    public static boolean isValidDirection(String direction) {
        return direction != null && DIRECTION_PATTERN.matcher(direction).matches();
    }

    /**
     * 白名单过滤：把任意来源的排序串归一化为安全的排序串。
     * <p>
     * 非法输入一律返回空串（丢弃排序），<b>不抛异常</b>——排序只是展示层的次要能力，
     * 因排序参数非法就让整个列表接口 500 属于把可用性交给攻击者。
     *
     * @param raw 原始排序串，允许为 null / 空
     * @return 安全排序串；非法返回 {@link #EMPTY_ORDER_BY}
     */
    public static String sanitize(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return EMPTY_ORDER_BY;
        }
        // 以空白切分；多余空白（含制表符、换行）一并折叠
        String[] tokens = raw.trim().split("\\s+");
        if (tokens.length == 1) {
            String single = tokens[0];
            if (isValidDirection(single)) {
                return single.toLowerCase(Locale.ROOT);
            }
            if (isValidField(single)) {
                return single;
            }
            return EMPTY_ORDER_BY;
        }
        if (tokens.length == 2) {
            String field = tokens[0];
            String direction = tokens[1];
            if (isValidField(field) && isValidDirection(direction)) {
                return field + " " + direction.toLowerCase(Locale.ROOT);
            }
            return EMPTY_ORDER_BY;
        }
        // 3 段及以上：必然不是「字段 + 方向」，一律丢弃
        return EMPTY_ORDER_BY;
    }

    /**
     * 判断原始排序串是否能被白名单接纳
     *
     * @param raw 原始排序串
     * @return 合法返回 true
     */
    public static boolean isValid(String raw) {
        return !EMPTY_ORDER_BY.equals(sanitize(raw));
    }

    /**
     * 安全地拼接「字段名 + 方向」，任一参数不合法即返回空串。
     *
     * @param field     排序字段名（必须是白名单内的真实列名）
     * @param direction 排序方向 asc / desc
     * @return 安全排序串；非法返回 {@link #EMPTY_ORDER_BY}
     */
    public static String build(String field, String direction) {
        if (!isValidField(field) || !isValidDirection(direction)) {
            return EMPTY_ORDER_BY;
        }
        return field + " " + direction.toLowerCase(Locale.ROOT);
    }
}
