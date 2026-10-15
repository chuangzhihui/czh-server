import type { PageDto } from "../ApiTypes";
import type {
    AliOssEntity,
    LocalUploadConfig,
    QiniuEntity,
    TosEntity,
    TxCosEntity
} from "./SetVo";

/** 保存本地上传配置入参 */
export interface SaveLocalDto extends LocalUploadConfig {
    visible: number;
}

/** 保存七牛云配置入参 */
export interface SaveQiniuDto extends QiniuEntity {
    visible: number;
}

/** 保存阿里OSS配置入参 */
export interface AliOssDto extends AliOssEntity {
    visible: number;
}

/** 保存腾讯COS配置入参 */
export interface TxCosDto extends TxCosEntity {
    visible: number;
}

/** 保存火山云配置入参 */
export interface TosDto extends TosEntity {
    visible: number;
}

/** 新增系统配置入参 */
export interface AddSettingDto {
    /** 是否允许删除这个配置 */
    canDel?: number;
    title: string;
    /** 1 文本 / 2 数字 / 3 图片 / 4 图文 */
    type: number;
    value: string;
}

/** 编辑系统配置入参 */
export interface EditSettingDto {
    id: number;
    title: string;
    /** 1 文本 / 2 数字 / 3 图片 / 4 图文 */
    type: number;
    value: string;
}

/** 新增文件（入库）入参 */
export interface AddFileDto {
    /** 文件保存位置：0 虚拟文件夹 1 七牛 2 阿里oss 3 腾讯cos 4 本地 5 火山云 */
    domain: number;
    /** 图片或视频高，其它为 0 */
    fileHeight?: number;
    /** 文件大小（kb） */
    fileSize?: number;
    /** 图片或视频宽，其它为 0 */
    fileWidth?: number;
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

/** 文件列表入参 */
export interface GetFileListDto extends PageDto {
    name?: string;
    pid?: number;
    /** 查询哪些文件类型，多个逗号隔开 */
    types?: string;
}

/** 保存系统安全配置入参 */
export interface SetAuthConfigDto {
    /** 是否开启自动备份：0 不开启 / 1 开启 */
    autoBackup: number;
    /** 备份数据的 cron 表达式 */
    bakupDbCron: string;
    /** 连续密码错误次数后冻结账号 */
    failNum: number;
    /** 清除密码错误记录的时间（秒） */
    failNumTime: number;
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
