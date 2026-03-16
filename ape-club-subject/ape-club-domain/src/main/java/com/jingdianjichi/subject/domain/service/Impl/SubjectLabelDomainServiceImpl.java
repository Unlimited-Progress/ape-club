package com.jingdianjichi.subject.domain.service.Impl;

import com.alibaba.fastjson.JSON;
import com.jingdianjichi.subject.common.enums.CategoryTypeEnum;
import com.jingdianjichi.subject.common.enums.IsDeletedFlagEnum;
import com.jingdianjichi.subject.domain.convert.SubjectLabelConverter;
import com.jingdianjichi.subject.domain.entity.SubjectLabelBO;
import com.jingdianjichi.subject.domain.service.SubjectLabelDomainService;
import com.jingdianjichi.subject.infra.basic.entity.SubjectCategory;
import com.jingdianjichi.subject.infra.basic.entity.SubjectLabel;
import com.jingdianjichi.subject.infra.basic.entity.SubjectMapping;
import com.jingdianjichi.subject.infra.basic.service.SubjectCategoryService;
import com.jingdianjichi.subject.infra.basic.service.SubjectLabelService;
import com.jingdianjichi.subject.infra.basic.service.SubjectMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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

    @Override
    public Boolean add(SubjectLabelBO subjectLabelBO) {
        if (log.isInfoEnabled()) {
            log.info("SubjectLabelDomainServiceImpl.add.bo:{}", JSON.toJSONString(subjectLabelBO));
        }
        SubjectLabel subjectLabel = SubjectLabelConverter.INSTANCE
                .convertBoToLabel(subjectLabelBO);
        subjectLabel.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        int count = subjectLabelService.insert(subjectLabel);
        return count > 0;
    }

    @Override
    public Boolean update(SubjectLabelBO subjectLabelBO) {
        if (log.isInfoEnabled()) {
            log.info("SubjectLabelDomainServiceImpl.update.bo:{}", JSON.toJSONString(subjectLabelBO));
        }
        SubjectLabel subjectLabel = SubjectLabelConverter.INSTANCE
                .convertBoToLabel(subjectLabelBO);
        int count = subjectLabelService.update(subjectLabel);
        return count > 0;
    }

    @Override
    public Boolean delete(SubjectLabelBO subjectLabelBO) {
        if (log.isInfoEnabled()) {
            log.info("SubjectLabelDomainServiceImpl.update.bo:{}", JSON.toJSONString(subjectLabelBO));
        }
        SubjectLabel subjectLabel = SubjectLabelConverter.INSTANCE
                .convertBoToLabel(subjectLabelBO);
        subjectLabel.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        int count = subjectLabelService.update(subjectLabel);
        return count > 0;
    }

    @Override
    public List<SubjectLabelBO> queryLabelByCategoryId(SubjectLabelBO subjectLabelBO) {
        // 如果当前分类是1级分类，则查询所有标签
        SubjectCategory subjectCategory = subjectCategoryService.queryById(subjectLabelBO.getCategoryId());
        if (CategoryTypeEnum.PRIMARY.getCode() == subjectCategory.getCategoryType()) {
            // 创建一个新的 SubjectLabel 对象，并设置分类 ID
            SubjectLabel subjectLabel = new SubjectLabel();
            subjectLabel.setCategoryId(subjectLabelBO.getCategoryId().toString());

            // 查询符合条件的所有标签
            List<SubjectLabel> labelList = subjectLabelService.queryByCondition(subjectLabel);

            // 将查询结果转换为 SubjectLabelBO 列表
            List<SubjectLabelBO> labelResultList = SubjectLabelConverter.INSTANCE.convertLabelToBoList(labelList);

            // 返回转换后的标签列表
            return labelResultList;
        }

        // 获取分类 ID
        Long categoryId = subjectLabelBO.getCategoryId();

        // 创建一个新的 SubjectMapping 对象，并设置分类 ID 和未删除标志
        SubjectMapping subjectMapping = new SubjectMapping();
        subjectMapping.setCategoryId(categoryId);
        subjectMapping.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());

        // 查询该分类下的所有标签映射关系
        List<SubjectMapping> mappingList = subjectMappingService.queryLabelId(subjectMapping);

        // 如果映射关系列表为空，返回空列表
        if (CollectionUtils.isEmpty(mappingList)) {
            return Collections.emptyList();
        }

        // 从映射关系列表中提取标签 ID 列表
        List<Long> labelIdList = mappingList.stream().map(SubjectMapping::getLabelId).collect(Collectors.toList());

        // 批量查询标签信息
        List<SubjectLabel> labelList = subjectLabelService.batchQueryById(labelIdList);

        // 创建一个新的 SubjectLabelBO 列表
        List<SubjectLabelBO> boList = new LinkedList<>();

        // 遍历查询到的标签列表，将每个标签转换为 SubjectLabelBO 对象并添加到结果列表中
        labelList.forEach(label -> {
            SubjectLabelBO bo = new SubjectLabelBO();
            bo.setId(label.getId());
            bo.setLabelName(label.getLabelName());
            bo.setCategoryId(categoryId);
            bo.setSortNum(label.getSortNum());
            boList.add(bo);
        });

        // 返回转换后的标签列表
        return boList;
    }
}
