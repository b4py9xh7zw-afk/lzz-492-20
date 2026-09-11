package com.scaffolding.common;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装
 * 
 * @author scaffolding
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    public PageResult() {
    }

    public PageResult(Long total, List<T> records, Long current, Long size) {
        this.total = total;
        this.records = records;
        this.current = current;
        this.size = size;
    }
}
