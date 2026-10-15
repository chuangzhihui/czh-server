/** 系统名称 */
export interface GetSystemNameVo {
    /** 系统名称 */
    name: string;
}

/** 图形验证码 */
export interface GetVerifyVo {
    /** 图片 base64 */
    img: string;
    /** 图片验证码标识 */
    uuid: string;
}

/** 登录成功返回 */
export interface AdminLoginVo {
    avatar: string;
    nickname: string;
    token: string;
}
