package com.czh.common.utils;

import com.czh.common.exception.ErrorException;
import com.czh.common.vo.ParseIdCardVo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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


     /* 从逗号分隔的字符串中删除指定元素，保持原格式（逗号分隔）
            * @param original 原始字符串，例如 "a,b,c,d"
            * @param toRemove 要删除的元素，例如 "b"
            * @return 删除后的新字符串，例如 "a,c,d"
            */
    public static String removeElement(String original, String toRemove) {
        // 1. 空值安全处理
        if (original == null || original.isEmpty()) {
            return original;
        }

        // 2. 按逗号分割成数组
        String[] elements = original.split(",");

        // 3. 用 StringBuilder 拼接结果
        StringBuilder sb = new StringBuilder();

        // 4. 遍历数组，跳过要删除的元素
        for (String elem : elements) {
            // 不匹配要删除的元素，才加入结果
            if (!elem.equals(toRemove)) {
                if (!sb.isEmpty()) {
                    sb.append(","); // 不是第一个元素，先加逗号
                }
                sb.append(elem);
            }
        }

        // 5. 返回最终字符串
        return sb.toString();
    }

    /**
     * 解析身份证，返回 ParseIdCardVo 对象
     * @param idCard 15位 或 18位身份证号
     * @return ParseIdCardVo
     */
    public static ParseIdCardVo parseIdCard(String idCard)  {
        ParseIdCardVo vo = new ParseIdCardVo();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String ID_CARD_REGEX = "(^\\d{15}$)|(^\\d{17}([0-9]|X|x)$)";
        // 正则校验身份证格式
        if (idCard == null || !Pattern.matches(ID_CARD_REGEX, idCard)) {
            throw new IllegalArgumentException("身份证号码格式不正确");
        }

        String birthStr;
        int genderNum;

        if (idCard.length() == 18) {
            // 18位身份证
            birthStr = idCard.substring(6, 14);
            genderNum = idCard.charAt(16) - '0';
        } else {
            // 15位身份证，补19开头
            birthStr = "19" + idCard.substring(6, 12);
            genderNum = idCard.charAt(14) - '0';
        }

        // 生日：字符串转 Date
        Date birthday = null;
        try {
            birthday = sdf.parse(birthStr);
        } catch (ParseException e) {
            throw new ErrorException("身份证解析失败");
        }
        vo.setBirthday(birthday);

        // 性别：1男 0女
        vo.setSex(genderNum % 2 == 1 ? 1 : 0);
        return vo;
    }

}
