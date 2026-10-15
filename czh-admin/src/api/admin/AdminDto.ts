import type { PageDto } from "../ApiTypes";

/** 修改密码入参 */
export interface EditPwdDto {
    /** 旧密码 */
    oldPwd: string;
    /** 新密码 */
    password: string;
}

/** 修改头像和昵称入参 */
export interface EditAvatarDto {
    /** 头像（头像和用户名不能同时为空） */
    avatar?: string;
    /** 用户名（头像和用户名不能同时为空） */
    username?: string;
}

/** 新增管理员入参 */
export interface AddAdminDto {
    password: string;
    roleId: number;
    userName: string;
}

/** 编辑管理员入参 */
export interface EditAdminDto {
    adminId: number;
    /** 留空表示不修改密码 */
    password?: string;
    roleId: number;
    userName: string;
}

/** 管理员列表入参 */
export interface AdminListDto extends PageDto {
    /** 昵称 */
    name?: string;
    /** 角色ID */
    roleId?: number;
}

/** 管理员操作日志入参 */
export interface AdminLogDto extends PageDto {
    /** 操作地址 */
    address?: string;
    /** 管理员ID */
    adminId?: number;
    /** 操作内容描述 */
    desc?: string;
    /** 操作结束时间（与开始时间成对出现） */
    etime?: string;
    /** 操作IP */
    ip?: string;
    /** 操作开始时间（与结束时间成对出现） */
    stime?: string;
}
