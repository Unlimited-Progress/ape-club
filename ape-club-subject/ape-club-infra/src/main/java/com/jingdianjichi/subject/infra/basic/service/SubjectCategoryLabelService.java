package com.jingdianjichi.subject.infra.basic.service;

import com.jingdianjichi.subject.infra.basic.entity.SubjectCategoryLabel;

import java.util.List;

public interface SubjectCategoryLabelService {

    SubjectCategoryLabel queryById(Long id);

    List<SubjectCategoryLabel> queryByCondition(SubjectCategoryLabel subjectCategoryLabel);

    List<SubjectCategoryLabel> batchQueryByCategoryIds(List<Long> categoryIdList);

    int insert(SubjectCategoryLabel subjectCategoryLabel);

    int update(SubjectCategoryLabel subjectCategoryLabel);
}
