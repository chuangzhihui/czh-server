package com.czh.common.utils;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

public class CZHUtil {
    /**
     * 字符串分割，转化为数组
     *
     * @param str 字符串
     * @return List<String>

     * @since 2020-04-22
     */
    public static List<String> stringToArrayStr(String str) {
        return stringToArrayStrRegex(str, ",");
    }
    /**
     * 数字字符数据转int格式数据
     *
     * @param str 待转换的数字字符串
     * @return int数组
     */
    public static List<Integer> stringToArrayInt(String str) {
        List<String> strings = stringToArrayStrRegex(str, ",");
        List<Integer> ids = new ArrayList<>();
        for (String string : strings) {
            ids.add(Integer.parseInt(string.trim()));
        }
        return ids;
    }

    /**
     * 字符串分割，转化为数组
     *
     * @param str   字符串
     * @param regex 分隔符有
     * @return List<String>
     */
    public static List<String> stringToArrayStrRegex(String str, String regex) {
        List<String> list = new ArrayList<>();
        if (str.contains(regex)) {
            String[] split = str.split(regex);
            for (String value : split) {
                if (StrUtil.isNotBlank(value)) {
                    list.add(value);
                }
            }
        } else {
            list.add(str);
        }
        return list;
    }

}
