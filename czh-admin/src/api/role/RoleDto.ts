/** 新增角色入参 */
export interface AddRoleDto {
    /** 角色描述 */
    describe: string;
    /** 勾选的菜单ID，多个用逗号隔开 */
    ids: string;
    /** 角色名称 */
    roleName: string;
}

/** 编辑角色入参 */
export interface EditRoleDto extends AddRoleDto {
    /** 角色ID */
    roleId: number;
}
