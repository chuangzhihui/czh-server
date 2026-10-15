import { httpGet, httpPost } from "../../util/request";
import type { LoginDto } from "./LoginDto";
import type { AdminLoginVo, GetSystemNameVo, GetVerifyVo } from "./LoginVo";

/** 登录 */
export const loginApi = (params: LoginDto) => {
    return httpPost<AdminLoginVo>("/admin/login/login", params);
}

/** 获取系统名称 */
export const getSystemNameApi = () => {
    return httpGet<GetSystemNameVo>("/admin/login/getSystemName", {});
}

/** 获取图形验证码 */
export const getCaptchaApi = () => {
    return httpGet<GetVerifyVo>("/admin/login/getCaptcha", {});
}
