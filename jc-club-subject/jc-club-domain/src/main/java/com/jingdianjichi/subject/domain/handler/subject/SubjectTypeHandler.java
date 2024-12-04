package com.jingdianjichi.subject.domain.handler.subject;

import com.jingdianjichi.subject.common.enums.SubjectInfoTypeEnum;
import com.jingdianjichi.subject.domain.entity.SubjectInfoBO;
import com.jingdianjichi.subject.domain.entity.SubjectOptionBO;
import com.jingdianjichi.subject.infra.basic.entity.SubjectInfo;


//当每个接口都差不多可以抽象出来时，用工厂加策略的模式

//这个接口要知道我们要操作什么东西，怎么去辨别出来的
//把四种类型聚合起来（单选，多选）
public interface SubjectTypeHandler {

    //由各自的策略标识出来我到底干嘛了，我叫什么名字
    //枚举身份识别
    SubjectInfoTypeEnum getHandlerType();


    //实际的题目插入
    void add(SubjectInfoBO subjectInfoBO);

    /**
     * 实际的题目的查询
     */
    SubjectOptionBO query(int subjectId);

}
