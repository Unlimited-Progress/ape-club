package com.jingdianjichi.subject.domain.service.Impl;

import com.alibaba.fastjson.JSON;
import com.jingdianjichi.subject.common.enums.IsDeletedFlagEnum;
import com.jingdianjichi.subject.domain.config.ThreadPoolConfig;
import com.jingdianjichi.subject.domain.convert.SubjectCategoryConverter;
import com.jingdianjichi.subject.domain.convert.SubjectCategoryConverterImpl;
import com.jingdianjichi.subject.domain.entity.SubjectCategoryBO;
import com.jingdianjichi.subject.domain.entity.SubjectLabelBO;
import com.jingdianjichi.subject.domain.service.SubjectCategoryDomainService;
import com.jingdianjichi.subject.domain.util.CacheUtil;
import com.jingdianjichi.subject.infra.basic.entity.SubjectCategory;
import com.jingdianjichi.subject.infra.basic.entity.SubjectLabel;
import com.jingdianjichi.subject.infra.basic.entity.SubjectMapping;
import com.jingdianjichi.subject.infra.basic.service.SubjectCategoryService;
import com.jingdianjichi.subject.infra.basic.service.SubjectLabelService;
import com.jingdianjichi.subject.infra.basic.service.SubjectMappingService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
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
    private SubjectLabelService subjectLabelService;
    @Resource
    private ThreadPoolExecutor labelThreadPool;
    @Resource
    private CacheUtil cacheUtil;


    @Override
    public void insert(SubjectCategoryBO subjectCategoryBO) {
        if (log.isInfoEnabled()){
            log.info("SubjectCategoryController.add.bo:{}", JSON.toJSONString(subjectCategoryBO));
        }
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE
                .convertToCategory(subjectCategoryBO);

        subjectCategoryService.insert(subjectCategory);
    }

    @Override
    public List<SubjectCategoryBO> queryCategory(SubjectCategoryBO subjectCategoryBO) {
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE.
                convertToCategory(subjectCategoryBO);

        subjectCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());

        List<SubjectCategory> categoryList = subjectCategoryService.queryCategory(subjectCategory);
        List<SubjectCategoryBO> boList = SubjectCategoryConverter.INSTANCE.
                convertCategoryToBO(categoryList);

        if (log.isInfoEnabled()){
            log.info("subjectCategoryDomainServiceImpl.queryCategory.bolist:{}",boList);
        }
        boList.forEach(bo -> {
            Integer count = subjectCategoryService.querySubjectCount(bo.getId());
            bo.setCount(count);
        });

        return boList;
    }

    @Override
    public Boolean update(SubjectCategoryBO subjectCategoryBO) {
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE.convertToCategory(subjectCategoryBO);
        int count = subjectCategoryService.update(subjectCategory);
        return count>0;
    }

    @Override
    public Boolean delete(SubjectCategoryBO subjectCategoryBO) {
        SubjectCategory subjectCategory = SubjectCategoryConverter.INSTANCE
                .convertToCategory(subjectCategoryBO);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.DELETED.getCode());
        int count = subjectCategoryService.update(subjectCategory);
        return count > 0;
    }

    /**
     * 查询分类标签及一致性
     */
    @Override
    @SneakyThrows
    public List<SubjectCategoryBO> queryCategoryAndLabel(SubjectCategoryBO subjectCategoryBO) {
        Long id = subjectCategoryBO.getId();
        String cacheKey = "categoryAndLabel"+ id;
//        (key) -> getSubjectCategoryBOS(id)，我们可以将一个函数作为参数传递给另一个函数cacheUtil.getResult()
        List<SubjectCategoryBO> subjectCategoryBOS = cacheUtil.getResult(cacheKey, SubjectCategoryBO.class,
                (key) -> getSubjectCategoryBOS(id));
        return subjectCategoryBOS;
    }

    private List<SubjectCategoryBO> getSubjectCategoryBOS(Long categoryId) {
        SubjectCategory subjectCategory = new SubjectCategory();
        subjectCategory.setParentId(categoryId);
        subjectCategory.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectCategory> subjectCategoryList = subjectCategoryService.queryCategory(subjectCategory);
        if (log.isInfoEnabled()){
            log.info("SubjectCategoryDomainServiceImpl.queryCategoryAndLabel.subjectCategoryList:{}",
                    JSON.toJSONString(subjectCategoryList));
        }
        List<SubjectCategoryBO> boList = SubjectCategoryConverter.INSTANCE.convertCategoryToBO(subjectCategoryList);
        //一次获取标签信息
//        FutureTask用于表示一个异步计算任务的结果
//        当只需要简单地执行一个异步任务并等待其完成时，FutureTask 可能是一个更好的选择
        List<FutureTask<Map<Long,List<SubjectLabelBO>>>> futureTaskList = new LinkedList<>();
        //线程池并发调用
        Map<Long,List<SubjectLabelBO>> map = new HashMap<>();


//        将boList中的每个类别映射为一个CompletableFuture对象。
//        这些CompletableFuture对象将在labelThreadPool线程池中异步执行getSubjectLabelBOS方法，
//        该方法接收一个类别参数并返回一个key是Long类型的categoryId和标签信息列表的Map
        List<CompletableFuture<Map<Long, List<SubjectLabelBO>>>> completableFutureList = boList.
                stream().
                map(category ->
//        CompletableFuture.supplyAsync：创建一个新的 CompletableFuture，并在指定的线程池中异步执行提供的供应商函数。
//        () -> getSubjectLabelBOS(category)：这是一个 lambda 表达式，表示异步执行的任务。
                        CompletableFuture.supplyAsync(() -> getSubjectLabelBOS(category), labelThreadPool))
                .collect(Collectors.toList());

        completableFutureList.forEach(future->{
            try {
                Map<Long, List<SubjectLabelBO>> resultMap = future.get();
                map.putAll(resultMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        boList.forEach(categoryBO->{
            //为什么能拿到？因为我们的key是Long类型的categoryId
            categoryBO.setLabelBOList(map.get(categoryBO.getId()));
        });
//        //把原有的单体的同步的，变成一个向线程池里提交任务的一个过程
//        boList.forEach(bo ->{
//        创建FutureTask的对象（作用管理多线程运行的结果）
//            FutureTask<Map<Long,List<SubjectLabelBO>>>  futureTask = new FutureTask<>(()->
//                    getSubjectLabelBOS(bo) );
//            futureTaskList.add(futureTask);
//            labelThreadPool.getLabelThreadPool().submit(futureTask);
//        });
//        //遍历结果放入map
//        for (FutureTask<Map<Long, List<SubjectLabelBO>>> futureTask : futureTaskList) {
//            Map<Long, List<SubjectLabelBO>> resultMap = futureTask.get();
//            if (CollectionUtils.isEmpty(resultMap)){
//                continue;
//            }
//            map.putAll(resultMap);
//        }
//        boList.forEach(categoryBO->{
//            //为什么能拿到？因为我们的key是Long类型的categoryId
//            categoryBO.setLabelBOList(map.get(categoryBO.getId()));
//        });

        return boList;
    }

    private Map<Long,List<SubjectLabelBO>> getSubjectLabelBOS(SubjectCategoryBO bo) {
        Map<Long,List<SubjectLabelBO>> labelMap = new HashMap<>();
        SubjectMapping subjectMapping = new SubjectMapping();
        subjectMapping.setCategoryId(bo.getId());
        List<SubjectMapping> mappingList = subjectMappingService.queryLabelId(subjectMapping);
        if (CollectionUtils.isEmpty(mappingList)){
            return null;
        }

        List<Long> labelIdList = mappingList.stream()
                .map(SubjectMapping::getLabelId).collect(Collectors.toList());
        List<SubjectLabel> subjectLabels = subjectLabelService.batchQueryById(labelIdList);
        List<SubjectLabelBO> labelBOList = new LinkedList<>();
        subjectLabels.forEach(label->{
            SubjectLabelBO subjectLabelBO = new SubjectLabelBO();
            subjectLabelBO.setId(label.getId());
            subjectLabelBO.setLabelName(label.getLabelName());
            subjectLabelBO.setCategoryId(Long.valueOf(label.getCategoryId()));
            subjectLabelBO.setSortNum(label.getSortNum());
            labelBOList.add(subjectLabelBO);
        });
        labelMap.put(bo.getId(),labelBOList);
        return labelMap;
    }
}
