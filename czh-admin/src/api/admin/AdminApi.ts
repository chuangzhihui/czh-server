import { httpGet, httpPost } from "../../util/request";
import type {
    AddAdminDto,
    AdminListDto,
    AdminLogDto,
    EditAdminDto,
    EditAvatarDto,
    EditPwdDto
} from "./AdminDto";
import type { AdminActionLog, AdminListVo, AuthConfig, GetLoginInfoVo } from "./AdminVo";
import type { PageInfoVo } from "../common/CommonVo";

/** 获取登录信息 */
export const getLoginInfoApi = () => {
    return httpPost<GetLoginInfoVo>("/admin/admin/getLoginInfo", {});
}

/** 退出登录 */
export const logoutApi = () => {
    return httpPost<void>("/admin/admin/loginOut", {});
}

/** 获取密码规则 */
export const getPwdRuleApi = () => {
    return httpPost<AuthConfig>("/admin/admin/getPwdRule", {});
}

/** 修改密码 */
export const editPwdApi = (params: EditPwdDto) => {
    return httpPost<void>("/admin/admin/editPwd", params);
}

/** 修改头像和昵称 */
export const editAvatarApi = (params: EditAvatarDto) => {
    return httpPost<void>("/admin/admin/editAvatar", params);
}

/** 添加管理员 */
export const addAdminApi = (params: AddAdminDto) => {
    return httpPost<void>("/admin/admin/addAdmin", params);
}

/** 修改管理员 */
export const editAdminApi = (params: EditAdminDto) => {
    return httpPost<void>("/admin/admin/editAdmin", params);
}

/** 删除管理员 */
export const delAdminApi = (adminId: number) => {
    return httpGet<void>("/admin/admin/delAdmin/" + adminId, {});
}

/** 管理员列表 */
export const adminListApi = (params: AdminListDto) => {
    return httpPost<PageInfoVo<AdminListVo>>("/admin/admin/adminList", params);
}

/** 冻结/解冻管理员 */
export const changeAdminStatusApi = (adminId: number) => {
    return httpGet<void>("/admin/admin/changeAdminStatus/" + adminId, {});
}

/** 管理员操作日志 */
export const adminLogApi = (params: AdminLogDto) => {
    return httpPost<PageInfoVo<AdminActionLog>>("/admin/admin/adminLog", params);
}
