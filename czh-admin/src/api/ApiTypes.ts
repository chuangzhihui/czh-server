/** 跨模块通用入参（不涉及返回类型，返回包装请放 api/common/CommonVo.ts） */

/** 分页入参 */
export interface PageDto {
    /** 当前页 */
    page: number;
    /** 每页展示条数 */
    size: number;
    /** 排序字段 */
    orderBy?: string;
}
