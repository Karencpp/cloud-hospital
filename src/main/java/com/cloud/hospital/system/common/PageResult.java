package com.cloud.hospital.system.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "通用分页返回结果")
public class PageResult<T> {

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "总页数")
    private Long pages;

    @Schema(description = "当前页数据列表")
    private List<T> records;

    public PageResult(Long total, Long pages, List<T> records) {
        this.total = total;
        this.pages = pages;
        this.records = records;
    }
}
