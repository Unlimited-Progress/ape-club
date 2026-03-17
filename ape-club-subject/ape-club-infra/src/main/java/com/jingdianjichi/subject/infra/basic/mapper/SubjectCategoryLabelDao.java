package com.jingdianjichi.subject.infra.basic.mapper;

import com.jingdianjichi.subject.infra.basic.entity.SubjectCategoryLabel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SubjectCategoryLabelDao {

    SubjectCategoryLabel queryById(Long id);

    List<SubjectCategoryLabel> queryByCondition(SubjectCategoryLabel subjectCategoryLabel);

    List<SubjectCategoryLabel> batchQueryByCategoryIds(@Param("list") List<Long> categoryIdList);

    int insert(SubjectCategoryLabel subjectCategoryLabel);

    int update(SubjectCategoryLabel subjectCategoryLabel);
}
