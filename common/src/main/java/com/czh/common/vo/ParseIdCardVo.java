package com.czh.common.vo;

import lombok.Data;

import java.util.Date;

/**
 * 身份证解析结果（StringUtil.parseIdCard 的返回类型）
 */
@Data
public class ParseIdCardVo {
    /** 生日 */
    private Date birthday;
    /** 性别：1男 0女 */
    private Integer sex;
}
