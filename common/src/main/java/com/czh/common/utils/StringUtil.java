package com.czh.common.utils;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    public static String getRadomStr(int length){
        String str="9876543210123456789";
        StringBuilder sb=new StringBuilder(4);
        for(int i=0;i<length;i++)
        {
            char ch=str.charAt(new Random().nextInt(str.length()));
            sb.append(ch);
        }
        return sb.toString();
    }
    /**
     * 字符串是否包含中文
     *
     * @param str 待校验字符串
     * @return true 包含中文字符 false 不包含中文字符
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4E00-\u9FA5|\\！|\\，|\\。|\\（|\\）|\\《|\\》|\\“|\\”|\\？|\\：|\\；|\\【|\\】]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 是否是英文字符串
     * @param charaString
     * @return
     */
    public static boolean isEnglishStr(String charaString){
        return charaString.matches("^[a-zA-Z]*");
    }

    /**
     * 检查字符串中是否包含特殊符号
     * @param str
     * @return
     */
    public static boolean isSpecialChar(String str) {
        String regEx = "[_`~!@#$%^&*()+=|{}':;'\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }

    /**
     * 隐藏手机号中间四位 如176****2061
     * @param phone
     * @return
     */
    public static String hidePhone(String phone) {
        return phone.substring(0,3)+"****"+phone.substring(7);
    }


    public static String hexToString(String hexValue) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hexValue.length(); i += 2) {
            String hexChar = hexValue.substring(i, i + 2);
            int decimalValue = Integer.parseInt(hexChar, 16);
            output.append((char) decimalValue);
        }
        return output.toString();
    }


    public static String[] splitString(String input) {
        int length = input.length();
        int arraySize = (length + 1) / 2;
        String[] result = new String[arraySize];
        int index = 0;
        for (int i = 0; i < length; i += 2) {
            if (i + 2 <= length) {
                result[index++] = input.substring(i, i + 2);
            } else {
                result[index++] = input.substring(i);
            }
        }
        return result;
    }
}
