import { httpGet, httpPost } from "../../util/request";
import type { EditRoleDto, AddRoleDto } from "./RoleDto";
import type { AddRoleGetMenusVo, Role } from "./RoleVo";
import type { PageDto } from "../ApiTypes";
import type { PageInfoVo } from "../common/CommonVo";

/** 角色列表 */
export const roleListApi = (params: PageDto) => {
    return httpPost<PageInfoVo<Role>>("/admin/role/roleList", params);
}

/** 新增角色时获取所有菜单 */
export const addRoleGetMenusApi = () => {
    return httpGet<AddRoleGetMenusVo[]>("/admin/role/addRoleGetMenus", {});
}

/** 新增角色 */
export const addRoleApi = (params: AddRoleDto) => {
    return httpPost<void>("/admin/role/addRole", params);
}

/** 编辑角色 */
export const editRoleApi = (params: EditRoleDto) => {
    return httpPost<void>("/admin/role/editRole", params);
}

/** 删除角色 */
export const delRoleApi = (roleId: number) => {
    return httpGet<void>("/admin/role/delRole/" + roleId, {});
}
