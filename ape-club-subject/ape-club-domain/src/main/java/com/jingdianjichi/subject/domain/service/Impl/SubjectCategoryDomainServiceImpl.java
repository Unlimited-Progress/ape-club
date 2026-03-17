package com.jingdianjichi.subject.domain.service.Impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.jingdianjichi.subject.common.enums.IsDeletedFlagEnum;
import com.jingdianjichi.subject.common.util.LoginUtil;
import com.jingdianjichi.subject.domain.convert.SubjectCategoryConverter;
import com.jingdianjichi.subject.domain.entity.SubjectCategoryBO;
import com.jingdianjichi.subject.domain.entity.SubjectLabelBO;
import com.jingdianjichi.subject.domain.service.SubjectCategoryDomainService;
import com.jingdianjichi.subject.domain.util.CacheUtil;
import com.jingdianjichi.subject.infra.basic.entity.SubjectCategory;
import com.jingdianjichi.subject.infra.basic.entity.SubjectCategoryLabel;
import com.jingdianjichi.subject.infra.basic.entity.SubjectLabel;
import com.jingdianjichi.subject.infra.basic.entity.SubjectMapping;
import com.jingdianjichi.subject.infra.basic.service.SubjectCategoryLabelService;
import com.jingdianjichi.subject.infra.basic.service.SubjectCategoryService;
import com.jingdianjichi.subject.infra.basic.service.SubjectLabelService;
import com.jingdianjichi.subject.infra.basic.service.SubjectMappingService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubjectCategoryDomainServiceImpl implements SubjectCategoryDomainService {

    @Resource
    private SubjectCategoryService subjectCategoryService;

    @Resource
    private SubjectMappingService subjectMappingService;

    @Resource
    private SubjectCategoryLabelService subjectCategoryLabelService;

    @Resource
    private SubjectLabelService subjectLabelService;

    @Resource
    private ThreadPoolExecutor labelThreadPool;

    @Resource
    private CacheUtil cacheUtil;

    @Override
    public void insert(SubjectCategoryBO subjectCategoryBO) {
        if (log.isInfoEnabled()) {
            log.info("SubjectCategoryController.add.bo:{}", JSON.toJSONString(subjectCategoryBO));
        }
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE.convertToCategory(subjectCategoryBO);
        subjectCategory.setCreatedBy(LoginUtil.getLoginId());
        subjectCategory.setCreatedTime(new Date());
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        subjectCategoryService.insert(subjectCategory);
        cacheUtil.invalidateAll();
    }

    @Override
    public List<SubjectCategoryBO> queryCategory(SubjectCategoryBO subjectCategoryBO) {
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE.convertToCategory(subjectCategoryBO);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());

        List<SubjectCategory> categoryList = subjectCategoryService.queryCategory(subjectCategory);
        List<SubjectCategoryBO> boList = SubjectCategoryConverter.INSTANCE.convertCategoryToBO(categoryList);
        if (log.isInfoEnabled()) {
            log.info("subjectCategoryDomainServiceImpl.queryCategory.bolist:{}", boList);
        }
        boList.forEach(bo -> bo.setCount(subjectCategoryService.querySubjectCount(bo.getId())));
        return boList;
    }

    @Override
    public Boolean update(SubjectCategoryBO subjectCategoryBO) {
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE.convertToCategory(subjectCategoryBO);
        subjectCategory.setUpdateBy(LoginUtil.getLoginId());
        subjectCategory.setUpdateTime(new Date());
        int count = subjectCategoryService.update(subjectCategory);
        if (count > 0) {
            cacheUtil.invalidateAll();
        }
        return count > 0;
    }

    @Override
    public Boolean delete(SubjectCategoryBO subjectCategoryBO) {
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE.convertToCategory(subjectCategoryBO);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        subjectCategory.setUpdateBy(LoginUtil.getLoginId());
        subjectCategory.setUpdateTime(new Date());
        int count = subjectCategoryService.update(subjectCategory);
        if (count > 0) {
            cacheUtil.invalidateAll();
        }
        return count > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean move(SubjectCategoryBO subjectCategoryBO) {
        Preconditions.checkNotNull(subjectCategoryBO.getId(), "分类id不能为空");
        Preconditions.checkNotNull(subjectCategoryBO.getParentId(), "目标一级分类不能为空");

        SubjectCategory currentCategory = subjectCategoryService.queryById(subjectCategoryBO.getId());
        Preconditions.checkNotNull(currentCategory, "待移动分类不存在");
        Preconditions.checkArgument(Objects.equals(currentCategory.getCategoryType(), 2), "仅支持移动二级分类");

        SubjectCategory targetPrimaryCategory = subjectCategoryService.queryById(subjectCategoryBO.getParentId());
        Preconditions.checkNotNull(targetPrimaryCategory, "目标一级分类不存在");
        Preconditions.checkArgument(Objects.equals(targetPrimaryCategory.getCategoryType(), 1), "目标分类必须为一级分类");

        SubjectCategory updateCategory = new SubjectCategory();
        updateCategory.setId(currentCategory.getId());
        updateCategory.setParentId(targetPrimaryCategory.getId());
        updateCategory.setUpdateBy(LoginUtil.getLoginId());
        updateCategory.setUpdateTime(new Date());
        int count = subjectCategoryService.update(updateCategory);
        if (count <= 0) {
            return false;
        }
        cacheUtil.invalidateAll();
        return true;
    }

    @Override
    @SneakyThrows
    public List<SubjectCategoryBO> queryCategoryAndLabel(SubjectCategoryBO subjectCategoryBO) {
        Long id = subjectCategoryBO.getId();
        String cacheKey = "categoryAndLabel" + id;
        return cacheUtil.getResult(cacheKey, SubjectCategoryBO.class, key -> getSubjectCategoryBOS(id));
    }

    private List<SubjectCategoryBO> getSubjectCategoryBOS(Long categoryId) {
        SubjectCategory subjectCategory = new SubjectCategory();
        subjectCategory.setParentId(categoryId);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectCategory> subjectCategoryList = subjectCategoryService.queryCategory(subjectCategory);
        if (log.isInfoEnabled()) {
            log.info("SubjectCategoryDomainServiceImpl.queryCategoryAndLabel.subjectCategoryList:{}",
                    JSON.toJSONString(subjectCategoryList));
        }
        List<SubjectCategoryBO> boList = SubjectCategoryConverter.INSTANCE.convertCategoryToBO(subjectCategoryList);
        Map<Long, List<SubjectLabelBO>> labelMap = new HashMap<>();

        List<CompletableFuture<Map<Long, List<SubjectLabelBO>>>> completableFutureList = boList.stream()
                .map(category -> CompletableFuture.supplyAsync(() -> getSubjectLabelBOS(category), labelThreadPool))
                .collect(Collectors.toList());

        completableFutureList.forEach(future -> {
            try {
                Map<Long, List<SubjectLabelBO>> resultMap = future.get();
                labelMap.putAll(resultMap);
            } catch (Exception e) {
                log.error("queryCategoryAndLabel.future.error", e);
            }
        });
        boList.forEach(categoryBO -> categoryBO.setLabelBOList(labelMap.get(categoryBO.getId())));
        return boList;
    }

    private Map<Long, List<SubjectLabelBO>> getSubjectLabelBOS(SubjectCategoryBO bo) {
        List<SubjectLabelBO> labelBOList = buildLabelBOList(Collections.singletonList(bo.getId()));
        if (CollectionUtils.isEmpty(labelBOList)) {
            return Collections.emptyMap();
        }
        Map<Long, List<SubjectLabelBO>> labelMap = new HashMap<>();
        labelMap.put(bo.getId(), labelBOList);
        return labelMap;
    }

    private List<SubjectLabelBO> buildLabelBOList(List<Long> categoryIdList) {
        if (CollectionUtils.isEmpty(categoryIdList)) {
            return Collections.emptyList();
        }

        Map<Long, Integer> labelSortMap = new HashMap<>();
        List<SubjectCategoryLabel> relationList = subjectCategoryLabelService.batchQueryByCategoryIds(categoryIdList);
        if (!CollectionUtils.isEmpty(relationList)) {
            relationList.forEach(relation ->
                    labelSortMap.merge(relation.getLabelId(), relation.getSortNum(), Math::min));
        }

        categoryIdList.forEach(categoryId -> {
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

        List<Long> labelIdList = new ArrayList<>(labelSortMap.keySet());
        List<SubjectLabel> subjectLabels = subjectLabelService.batchQueryById(labelIdList);
        if (CollectionUtils.isEmpty(subjectLabels)) {
            return Collections.emptyList();
        }

        List<SubjectLabelBO> labelBOList = new LinkedList<>();
        subjectLabels.stream()
                .filter(label -> Objects.equals(label.getIsDeleted(), IsDeletedFlagEnum.UN_DELETED.getCode()))
                .sorted(Comparator
                        .comparing((SubjectLabel label) -> labelSortMap.getOrDefault(label.getId(), Integer.MAX_VALUE))
                        .thenComparing(SubjectLabel::getId))
                .forEach(label -> {
                    SubjectLabelBO subjectLabelBO = new SubjectLabelBO();
                    subjectLabelBO.setId(label.getId());
                    subjectLabelBO.setLabelName(label.getLabelName());
                    subjectLabelBO.setCategoryId(categoryIdList.get(0));
                    subjectLabelBO.setSortNum(labelSortMap.getOrDefault(label.getId(), label.getSortNum()));
                    labelBOList.add(subjectLabelBO);
                });
        return labelBOList;
    }
}
