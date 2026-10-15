/** 登录入参 */
export interface LoginDto {
    /** 图形验证码 */
    code: string;
    /** 密码 */
    password: string;
    /** 用户名 */
    username: string;
    /** 图形验证码标识 */
    uuid: string;
}
