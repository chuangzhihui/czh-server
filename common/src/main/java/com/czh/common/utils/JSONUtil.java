package com.czh.common.utils;

import com.alibaba.fastjson.JSONArray;

public class JSONUtil {
    /**
     * 将json数组以特定符号连接起来
     * @param separator
     * @param jarr
     * @return
     */
    public static String implodeJsonArray(String separator, JSONArray jarr){
        String res="";
        for (int i = 0; i < jarr.size(); i++) {
            if(i<jarr.size()-1){
                res += jarr.getString(i)+separator;
            }else{
                res += jarr.getString(i);
            }

        }
        return res;
    }

    /**
     * 判断jsonarray中是否包含某个值
     * @param a
     * @param jarr
     * @return
     */
    public static boolean InJsonArray(Object a,JSONArray jarr){
        for(Object o : jarr){
            if(o.equals(a)){
                return true;
            }
        }
        return false;
    }
}
