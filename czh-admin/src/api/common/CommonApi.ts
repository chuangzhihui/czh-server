import { httpPost } from "../../util/request";
import type { SelectVo } from "./CommonVo";

/** 获取系统角色下拉列表 */
export const getRoleSelectListApi = () => {
    return httpPost<SelectVo[]>("/admin/common/getRoleSelectList", {});
}

/** 获取所有管理员下拉 */
export const getAdminSelectListApi = () => {
    return httpPost<SelectVo[]>("/admin/common/getAdminSelectList", {});
}
