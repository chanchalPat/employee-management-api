package com.chanchal.employeemanagement.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Generic paginated response containing records and pagination metadata"
)
public class PageResponse<T> {

    @Schema(
            description = "Records returned on the current page",
            example = "[]"
    )
    private final List<T> content;

    @Schema(
            description = "Zero-based current page number",
            example = "0",
            minimum = "0"
    )
    private final int pageNumber;

    @Schema(
            description = "Maximum number of records requested per page",
            example = "10",
            minimum = "1"
    )
    private final int pageSize;

    @Schema(
            description = "Total number of records matching the search criteria",
            example = "25",
            minimum = "0"
    )
    private final long totalElements;

    @Schema(
            description = "Total number of pages available",
            example = "3",
            minimum = "0"
    )
    private final int totalPages;

    @Schema(
            description = "Indicates whether the current page is the first page",
            example = "true"
    )
    private final boolean first;

    @Schema(
            description = "Indicates whether the current page is the last page",
            example = "false"
    )
    private final boolean last;

    public PageResponse(
            List<T> content,
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    public List<T> getContent() {
        return content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }
}
