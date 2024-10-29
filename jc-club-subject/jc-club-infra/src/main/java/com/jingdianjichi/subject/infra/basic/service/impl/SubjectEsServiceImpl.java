package com.jingdianjichi.subject.infra.basic.service.impl;

import com.jingdianjichi.subject.common.entity.PageResult;
import com.jingdianjichi.subject.infra.basic.entity.EsSubjectFields;
import com.jingdianjichi.subject.infra.basic.entity.SubjectInfoEs;
import com.jingdianjichi.subject.infra.basic.es.EsIndexInfo;
import com.jingdianjichi.subject.infra.basic.es.EsRestClient;
import com.jingdianjichi.subject.infra.basic.es.EsSearchRequest;
import com.jingdianjichi.subject.infra.basic.es.EsSourceData;
import com.jingdianjichi.subject.infra.basic.service.SubjectEsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Slf4j
public class SubjectEsServiceImpl implements SubjectEsService {


    @Override
    public boolean insert(SubjectInfoEs subjectInfoEs) {
        EsSourceData esSourceData = new EsSourceData();
        Map<String, Object> data = convert2EsSourceData(subjectInfoEs);
        esSourceData.setDocId(subjectInfoEs.getDocId().toString());
        esSourceData.setData(data);

        return EsRestClient.insertDoc(getEsIndexInfo(),esSourceData);
    }

    @Override
    public PageResult<SubjectInfoEs> querySubjectList(SubjectInfoEs req) {
        // 创建分页结果对象
        PageResult<SubjectInfoEs> pageResult = new PageResult<>();
        // 根据请求参数构建查询条件
        EsSearchRequest esSearchRequest = createSearchListQuery(req);
        // 执行查询并获取响应
        SearchResponse searchResponse = EsRestClient.searchWithTermQuery(getEsIndexInfo(), esSearchRequest);

        // 初始化结果列表
        List<SubjectInfoEs> subjectInfoEsList = new LinkedList<>();
        // 获取搜索结果
        SearchHits searchHits = searchResponse.getHits();
        // 如果没有搜索结果，设置分页信息并返回空结果
        if (searchHits == null || searchHits.getHits() == null){
            pageResult.setPageNo(req.getPageNo());
            pageResult.setPageSize(req.getPageSize());
            pageResult.setRecords(subjectInfoEsList);
            pageResult.setTotal(0);
            return pageResult;
        }
        // 获取搜索命中的结果数组
        SearchHit[] hits = searchHits.getHits();
        // 遍历结果数组，将每个结果转换为SubjectInfoEs对象并添加到结果列表中
        for (SearchHit hit : hits) {
            SubjectInfoEs subjectInfoEs =  convertResult(hit);
            if (Objects.nonNull(subjectInfoEs)){
                subjectInfoEsList.add(subjectInfoEs);
            }
        }

        // 设置分页信息并返回结果
        pageResult.setPageNo(req.getPageNo());
        pageResult.setPageSize(req.getPageSize());
        pageResult.setRecords(subjectInfoEsList);
        pageResult.setTotal(Long.valueOf(searchHits.getTotalHits().value).intValue());
        return pageResult;
    }

    private SubjectInfoEs convertResult(SearchHit hit) {
        // 从搜索命中结果中获取源数据映射
//        结合HighlightBuilder，可以实现搜索结果的高亮显示，并通过hit.getSourceAsMap()获取高亮后的文本
        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
        // 如果源数据映射为空，返回null
        if (CollectionUtils.isEmpty(sourceAsMap)){
            return null;
        }
        // 创建SubjectInfoEs对象并设置属性值
        SubjectInfoEs result = new SubjectInfoEs();
//        从 sourceAsMap 这个映射中获取键为 EsSubjectFields.SUBJECT_ID 的值，并将其转换为 long 类型
        result.setSubjectId(MapUtils.getLong(sourceAsMap,EsSubjectFields.SUBJECT_ID));
        result.setSubjectName(MapUtils.getString(sourceAsMap, EsSubjectFields.SUBJECT_NAME));
        result.setSubjectAnswer(MapUtils.getString(sourceAsMap, EsSubjectFields.SUBJECT_ANSWER));
        result.setDocId(MapUtils.getLong(sourceAsMap, EsSubjectFields.DOC_ID));
        result.setSubjectType(MapUtils.getInteger(sourceAsMap, EsSubjectFields.SUBJECT_TYPE));
        // 计算得分（分值越大，越准确）
        result.setScore(new BigDecimal(String.valueOf(hit.getScore())).multiply(new BigDecimal("100.00")
                .setScale(2, RoundingMode.HALF_UP)));
        //                        setScale(2) 表示将结果保留两位小数。
//                        RoundingMode.HALF_UP 是一个枚举值，表示使用四舍五入的方式进行舍入。
//                        在这种模式下，如果第三位小数大于等于5，则第二位小数加1；否则保持不变。

        // 处理name的高亮显示
        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        HighlightField subjectNameField = highlightFields.get(EsSubjectFields.SUBJECT_NAME);

        if (Objects.nonNull(subjectNameField)){
//            获取 EsSubjectFields.SUBJECT_NAME 字段的高亮片段
            Text[] fragments = subjectNameField.getFragments();
//            用于构建最终的高亮文本字符串
            StringBuilder subjectNameBuilder = new StringBuilder();
            for (Text fragment : fragments) {
                subjectNameBuilder.append(fragments);
            }
            result.setSubjectName(subjectNameBuilder.toString());
        }

        // 处理答案高亮显示
        HighlightField subjectAnswerField = highlightFields.get(EsSubjectFields.SUBJECT_ANSWER);
        if(Objects.nonNull(subjectAnswerField)){
            Text[] fragments = subjectAnswerField.getFragments();
            StringBuilder subjectAnswerBuilder = new StringBuilder();
            for (Text fragment : fragments) {
                subjectAnswerBuilder.append(fragment);
            }
            result.setSubjectAnswer(subjectAnswerBuilder.toString());
        }

        return result;
    }

    private EsSearchRequest createSearchListQuery(SubjectInfoEs req) {
        // 创建搜索请求对象
        EsSearchRequest esSearchRequest = new EsSearchRequest();
        // 创建布尔查询构建器
        BoolQueryBuilder bq = new BoolQueryBuilder();
        // 创建匹配查询构建器，用于匹配题目名称
        MatchQueryBuilder subjectNameQueryBuilder =
                QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_NAME, req.getKeyWord());
        // 将匹配查询构建器添加到布尔查询构建器中，并设置权重
        bq.should(subjectNameQueryBuilder);
        subjectNameQueryBuilder.boost(2);

        // 创建匹配查询构建器，用于匹配题目答案
        MatchQueryBuilder subjectAnswerQueryBuilder =
                QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_ANSWER, req.getSubjectAnswer());
        // 将匹配查询构建器添加到布尔查询构建器中
        bq.should(subjectAnswerQueryBuilder);

        // 创建匹配查询构建器，用于匹配题目类型
        MatchQueryBuilder subjectTypeQueryBuilder =
                QueryBuilders.matchQuery(EsSubjectFields.SUBJECT_TYPE, req.getSubjectType());
        // 将匹配查询构建器添加到布尔查询构建器中，并设置为必须匹配的条件
        bq.must(subjectTypeQueryBuilder);
        // 设置至少匹配一个条件
        bq.minimumShouldMatch(1);

        // 创建高亮构建器，并设置高亮的字段和样式
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("*").requireFieldMatch(false);
//        .field("*")：设置要高亮的字段，这里使用通配符"*"表示所有字段。
//.requireFieldMatch(false)：设置是否需要匹配字段，这里设置为false表示不需要匹配字段，即只要包含搜索关键词的字段都会被高亮。
        highlightBuilder.preTags("<span style = \"color:red\">");
        highlightBuilder.postTags("</span>");

        // 将布尔查询构建器、高亮构建器和其他参数设置到搜索请求对象中
        esSearchRequest.setBq(bq);
        esSearchRequest.setHighlightBuilder(highlightBuilder);
        esSearchRequest.setFields(EsSubjectFields.FIELD_QUERY);
        esSearchRequest.setFrom((req.getPageNo()-1)*req.getPageSize());
        esSearchRequest.setSize(req.getPageSize());
        esSearchRequest.setNeedScroll(false);
        return esSearchRequest;
    }


    private Map<String, Object> convert2EsSourceData(SubjectInfoEs subjectInfoEs) {
        Map<String, Object> data = new HashMap<>();
        data.put(EsSubjectFields.SUBJECT_ID, subjectInfoEs.getSubjectId());
        data.put(EsSubjectFields.DOC_ID, subjectInfoEs.getDocId());
        data.put(EsSubjectFields.SUBJECT_NAME, subjectInfoEs.getSubjectName());
        data.put(EsSubjectFields.SUBJECT_ANSWER, subjectInfoEs.getSubjectAnswer());
        data.put(EsSubjectFields.SUBJECT_TYPE, subjectInfoEs.getSubjectType());
        data.put(EsSubjectFields.CREATE_USER, subjectInfoEs.getCreateUser());
        data.put(EsSubjectFields.CREATE_TIME, subjectInfoEs.getCreateTime());
        return data;
    }

    private EsIndexInfo getEsIndexInfo() {
        EsIndexInfo esIndexInfo = new EsIndexInfo();
        esIndexInfo.setClusterName("73438a827b55");
        esIndexInfo.setIndexName("subject_index");
        return esIndexInfo;
    }


}
