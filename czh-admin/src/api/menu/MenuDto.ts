/** 新增菜单入参 */
export interface AddMenuDto {
    /** 是否显示：0 否 / 1 是 */
    display: number;
    /** 图标 */
    icon?: string;
    /** 菜单等级，最高三级 */
    level: number;
    /** 菜单名称 */
    name: string;
    /** 前端路由 */
    path?: string;
    /** 上级菜单ID */
    pid: number;
    /** 后端路由 */
    route?: string;
    /** 排序 */
    sort: number;
}

/** 编辑菜单入参 */
export interface EditMenuDto extends AddMenuDto {
    /** 菜单ID */
    id: number;
}
