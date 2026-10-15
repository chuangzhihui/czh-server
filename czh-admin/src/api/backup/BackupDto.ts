import type { PageDto } from "../ApiTypes";

/** 备份记录列表入参 */
export interface DbBackUpListDto extends PageDto {
    adminId?: number;
    /** 备份结束时间（与开始时间成对出现） */
    etime?: string;
    /** 备份开始时间 */
    stime?: string;
}
