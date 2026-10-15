/** 角色列表行 */
export interface Role {
    atime: string;
    describe: string;
    /** 该角色拥有的菜单ID集合 */
    ids: string;
    roleId: number;
    roleName: string;
}

/** 新增角色时的菜单树节点 */
export interface AddRoleGetMenusVo {
    child?: AddRoleGetMenusVo[];
    id: number;
    name: string;
}
