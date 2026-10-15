/** 阿里OSS配置 */
export interface AliOssEntity {
    /** 表单开关：是否启用该渠道（0 未启用） */
    visible?: number | boolean;
    /** AccessKey */
    ak?: string;
    /** 空间名称 */
    bucket?: string;
    /** 自定义域名 */
    domain?: string;
    /** 节点地址 */
    endpoint?: string;
    /** SecretKey */
    sk?: string;
}

/** 本地上传配置 */
export interface LocalUploadConfig {
    /** 表单开关：是否启用该渠道（0 未启用） */
    visible?: number | boolean;
    /** 访问域名 */
    domain?: string;
    /** 上传地址 */
    host?: string;
    /** 上传文件的基础目录，不要以斜线结尾 */
    path?: string;
}

/** 七牛云配置 */
export interface QiniuEntity {
    /** 表单开关：是否启用该渠道（0 未启用） */
    visible?: number | boolean;
    /** 七牛云 AccessKey */
    accessKey?: string;
    /** 空间名称 */
    bucket?: string;
    /** 自定义域名 */
    domain?: string;
    /** 节点地址 */
    endpoint?: string;
    /** 七牛云 SecretKey */
    secretKey?: string;
}

/** 火山云配置 */
export interface TosEntity {
    /** 表单开关：是否启用该渠道（0 未启用） */
    visible?: number | boolean;
    accessKey?: string;
    /** 空间名称 */
    bucket?: string;
    /** 自定义域名 */
    domain?: string;
    /** 节点 */
    endpoint?: string;
    /** 地域 */
    region?: string;
    secretKey?: string;
}

/** 腾讯云COS配置 */
export interface TxCosEntity {
    /** 表单开关：是否启用该渠道（0 未启用） */
    visible?: number | boolean;
    ak?: string;
    bucket?: string;
    bucketName?: string;
    domain?: string;
    sk?: string;
}

/** 上传配置总览 */
export interface GetUploadConfigVo {
    /** 阿里OSS配置 */
    alioss: AliOssEntity;
    /** 本地配置 */
    local: LocalUploadConfig;
    /** 七牛云配置 */
    qiniu: QiniuEntity;
    /** 火山云配置 */
    tos: TosEntity;
    /** 腾讯云COS配置 */
    txcos: TxCosEntity;
    /** 当前选中项：0 未选用 1 七牛 2 阿里 3 腾讯 4 本地 5 火山云 */
    visible: number;
}

/** 系统配置列表行 */
export interface GetSettingListVo {
    /** 是否允许删除 */
    canDel: number;
    id: number;
    title: string;
    /** 1 文本 / 2 数字 / 3 图片 / 4 图文 */
    type: number;
    value: string;
}

/**
 * 上传凭证
 * 说明：不同上传渠道返回的字段不同，均为可选，按 visible 判断使用哪一组
 */
export interface GetUploadTokenVo {
    /** 上传token */
    token?: string;
    /** 上传地址 */
    host?: string;
    /** 上传类型：1 七牛 2 阿里云 3 腾讯云 4 本地 5 火山云 */
    type?: number;
    /** 当前启用渠道，取值同上 */
    visible?: number;
    /** 七牛节点地址 / 腾讯端点 */
    endpoint?: string;
    /** 阿里云域名 */
    domain?: string;
    /** 腾讯云路径 */
    path?: string;
    /** 本地服务器上传地址 */
    uploadUrl?: string;
    /** 阿里云：策略 */
    policy?: string;
    /** 阿里云：AccessKeyId */
    OSSAccessKeyId?: string;
    /** 阿里云：签名 */
    signature?: string;
    /** 腾讯云：临时密钥 Id */
    TmpSecretId?: string;
    /** 腾讯云：临时密钥 Key */
    TmpSecretKey?: string;
    /** 腾讯云：安全令牌 */
    SecurityToken?: string;
    /** 腾讯云：密钥生效时间 */
    StartTime?: number;
    /** 腾讯云：密钥失效时间 */
    ExpiredTime?: number;
    /** 腾讯云：存储桶 */
    Bucket?: string;
    /** 腾讯云：地域 */
    Region?: string;
}

/** 文件库实体 */
export interface UploadFiles {
    atime: string;
    /** 文件保存位置：0 虚拟文件夹 1 七牛 2 阿里oss 3 腾讯cos */
    domain: number;
    /** 图片或视频高，其它为 0 */
    fileHeight: number;
    /** 文件大小（kb） */
    fileSize: number;
    /** 图片或视频宽，其它为 0 */
    fileWidth: number;
    id: number;
    /** 上传到第三方的 key */
    key: string;
    /** 文件名称 */
    name: string;
    /** 上级目录ID，顶级目录为 0 */
    pid: number;
    /** 图片缩略图或视频封面图 */
    thumb?: string;
    /** 文件类型：1 图片 2 视频 3 Excel 4 word 5 pdf 6 zip 7 未知类型 8 文件夹 */
    type: number;
    /** 文件地址 */
    url: string;
}

/** 系统安全 / 密码规则配置 */
export interface AuthConfig {
    /** 上次编辑时间 */
    atime: string;
    /** 是否开启自动备份：0 不开启 / 1 开启 */
    autoBackup: number;
    /** 备份数据的 cron 表达式 */
    bakupDbCron: string;
    /** 连续密码错误次数后冻结账号 */
    failNum: number;
    /** 清除密码错误记录的时间（秒） */
    failNumTime: number;
    id: number;
    /** 密码多少天后必须强制更换 */
    passMax: number;
    /** 多少天开始提示用户修改密码 */
    passWran: number;
    /** 密码正则 */
    pwdReg: string;
    /** 密码正则描述 */
    pwdRegDesc: string;
    /** 静默多久后登录失效（秒） */
    timeOut: number;
}
