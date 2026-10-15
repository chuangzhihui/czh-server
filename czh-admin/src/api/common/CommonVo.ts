/** 跨模块通用返回包装 */
import type { PageDto } from "../ApiTypes";

/** 分页返回包装 */
export interface PageInfoVo<T> {
    /** 数据列表 */
    list: T[];
    /** 总条数 */
    total: number;
}

/** 下拉项 */
export interface SelectVo {
    label?: string;
    value?: number;
}

/** 分页入参再导出：模块内继承使用，避免各处重复写 ../../ApiTypes 路径 */
export type CommonPageDto = PageDto;
