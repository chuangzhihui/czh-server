import { httpGet, httpPost } from "../../util/request";
import type {
    AddFileDto,
    AddSettingDto,
    AliOssDto,
    EditSettingDto,
    GetFileListDto,
    SaveLocalDto,
    SaveQiniuDto,
    SetAuthConfigDto,
    TosDto,
    TxCosDto
} from "./SetDto";
import type {
    AuthConfig,
    GetSettingListVo,
    GetUploadConfigVo,
    GetUploadTokenVo,
    UploadFiles
} from "./SetVo";
import type { PageDto } from "../ApiTypes";
import type { PageInfoVo } from "../common/CommonVo";

/** 获取上传配置 */
export const getUploadConfigApi = () => {
    return httpGet<GetUploadConfigVo>("/admin/setting/getUploadConfig", {});
}

/** 保存为本地上传 */
export const saveLocalApi = (params: SaveLocalDto) => {
    return httpPost<void>("/admin/setting/saveLocal", params);
}

/** 保存七牛云配置 */
export const saveQiniuApi = (params: SaveQiniuDto) => {
    return httpPost<void>("/admin/setting/saveQiniu", params);
}

/** 保存阿里OSS配置 */
export const saveAliossApi = (params: AliOssDto) => {
    return httpPost<void>("/admin/setting/saveAlioss", params);
}

/** 保存腾讯COS配置 */
export const saveTxcosApi = (params: TxCosDto) => {
    return httpPost<void>("/admin/setting/saveTxcos", params);
}

/** 保存火山云配置 */
export const saveTosApi = (params: TosDto) => {
    return httpPost<void>("/admin/setting/saveTos", params);
}

/** 添加系统配置 */
export const addSettingApi = (params: AddSettingDto) => {
    return httpPost<void>("/admin/setting/addSetting", params);
}

/** 编辑系统配置 */
export const editSettingApi = (params: EditSettingDto) => {
    return httpPost<void>("/admin/setting/editSetting", params);
}

/** 删除系统配置 */
export const deleteSettingApi = (id: number) => {
    return httpGet<void>("/admin/setting/delSetting/" + id, {});
}

/** 系统设置列表 */
export const settingListApi = (params: PageDto) => {
    return httpPost<PageInfoVo<GetSettingListVo>>("/admin/setting/settingList", params);
}

/** 获取上传token */
export const getUploadTokenApi = () => {
    return httpGet<GetUploadTokenVo>("/admin/setting/getUploadToken", {});
}

/** 添加文件 */
export const addFileApi = (params: AddFileDto) => {
    return httpPost<void>("/admin/setting/addFile", params);
}

/** 删除文件 */
export const delFileApi = (id: number) => {
    return httpGet<void>("/admin/setting/delFile/" + id, {});
}

/** 获取文件列表 */
export const getFileListApi = (params: GetFileListDto) => {
    return httpPost<PageInfoVo<UploadFiles>>("/admin/setting/getFileList", params);
}

/** 获取系统安全配置 */
export const getAuthConfigApi = () => {
    return httpPost<AuthConfig>("/admin/setting/getAuthConfig", {});
}

/** 保存系统安全配置 */
export const setAuthConfigApi = (params: SetAuthConfigDto) => {
    return httpPost<void>("/admin/setting/setAuthConfig", params);
}
