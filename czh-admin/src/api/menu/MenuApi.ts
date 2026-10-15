import { httpGet, httpPost } from "../../util/request";
import type { AddMenuDto, EditMenuDto } from "./MenuDto";
import type { GetMenuListVo, GetMenusByPidVo } from "./MenuVo";
import type { PageDto } from "../ApiTypes";
import type { PageInfoVo } from "../common/CommonVo";

/** 添加系统菜单 */
export const addMenuApi = (params: AddMenuDto) => {
    return httpPost<void>("/admin/menu/addMenu", params);
}

/** 根据上级菜单ID获取菜单列表 */
export const getMenusByPidApi = (pid: number) => {
    return httpGet<GetMenusByPidVo[]>("/admin/menu/getMenusByPid/" + pid, {});
}

/** 编辑菜单 */
export const editMenuApi = (params: EditMenuDto) => {
    return httpPost<void>("/admin/menu/editMenu", params);
}

/** 根据菜单ID删除菜单 */
export const delMenuApi = (id: number) => {
    return httpGet<void>("/admin/menu/delMenu/" + id, {});
}

/** 获取菜单列表 */
export const menuListApi = (params: PageDto) => {
    return httpPost<PageInfoVo<GetMenuListVo>>("/admin/menu/menuList", params);
}
