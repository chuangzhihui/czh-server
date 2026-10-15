package com.czh.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 敏感字段脱敏工具
 * <p>
 * 用于操作日志等场景：把请求体中的密码字段、返回体中的令牌字段的<b>值替换为 {@code ****}</b>，
 * 字段名保留，避免明文密码与 JWT 落库后被有日志查看权限的人获取。
 * <p>
 * 匹配规则：字段名（忽略大小写）<b>包含</b>以下任一关键字即被脱敏
 * <ul>
 *     <li>{@code password} —— 覆盖 password / oldPassword / newPassword</li>
 *     <li>{@code pwd}      —— 覆盖 pwd / oldPwd / newPwd</li>
 *     <li>{@code token}    —— 覆盖 token / accessToken / refreshToken</li>
 * </ul>
 * 递归处理 JSONObject / JSONArray，任意层级均可命中。
 *
 * @see com.czh.common.annotation.Log
 */
public class SensitiveUtil {

    /** 敏感字段名关键字（忽略大小写，命中任一即将字段值替换为掩码） */
    private static final List<String> SENSITIVE_KEYWORDS = Arrays.asList("password", "pwd", "token");

    /** 脱敏后的占位值 */
    private static final String MASK = "****";

    private SensitiveUtil() {
    }

    /**
     * 将对象序列化为 JSON 字符串，并把其中敏感字段的值替换为 {@code ****}（字段名保留）。
     *
     * @param obj 待序列化对象；可以是 POJO、Map，也可以是已序列化好的 JSON 字符串
     * @return 脱敏后的 JSON 字符串；入参为 null 或序列化异常时返回空串，绝不抛出业务异常
     */
    public static String safeJson(Object obj) {
        if (obj == null) {
            return "";
        }
        try {
            Object node;
            if (obj instanceof String) {
                String str = ((String) obj).trim();
                // 已是 JSON 文本则直接解析，普通字符串原样返回
                if ((str.startsWith("{") && str.endsWith("}")) || (str.startsWith("[") && str.endsWith("]"))) {
                    node = JSON.parse(str);
                } else {
                    return (String) obj;
                }
            } else {
                node = JSON.parse(JSON.toJSONString(obj));
            }
            clean(node);
            return JSON.toJSONString(node);
        } catch (Exception e) {
            // 脱敏仅服务于日志记录，失败不应影响业务主流程
            return "";
        }
    }

    /**
     * 递归脱敏：命中关键字的字段保留键名，值替换为 {@code ****}
     */
    private static void clean(Object node) {
        if (node instanceof JSONObject) {
            JSONObject json = (JSONObject) node;
            // 用副本遍历，避免遍历过程中修改结构触发并发修改异常
            for (String key : new ArrayList<>(json.keySet())) {
                if (isSensitive(key)) {
                    json.put(key, MASK);
                    continue;
                }
                clean(json.get(key));
            }
        } else if (node instanceof JSONArray) {
            JSONArray array = (JSONArray) node;
            for (int i = 0; i < array.size(); i++) {
                clean(array.get(i));
            }
        }
    }

    /**
     * 判断字段名是否为敏感字段
     */
    private static boolean isSensitive(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        String lowerKey = key.toLowerCase();
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (lowerKey.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
