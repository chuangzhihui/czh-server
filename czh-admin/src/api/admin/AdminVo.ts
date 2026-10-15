/** 登录信息 */
export interface GetLoginInfoVo {
    /** 头像 */
    avatar: string;
    /** 修改密码提示文案 */
    changePwdTip: string;
    /** 修改密码提示：0 不提示 / 1 警告提示 / 2 强制修改 */
    changePwdType: number;
    /** 权限菜单 */
    menus: AuthMenuVo[];
    /** 系统名称 */
    name: string;
    /** 昵称 */
    username: string;
}

/** 权限菜单 */
export interface AuthMenuVo {
    child?: AuthMenuVo[];
    icon?: string;
    id?: number;
    path?: string;
    title?: string;
}

/** 系统安全 / 密码规则配置 */
export interface AuthConfig {
    /** 上次编辑时间 */
    atime?: string;
    /** 是否开启自动备份：0 不开启 / 1 开启 */
    autoBackup?: number;
    /** 备份数据的 cron 表达式 */
    bakupDbCron?: string;
    /** 连续密码错误次数后冻结账号 */
    failNum?: number;
    /** 清除密码错误记录的时间（秒） */
    failNumTime?: number;
    id?: number;
    /** 密码多少天后必须强制更换 */
    passMax?: number;
    /** 多少天开始提示用户修改密码 */
    passWran?: number;
    /** 密码正则 */
    pwdReg?: string;
    /** 密码正则描述 */
    pwdRegDesc?: string;
    /** 静默多久后登录失效（秒） */
    timeOut?: number;
}

/** 管理员列表行 */
export interface AdminListVo {
    adminId: number;
    atime: string;
    /** 上次修改密码的时间 */
    lastChangePwdTime: string;
    lastLoginIp: string;
    lastLoginTime: string;
    /** 上次登录失败的IP */
    lastTryLoginIp: string;
    /** 上次登录失败的时间 */
    lastTryLoginTime: string;
    roleId: number;
    roleName: string;
    /** 是否冻结 */
    status: number;
    /** 用户名 */
    userName: string;
}

/** 管理员操作日志行 */
export interface AdminActionLog {
    /** 返回值 */
    bake: string;
    /** 操作时间 */
    createTime: string;
    id: string;
    /** 操作IP */
    ip: string;
    /** 操作地址 */
    method: string;
    /** 操作名称 */
    operation: string;
    /** 请求数据 */
    params: string;
    /** 操作用户ID */
    userID: number;
    /** 操作用户 */
    userName: string;
}
