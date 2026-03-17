package com.jingdianjichi.subject.infra.basic.service.impl;

import com.jingdianjichi.subject.infra.basic.entity.SubjectCategoryLabel;
import com.jingdianjichi.subject.infra.basic.mapper.SubjectCategoryLabelDao;
import com.jingdianjichi.subject.infra.basic.service.SubjectCategoryLabelService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service("subjectCategoryLabelService")
public class SubjectCategoryLabelServiceImpl implements SubjectCategoryLabelService {

    @Resource
    private SubjectCategoryLabelDao subjectCategoryLabelDao;

    @Override
    public SubjectCategoryLabel queryById(Long id) {
        return subjectCategoryLabelDao.queryById(id);
    }

    @Override
    public List<SubjectCategoryLabel> queryByCondition(SubjectCategoryLabel subjectCategoryLabel) {
        return subjectCategoryLabelDao.queryByCondition(subjectCategoryLabel);
    }

    @Override
    public List<SubjectCategoryLabel> batchQueryByCategoryIds(List<Long> categoryIdList) {
        if (categoryIdList == null || categoryIdList.isEmpty()) {
            return Collections.emptyList();
        }
        return subjectCategoryLabelDao.batchQueryByCategoryIds(categoryIdList);
    }

    @Override
    public int insert(SubjectCategoryLabel subjectCategoryLabel) {
        return subjectCategoryLabelDao.insert(subjectCategoryLabel);
    }

    @Override
    public int update(SubjectCategoryLabel subjectCategoryLabel) {
        return subjectCategoryLabelDao.update(subjectCategoryLabel);
    }
}
