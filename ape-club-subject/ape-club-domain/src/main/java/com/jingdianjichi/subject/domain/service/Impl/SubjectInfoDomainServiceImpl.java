package com.jingdianjichi.subject.domain.service.Impl;

import com.alibaba.fastjson.JSON;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jingdianjichi.subject.common.entity.PageResult;
import com.jingdianjichi.subject.common.enums.IsDeletedFlagEnum;

import com.jingdianjichi.subject.common.util.IdWorkerUtil;
import com.jingdianjichi.subject.common.util.LoginUtil;
import com.jingdianjichi.subject.domain.convert.SubjectInfoConverter;
import com.jingdianjichi.subject.domain.entity.SubjectInfoBO;

import com.jingdianjichi.subject.domain.entity.SubjectOptionBO;
import com.jingdianjichi.subject.domain.handler.subject.SubjectTypeHandler;
import com.jingdianjichi.subject.domain.handler.subject.SubjectTypeHandlerFactory;
import com.jingdianjichi.subject.domain.redis.RedisUtil;
import com.jingdianjichi.subject.domain.service.SubjectInfoDomainService;

import com.jingdianjichi.subject.domain.service.SubjectLikedDomainService;
import com.jingdianjichi.subject.infra.basic.entity.SubjectInfo;
import com.jingdianjichi.subject.infra.basic.entity.SubjectInfoEs;
import com.jingdianjichi.subject.infra.basic.entity.SubjectLabel;
import com.jingdianjichi.subject.infra.basic.entity.SubjectMapping;
import com.jingdianjichi.subject.infra.basic.service.SubjectEsService;
import com.jingdianjichi.subject.infra.basic.service.SubjectInfoService;
import com.jingdianjichi.subject.infra.basic.service.SubjectLabelService;
import com.jingdianjichi.subject.infra.basic.service.SubjectMappingService;

import com.jingdianjichi.subject.infra.entity.UserInfo;
import com.jingdianjichi.subject.infra.rpc.UserRpc;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubjectInfoDomainServiceImpl implements SubjectInfoDomainService {

    @Resource
    private SubjectInfoService subjectInfoService;

    @Resource
    private SubjectMappingService subjectMappingService;

    @Resource
    private SubjectLabelService subjectLabelService;

    @Resource
    private SubjectTypeHandlerFactory subjectTypeHandlerFactory;

    @Resource
    private SubjectEsService subjectEsService;


    @Resource
    private SubjectLikedDomainService subjectLikedDomainService;

    @Resource
    private UserRpc userRpc;

    @Resource
    private RedisUtil redisUtil;

    private static final String RANK_KEY = "subject_rank";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SubjectInfoBO subjectInfoBO) {
        if (log.isInfoEnabled()) {
            log.info("SubjectInfoDomainServiceImpl.add.bo:{}", JSON.toJSONString(subjectInfoBO));
        }
        //上一个工厂＋策略的模式
        //一个工厂包含4种类型 ，根据传入的type自动映射选择处理

        SubjectInfo subjectInfo = SubjectInfoConverter.INSTANCE.convertBoToInfo(subjectInfoBO);
        subjectInfo.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        subjectInfoService.insert(subjectInfo);

        SubjectTypeHandler handler = subjectTypeHandlerFactory.getHandler(subjectInfo.getSubjectType());
        handler.add(subjectInfoBO);
        List<Integer> categoryIds = subjectInfoBO.getCategoryIds();
        List<Integer> labelIds = subjectInfoBO.getLabelIds();
        List<SubjectMapping> mappingList = new LinkedList<>();
        categoryIds.forEach(categoryId -> {
            labelIds.forEach(labelId -> {
                SubjectMapping subjectMapping = new SubjectMapping();
                subjectMapping.setSubjectId(subjectInfo.getId());
                subjectMapping.setCategoryId(Long.valueOf(categoryId));
                subjectMapping.setLabelId(Long.valueOf(labelId));
                subjectMapping.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
                mappingList.add(subjectMapping);
            });
        });
        subjectMappingService.batchInsert(mappingList);

        //同步到Elasticsearch 可以快速地存储、搜索和分析大量的数据
        SubjectInfoEs subjectInfoEs = new SubjectInfoEs();
        subjectInfoEs.setDocId(new IdWorkerUtil(1, 1, 1).nextId());
        subjectInfoEs.setSubjectId(subjectInfo.getId());
        subjectInfoEs.setSubjectAnswer(subjectInfoBO.getSubjectAnswer());
        subjectInfoEs.setCreateTime(new Date().getTime());
        subjectInfoEs.setCreateUser("ape");
        subjectInfoEs.setSubjectName(subjectInfo.getSubjectName());
        subjectInfoEs.setSubjectType(subjectInfo.getSubjectType());
        subjectEsService.insert(subjectInfoEs);

        //redis放入zadd计入排行榜

//        RANK_KEY：这是有序集合的键名，标识了你要操作的特定有序集合。
//        LoginUtil.getLoginId()：这是一个方法调用，可能返回当前登录用户的唯一标识符（ID）。这个 ID 将作为有序集合中的成员。
//        1：这是要为该成员增加的分数。如果该成员已经存在于有序集合中，它的现有分数将增加 1；如果不存在，将以 1 作为初始分数添加该成员。

        redisUtil.addScore(RANK_KEY, LoginUtil.getLoginId(), 1);
    }

    /**
     * 查询题目列表
     */
    @Override
    public PageResult<SubjectInfoBO> getSubjectPage(SubjectInfoBO subjectInfoBO) {
        PageResult<SubjectInfoBO> subjectInfoBOPageResult = new PageResult<>();

        subjectInfoBOPageResult.setPageNo(subjectInfoBO.getPageNo());
        subjectInfoBOPageResult.setPageSize(subjectInfoBO.getPageSize());

        int start = (subjectInfoBO.getPageNo() - 1) * subjectInfoBO.getPageSize();
        SubjectInfo subjectInfo = SubjectInfoConverter.INSTANCE.convertBoToInfo(subjectInfoBO);
        int count = subjectInfoService.countByCondition(subjectInfo, subjectInfoBO.getLabelId(), subjectInfoBO.getCategoryId());
        if (count == 0) {
            return subjectInfoBOPageResult;
        }

        List<SubjectInfo> subjectInfoList = subjectInfoService.queryPage(subjectInfo, subjectInfoBO.getCategoryId(), subjectInfoBO.getLabelId(), start
                , subjectInfoBO.getPageSize());

        List<SubjectInfoBO> subjectInfoBOS = SubjectInfoConverter.INSTANCE.convertListInfoToBO(subjectInfoList);
        subjectInfoBOS.forEach(info -> {
            SubjectMapping subjectMapping = new SubjectMapping();
            subjectMapping.setSubjectId(info.getId());
            List<SubjectMapping> mappingList = subjectMappingService.queryLabelId(subjectMapping);
            List<Long> labelIds = mappingList.stream().map(SubjectMapping::getLabelId).collect(Collectors.toList());
            List<SubjectLabel> labelList = subjectLabelService.batchQueryById(labelIds);
            List<String> labelNames = labelList.stream().map(SubjectLabel::getLabelName).collect(Collectors.toList());
            info.setLabelName(labelNames);
        });
        subjectInfoBOPageResult.setRecords(subjectInfoBOS);
        subjectInfoBOPageResult.setTotal(count);

        return subjectInfoBOPageResult;
    }


    /**
     * 查询题目详细信息
     *
     * @param subjectInfoBO
     * @return
     */
    @Override
    public SubjectInfoBO querySubjectInfo(SubjectInfoBO subjectInfoBO) {
        //查询info
        SubjectInfo subjectInfo = subjectInfoService.queryById(subjectInfoBO.getId());
        BeanUtils.copyProperties(subjectInfo, subjectInfoBO);
        //利用工厂来实现查询答案
        SubjectTypeHandler handler = subjectTypeHandlerFactory.getHandler(subjectInfo.getSubjectType());
        SubjectOptionBO optionBO = handler.query(subjectInfo.getId().intValue());
        //copy属性
        SubjectInfoBO subjectInfoBO1 = new SubjectInfoBO();
        BeanUtils.copyProperties(optionBO, subjectInfoBO1);
        BeanUtils.copyProperties(subjectInfo, subjectInfoBO1);
        //查询mapping表获取labelids
        SubjectMapping subjectMapping = new SubjectMapping();
        subjectMapping.setSubjectId(subjectInfo.getId());
        subjectMapping.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectMapping> mappingList = subjectMappingService.queryLabelId(subjectMapping);

        List<Long> labelIds = mappingList.stream()
                .map(SubjectMapping::getLabelId)
                .collect(Collectors.toList());
        //获取标签名字
        List<SubjectLabel> labelList = subjectLabelService.batchQueryById(labelIds);
        List<String> labelNameList = labelList.stream().map(SubjectLabel::getLabelName).collect(Collectors.toList());
        subjectInfoBO1.setLabelName(labelNameList);

        subjectInfoBO1.setLiked(subjectLikedDomainService.isLiked(subjectInfoBO.getId().toString(), LoginUtil.getLoginId()));
        subjectInfoBO1.setLikedCount(subjectLikedDomainService.getLikedCount(subjectInfoBO.getId().toString()));

        //设置游标，根据输入对象中的条件查询下一个和上一个主题的 ID，并将这些 ID 设置到输出对象中
        assembleSubjectCursor(subjectInfoBO,subjectInfoBO1);
        return subjectInfoBO1;
    }

    /**
     * 全文检索
     * @return
     */
    @Override
    public PageResult<SubjectInfoEs> getSubjectPageBySearch(SubjectInfoBO subjectInfoBO) {
        SubjectInfoEs subjectInfoEs = new SubjectInfoEs();
        subjectInfoEs.setPageNo(subjectInfoBO.getPageNo());
        subjectInfoEs.setPageSize(subjectInfoBO.getPageSize());
        subjectInfoEs.setKeyWord(subjectInfoBO.getKeyWord());
        return subjectEsService.querySubjectList(subjectInfoEs);
    }

    @Override
    public List<SubjectInfoBO> getContributeList() {
//         从 Redis 中获取排名前 6 的成员及其分数
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisUtil.rankWithScore(RANK_KEY, 0, 5);
        if (log.isInfoEnabled()) {
            log.info("getContributeList.typedTuples:{}", JSON.toJSONString(typedTuples));

        }
        if (CollectionUtils.isEmpty(typedTuples)) {
            return Collections.emptyList();
        }

        List<SubjectInfoBO> boList = new LinkedList<>();
        typedTuples.forEach(rank -> {
            SubjectInfoBO subjectInfoBO = new SubjectInfoBO();
            // 设置贡献数量（即分数）
            subjectInfoBO.setSubjectCount(rank.getScore().intValue());
//            getValue()：这个方法返回有序集合中的成员值，返回类型是 String。在这个上下文中，成员值通常是用户的唯一标识符（ID）
            UserInfo userInfo = userRpc.getUserInfo(rank.getValue());
            subjectInfoBO.setCreateUser(userInfo.getUserName());
            subjectInfoBO.setCreateUserAvatar(userInfo.getAvatar());
            boList.add(subjectInfoBO);
        });
        return boList;
    }

    private void assembleSubjectCursor(SubjectInfoBO subjectInfoBO,SubjectInfoBO bo) {
        Long categoryId = subjectInfoBO.getCategoryId();
        Long labelId = subjectInfoBO.getLabelId();
        Long subjectId = subjectInfoBO.getLastSubjectId();

        //与前端做兼容
        if (Objects.isNull(categoryId) || Objects.isNull(labelId)){
            return;
        }

        Long nextSubjectId = subjectInfoService.querySubjectIdCursor(subjectId, categoryId, labelId, 1);
        bo.setNextSubjectId(nextSubjectId);
        Long lastSubjectId = subjectInfoService.querySubjectIdCursor(subjectId, categoryId, labelId, 0);
        bo.setLastSubjectId(lastSubjectId);
    }

}
