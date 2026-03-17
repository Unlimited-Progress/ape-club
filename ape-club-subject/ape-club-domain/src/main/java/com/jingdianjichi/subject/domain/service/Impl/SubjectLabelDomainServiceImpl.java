package com.jingdianjichi.subject.domain.service.Impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.jingdianjichi.subject.common.enums.CategoryTypeEnum;
import com.jingdianjichi.subject.common.enums.IsDeletedFlagEnum;
import com.jingdianjichi.subject.common.util.LoginUtil;
import com.jingdianjichi.subject.domain.convert.SubjectLabelConverter;
import com.jingdianjichi.subject.domain.entity.SubjectLabelBO;
import com.jingdianjichi.subject.domain.service.SubjectLabelDomainService;
import com.jingdianjichi.subject.domain.util.CacheUtil;
import com.jingdianjichi.subject.infra.basic.entity.SubjectCategory;
import com.jingdianjichi.subject.infra.basic.entity.SubjectCategoryLabel;
import com.jingdianjichi.subject.infra.basic.entity.SubjectLabel;
import com.jingdianjichi.subject.infra.basic.entity.SubjectMapping;
import com.jingdianjichi.subject.infra.basic.service.SubjectCategoryLabelService;
import com.jingdianjichi.subject.infra.basic.service.SubjectCategoryService;
import com.jingdianjichi.subject.infra.basic.service.SubjectLabelService;
import com.jingdianjichi.subject.infra.basic.service.SubjectMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubjectLabelDomainServiceImpl implements SubjectLabelDomainService {

    @Resource
    private SubjectLabelService subjectLabelService;

    @Resource
    private SubjectMappingService subjectMappingService;

    @Resource
    private SubjectCategoryService subjectCategoryService;

    @Resource
    private SubjectCategoryLabelService subjectCategoryLabelService;

    @Resource
    private CacheUtil cacheUtil;

    @Override
    public Boolean add(SubjectLabelBO subjectLabelBO) {
        if (log.isInfoEnabled()) {
            log.info("SubjectLabelDomainServiceImpl.add.bo:{}", JSON.toJSONString(subjectLabelBO));
        }

        SubjectCategory secondaryCategory = subjectCategoryService.queryById(subjectLabelBO.getCategoryId());
        Preconditions.checkNotNull(secondaryCategory, "二级分类不存在");
        Preconditions.checkArgument(Objects.equals(secondaryCategory.getCategoryType(), CategoryTypeEnum.SECOND.getCode()),
                "标签必须挂在二级分类下");

        SubjectLabel existingLabel = getExistingLabel(subjectLabelBO.getLabelName());
        Integer nextSortNum = getNextSortNum(subjectLabelBO.getCategoryId());
        SubjectLabel subjectLabel = existingLabel;
        if (Objects.isNull(subjectLabel)) {
            subjectLabel = SubjectLabelConverter.INSTANCE.convertBoToLabel(subjectLabelBO);
            subjectLabel.setSortNum(nextSortNum);
            subjectLabel.setCategoryId(String.valueOf(subjectLabelBO.getCategoryId()));
            subjectLabel.setCreatedBy(LoginUtil.getLoginId());
            subjectLabel.setCreatedTime(new Date());
            subjectLabel.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
            subjectLabelService.insert(subjectLabel);
        }

        SubjectCategoryLabel relationQuery = new SubjectCategoryLabel();
        relationQuery.setCategoryId(subjectLabelBO.getCategoryId());
        relationQuery.setLabelId(subjectLabel.getId());
        relationQuery.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectCategoryLabel> relationList = subjectCategoryLabelService.queryByCondition(relationQuery);
        if (!CollectionUtils.isEmpty(relationList)) {
            return true;
        }

        SubjectCategoryLabel relation = new SubjectCategoryLabel();
        relation.setCategoryId(subjectLabelBO.getCategoryId());
        relation.setLabelId(subjectLabel.getId());
        relation.setSortNum(nextSortNum);
        relation.setCreatedBy(LoginUtil.getLoginId());
        relation.setCreatedTime(new Date());
        relation.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        int count = subjectCategoryLabelService.insert(relation);
        if (count > 0) {
            cacheUtil.invalidateAll();
        }
        return count > 0;
    }

    @Override
    public Boolean update(SubjectLabelBO subjectLabelBO) {
        if (log.isInfoEnabled()) {
            log.info("SubjectLabelDomainServiceImpl.update.bo:{}", JSON.toJSONString(subjectLabelBO));
        }
        SubjectLabel subjectLabel = SubjectLabelConverter.INSTANCE.convertBoToLabel(subjectLabelBO);
        subjectLabel.setUpdateBy(LoginUtil.getLoginId());
        subjectLabel.setUpdateTime(new Date());
        int count = subjectLabelService.update(subjectLabel);
        if (count > 0) {
            cacheUtil.invalidateAll();
        }
        return count > 0;
    }

    @Override
    public Boolean delete(SubjectLabelBO subjectLabelBO) {
        if (log.isInfoEnabled()) {
            log.info("SubjectLabelDomainServiceImpl.update.bo:{}", JSON.toJSONString(subjectLabelBO));
        }
        SubjectLabel subjectLabel = SubjectLabelConverter.INSTANCE.convertBoToLabel(subjectLabelBO);
        subjectLabel.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        subjectLabel.setUpdateBy(LoginUtil.getLoginId());
        subjectLabel.setUpdateTime(new Date());
        int count = subjectLabelService.update(subjectLabel);

        SubjectCategoryLabel relationQuery = new SubjectCategoryLabel();
        relationQuery.setLabelId(subjectLabelBO.getId());
        relationQuery.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectCategoryLabel> relationList = subjectCategoryLabelService.queryByCondition(relationQuery);
        relationList.forEach(relation -> {
            SubjectCategoryLabel updateRelation = new SubjectCategoryLabel();
            updateRelation.setId(relation.getId());
            updateRelation.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
            updateRelation.setUpdateBy(LoginUtil.getLoginId());
            updateRelation.setUpdateTime(new Date());
            subjectCategoryLabelService.update(updateRelation);
        });
        if (count > 0) {
            cacheUtil.invalidateAll();
        }
        return count > 0;
    }

    @Override
    public List<SubjectLabelBO> queryLabelByCategoryId(SubjectLabelBO subjectLabelBO) {
        SubjectCategory subjectCategory = subjectCategoryService.queryById(subjectLabelBO.getCategoryId());
        Preconditions.checkNotNull(subjectCategory, "分类不存在");

        List<Long> targetCategoryIdList = new LinkedList<>();
        if (Objects.equals(subjectCategory.getCategoryType(), CategoryTypeEnum.PRIMARY.getCode())) {
            SubjectCategory queryCategory = new SubjectCategory();
            queryCategory.setParentId(subjectLabelBO.getCategoryId());
            queryCategory.setCategoryType(CategoryTypeEnum.SECOND.getCode());
            queryCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
            List<SubjectCategory> secondaryCategoryList = subjectCategoryService.queryCategory(queryCategory);
            targetCategoryIdList = secondaryCategoryList.stream().map(SubjectCategory::getId).collect(Collectors.toList());
        } else {
            targetCategoryIdList.add(subjectLabelBO.getCategoryId());
        }

        if (CollectionUtils.isEmpty(targetCategoryIdList)) {
            return Collections.emptyList();
        }

        Map<Long, Integer> labelSortMap = new HashMap<>();
        List<SubjectCategoryLabel> relationList = subjectCategoryLabelService.batchQueryByCategoryIds(targetCategoryIdList);
        if (!CollectionUtils.isEmpty(relationList)) {
            relationList.forEach(relation ->
                    labelSortMap.merge(relation.getLabelId(), relation.getSortNum(), Math::min));
        }

        targetCategoryIdList.forEach(categoryId -> {
            SubjectMapping subjectMapping = new SubjectMapping();
            subjectMapping.setCategoryId(categoryId);
            subjectMapping.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
            List<SubjectMapping> mappingList = subjectMappingService.queryLabelId(subjectMapping);
            if (!CollectionUtils.isEmpty(mappingList)) {
                mappingList.stream()
                        .map(SubjectMapping::getLabelId)
                        .filter(Objects::nonNull)
                        .forEach(labelId -> labelSortMap.putIfAbsent(labelId, Integer.MAX_VALUE));
            }
        });

        if (labelSortMap.isEmpty()) {
            return Collections.emptyList();
        }

        List<SubjectLabel> labelList = subjectLabelService.batchQueryById(new LinkedList<>(labelSortMap.keySet()));
        if (CollectionUtils.isEmpty(labelList)) {
            return Collections.emptyList();
        }

        List<SubjectLabelBO> boList = new LinkedList<>();
        labelList.stream()
                .filter(label -> Objects.equals(label.getIsDeleted(), IsDeletedFlagEnum.UN_DELETED.getCode()))
                .sorted(Comparator
                        .comparing((SubjectLabel label) -> labelSortMap.getOrDefault(label.getId(), Integer.MAX_VALUE))
                        .thenComparing(SubjectLabel::getId))
                .forEach(label -> {
                    SubjectLabelBO bo = new SubjectLabelBO();
                    bo.setId(label.getId());
                    bo.setLabelName(label.getLabelName());
                    bo.setCategoryId(subjectLabelBO.getCategoryId());
                    bo.setSortNum(labelSortMap.getOrDefault(label.getId(), label.getSortNum()));
                    boList.add(bo);
                });
        return boList;
    }

    private SubjectLabel getExistingLabel(String labelName) {
        SubjectLabel queryLabel = new SubjectLabel();
        queryLabel.setLabelName(labelName);
        queryLabel.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectLabel> existingLabelList = subjectLabelService.queryByCondition(queryLabel);
        if (CollectionUtils.isEmpty(existingLabelList)) {
            return null;
        }
        return existingLabelList.get(0);
    }

    private Integer getNextSortNum(Long categoryId) {
        SubjectCategoryLabel relationQuery = new SubjectCategoryLabel();
        relationQuery.setCategoryId(categoryId);
        relationQuery.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectCategoryLabel> relationList = subjectCategoryLabelService.queryByCondition(relationQuery);
        if (CollectionUtils.isEmpty(relationList)) {
            return 1;
        }
        return relationList.stream()
                .map(SubjectCategoryLabel::getSortNum)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }
}
