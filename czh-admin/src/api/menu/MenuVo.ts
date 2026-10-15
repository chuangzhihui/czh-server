/** 按上级菜单查询出的子菜单项 */
export interface GetMenusByPidVo {
    id: number;
    name: string;
    path?: string;
}

/** 菜单列表行（含子级） */
export interface GetMenuListVo {
    child: GetMenuListVo[];
    display: number;
    icon: string;
    id: number;
    level: number;
    name: string;
    open: boolean;
    path?: string;
    fullPath?: string;
    pid: number;
    route?: string;
    sort: number;
}
