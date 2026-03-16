package com.jingdianjichi.subject.infra.basic.entity;

import com.jingdianjichi.subject.common.entity.PageInfo;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SubjectInfoEs extends PageInfo implements Serializable {

    private Long subjectId;

    private Long docId;

    private String subjectName;

    private String subjectAnswer;

    private String createUser;

    private Long createTime;

    private Integer subjectType;

    private String keyWord;

    //分数越高，相关性越高
    private BigDecimal score;
}
