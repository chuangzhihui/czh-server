package com.czh.common.utils;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Objects;

/**
 *  Request工具类
 *  +----------------------------------------------------------------------
 *  | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2025 https://www.crmeb.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 *  +----------------------------------------------------------------------
 *  | Author: CRMEB Team <admin@crmeb.com>
 *  +----------------------------------------------------------------------
 */
public class RequestUtil extends HttpServlet {


    public static HttpServletRequest getRequest() {
        if(RequestContextHolder.getRequestAttributes() != null){
            return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        }
        return null;
    }
    public static String getIp(){
        HttpServletRequest request=getRequest();
        // 优先取 X-Real-IP
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("x-forwarded-for");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
            if ("0:0:0:0:0:0:0:1".equals(ip))
            {
                ip = "unknown";
            }
        }
        if ("unknown".equalsIgnoreCase(ip)){
            ip = "unknown";
            return ip;
        }
        int index = ip.indexOf(',');
        if (index >= 0){
            ip = ip.substring(0, index);
        }
        return ip;
    }
    public static HashMap<String, Object> getRequestParamAndHeader(){

        try{
            HttpServletRequest request = getRequest();
            if(request == null){
                return null;
            }

            //request信息
            HashMap<String, Object> data = new HashMap<>();
            HashMap<String, Object> requestParams = new HashMap<>();
            Enumeration<String> paraNames = request.getParameterNames();
            if(paraNames != null){
                for(Enumeration<String> enumeration =paraNames;enumeration.hasMoreElements();){
                    String key= enumeration.nextElement();
                    requestParams.put(key, request.getParameter(key));
                }
            }

            HashMap<String, Object> requestFilter = new HashMap<>();
            Enumeration<String> attributeNames = request.getAttributeNames();
            if(attributeNames != null){
                for ( Enumeration<String> attributeNames1 = attributeNames; attributeNames1.hasMoreElements();) {
                    String key= attributeNames1.nextElement();
                    if(key.contains("request_")){
                        requestFilter.put(key, request.getAttribute(key));
                    }
                }
            }

            data.put("url", request.getRequestURL());
            data.put("uri", request.getRequestURI());
            data.put("method", request.getMethod());
            data.put("request", requestParams);
            data.put("request_filter", requestFilter);

            //header 信息
            Enumeration<String> headerNames = request.getHeaderNames();
            HashMap<String, Object> headerParams = new HashMap<>();
            if(headerNames != null){
                for(Enumeration<String> enumeration = headerNames;enumeration.hasMoreElements();){
                    String key= enumeration.nextElement();
                    String value=request.getHeader(key);
                    headerParams.put(key, value);
                }
            }
            data.put("header", headerParams);


            return data;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String getDomain(){
        HttpServletRequest request = getRequest();
        if(request == null){
            return null;
        }
        return request.getServerName() + ":" + request.getServerPort();
    }


    public static String getRequestRoute() {
        HttpServletRequest request = getRequest();
        StringBuilder route= new StringBuilder();
        String[] uris= {};
        if (request != null) {
            uris = request.getRequestURI().split("/");
        }
        for(int i=0;i<4;i++)
        {
            route.append(uris[i]);
            if(i<3)
            {
                route.append("/");
            }
        }
        return route.toString();
    }
}
