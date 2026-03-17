package com.jingdianjichi.subject.infra.basic.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 分类标签关联表实体
 */
@Data
public class SubjectCategoryLabel implements Serializable {

    private static final long serialVersionUID = 3361787390852550520L;

    private Long id;

    /**
     * 二级分类id
     */
    private Long categoryId;

    /**
     * 标签id
     */
    private Long labelId;

    /**
     * 当前分类下的排序
     */
    private Integer sortNum;

    private String createdBy;

    private Date createdTime;

    private String updateBy;

    private Date updateTime;

    private Integer isDeleted;
}
