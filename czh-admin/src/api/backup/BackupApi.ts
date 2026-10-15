import { httpGet, httpPost } from "../../util/request";
import type { DbBackUpListDto } from "./BackupDto";
import type { DbBackupVo } from "./BackupVo";
import type { PageInfoVo } from "../common/CommonVo";
import { httpBlob } from "../../util/request";

/** 备份数据库 */
export const backUpDbApi = () => {
    return httpGet<void>("/admin/backup/backUpDb", {});
}

/** 根据备份记录ID恢复数据库 */
export const restoreDbApi = (id: number) => {
    return httpGet<void>("/admin/backup/restoreDb/" + id, {});
}

/** 获取备份文件列表 */
export const getBackDbListApi = (params: DbBackUpListDto) => {
    return httpPost<PageInfoVo<DbBackupVo>>("/admin/backup/getList", params);
}

/** 下载备份 */
export const downloadApi = (id: number) => {
    return httpBlob("/admin/backup/download/" + id, {});
}

/** 删除备份 */
export const removeBackUpFileApi = (id: number) => {
    return httpGet<void>("/admin/backup/removeBackUpFile/" + id, {});
}
