package com.czh.common.dto;

import lombok.Data;

@Data
public class PageDto {
    private int page;
    private int size;
    private String orderBy;//排序方式
}
