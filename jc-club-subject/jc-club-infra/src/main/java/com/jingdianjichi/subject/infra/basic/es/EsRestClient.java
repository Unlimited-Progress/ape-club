package com.jingdianjichi.subject.infra.basic.es;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class EsRestClient {

//    RestHighLevelClient是Elasticsearch的Java高级REST客户端，
//    用于连接和与Elasticsearch服务交互。这个客户端提供了一种面向对象的API，
//    使得Java开发者可以更容易地操作Elasticsearch，而无需手动构造HTTP请求。

//    RestHighLevelClient适用于实时日志分析和警报系统等场景，其中它可以快速检索特定类型的日志，实时生成警报和通知
    // 定义一个静态Map，用于存储不同Elasticsearch集群配置名称对应的RestHighLevelClient实例
    public static Map<String, RestHighLevelClient> clientMap = new HashMap<>();

//    COMMON_OPTIONS 是一个 RequestOptions 对象，包含了一些通用的配置选项，如超时时间、重试策略等。
    private static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }

    @Resource
    private EsConfigProperties esConfigProperties;
    // 使用@PostConstruct注解，表示在依赖注入完成后，立即执行此方法进行初始化操作
    @PostConstruct
    public void initialize() {
        List<EsClusterConfig> esClusterConfigs = esConfigProperties.getEsClusterConfigs();
        // 从esConfigProperties中获取所有的Elasticsearch集群配置列表
        for (EsClusterConfig esClusterConfig : esClusterConfigs) {
            log.info("initalize.config.name:{},node:{}", esClusterConfig.getName(), esClusterConfig.getNodes());
            // 调用initRestClient方法，根据集群配置创建一个RestHighLevelClient实例
            RestHighLevelClient restHighLevelClient = initRestClient(esClusterConfig);

            if (restHighLevelClient != null) {
                clientMap.put(esClusterConfig.getName(), restHighLevelClient);
            } else {
                log.info("config.name:{},node:{}.initError", esClusterConfig.getName(), esClusterConfig.getNodes());
            }
        }

    }

    public RestHighLevelClient initRestClient(EsClusterConfig esClusterConfig) {
        // 将集群配置中的节点信息（IP地址和端口）按照逗号分隔，得到一个字符串数组
        String[] ipPostArr = esClusterConfig.getNodes().split(",");
        // 创建一个HttpHost列表，用于存储解析后的IP地址和端口信息
        List<HttpHost> httpHostList = new ArrayList<>(ipPostArr.length);

        for (String ipPost : ipPostArr) {
            String[] ipPostInfo = ipPost.split(":");
            // 如果分割后的数组长度为2，说明IP地址和端口格式正确
            if (ipPostInfo.length == 2) {
                // 创建一个HttpHost实例，使用解析后的IP地址和端口信息
                HttpHost httpHost = new HttpHost(ipPostInfo[0], NumberUtils.toInt(ipPostInfo[1]));
                httpHostList.add(httpHost);
            }
        }
//        RestClientBuilder 的 builder 方法需要一个 HttpHost 类型的数组作为参数来构建 RestHighLevelClient 实例。
        HttpHost[] httpHosts = new HttpHost[httpHostList.size()];
//        将 httpHostList 列表中的元素转换为一个数组，并将结果存储在 httpHosts 数组中。
//        这样做的目的是将 httpHostList 中的 HttpHost 对象传递给 RestClientBuilder，
//        以便它可以使用这些对象来建立与 Elasticsearch 集群的连接
        httpHostList.toArray(httpHosts);

        // 使用RestClientBuilder构建一个RestHighLevelClient实例，传入解析后的HttpHost数组
        RestClientBuilder builder = RestClient.builder(httpHosts);
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(builder);

        return restHighLevelClient;
    }
//    getClient(esIndexInfo.getClusterName())：
//    调用 getClient 方法，传入 esIndexInfo 对象的 clusterName 属性值，
//    以获取对应的 RestHighLevelClient 实例。这个实例用于与 Elasticsearch 集群进行通信。
    private static RestHighLevelClient getClient(String clusterName){return clientMap.get(clusterName);}

    public static boolean insertDoc(EsIndexInfo esIndexInfo, EsSourceData esSourceData) {
        try {
            IndexRequest indexRequest = new IndexRequest(esIndexInfo.getIndexName());
            indexRequest.source(esSourceData.getData());
            indexRequest.id(esSourceData.getDocId());
//            indexRequest 是一个 IndexRequest 对象，包含了要插入的文档信息
//            index 方法是 RestHighLevelClient 类的一个实例方法，用于将指定的文档插入到 Elasticsearch 索引中
            getClient(esIndexInfo.getClusterName()).index(indexRequest, COMMON_OPTIONS);
            return true;
        } catch (Exception e) {
            log.error("insertDoc.exception:{}", e.getMessage(), e);
        }
        return false;
    }

    public static boolean updateDoc(EsIndexInfo esIndexInfo, EsSourceData esSourceData) {
        try {
            UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.index(esIndexInfo.getIndexName());
            updateRequest.id(esSourceData.getDocId());
            updateRequest.doc(esSourceData.getData());
            getClient(esIndexInfo.getClusterName()).update(updateRequest, COMMON_OPTIONS);
            return true;
        } catch (Exception e) {
            log.error("updateDoc.exception:{}", e.getMessage(), e);
        }
        return false;
    }

    public static boolean batchUpdateDoc(EsIndexInfo esIndexInfo,
                                         List<EsSourceData> esSourceDataList) {
        try {
            boolean flag =false;
            BulkRequest bulkRequest = new BulkRequest();
            for (EsSourceData esSourceData : esSourceDataList) {
                String docId = esSourceData.getDocId();
                if (StringUtils.isNotBlank(docId)){
                    UpdateRequest updateRequest = new UpdateRequest();
                    updateRequest.index(esIndexInfo.getIndexName());
                    updateRequest.id(esSourceData.getDocId());
                    updateRequest.doc(esSourceData.getData());
                    bulkRequest.add(updateRequest);
                    flag =true;
                }
            }
            if (flag) {
                BulkResponse bulk = getClient(esIndexInfo.getClusterName()).bulk(bulkRequest, COMMON_OPTIONS);
                //操作失败
                if (bulk.hasFailures()) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.error("updateDoc.exception:{}", e.getMessage(), e);
        }
        return false;
    }

    public static boolean delete(EsIndexInfo esIndexInfo) {
        try {
            DeleteByQueryRequest deleteByQueryRequest =
                    new DeleteByQueryRequest(esIndexInfo.getIndexName());
//            全部删除
            deleteByQueryRequest.setQuery(QueryBuilders.matchAllQuery());
            BulkByScrollResponse response = getClient(esIndexInfo.getClusterName()).deleteByQuery(
                    deleteByQueryRequest, COMMON_OPTIONS
            );
            long deleted = response.getDeleted();
            log.info("deleted.size:{}", deleted);
            return true;
        } catch (Exception e) {
            log.error("delete.exception:{}", e.getMessage(), e);
        }
        return false;
    }

    public static boolean deleteDoc(EsIndexInfo esIndexInfo, String docId) {
        try {
            DeleteRequest deleteRequest = new DeleteRequest(esIndexInfo.getIndexName());
            deleteRequest.id(docId);
            DeleteResponse response = getClient(esIndexInfo.getClusterName()).delete(deleteRequest, COMMON_OPTIONS);
            log.info("deleteDoc.response:{}", JSON.toJSONString(response));
            return true;
        } catch (Exception e) {
            log.error("deleteDoc.exception:{}", e.getMessage(), e);
        }
        return false;
    }

    public static boolean isExistDocById(EsIndexInfo esIndexInfo, String docId) {
        try {
            GetRequest getRequest = new GetRequest(esIndexInfo.getIndexName());
            getRequest.id(docId);
            return getClient(esIndexInfo.getClusterName()).exists(getRequest, COMMON_OPTIONS);
        } catch (Exception e) {
            log.error("isExistDocById.exception:{}", e.getMessage(), e);
        }
        return false;
    }

    public static Map<String, Object> getDocById(EsIndexInfo esIndexInfo, String docId) {
        try {
            GetRequest getRequest = new GetRequest(esIndexInfo.getIndexName());
            getRequest.id(docId);
            GetResponse response = getClient(esIndexInfo.getClusterName()).get(getRequest, COMMON_OPTIONS);
            Map<String, Object> source = response.getSource();
            return source;
        } catch (Exception e) {
            log.error("isExistDocById.exception:{}", e.getMessage(), e);
        }
        return null;
    }



    public static Map<String, Object> getDocById(EsIndexInfo esIndexInfo, String docId,
                                                 String[] fields) {
        try {
            GetRequest getRequest = new GetRequest(esIndexInfo.getIndexName());
            getRequest.id(docId);
//            true：表示是否应返回文档的源内容。如果为true，则返回文档的源内容；如果为false，则不返回。
//            fields：这是一个字符串数组，指定要从搜索结果中返回哪些字段。如果设置为null，则返回所有字段。
//            null：这是一个可选参数，用于指定一个过滤器，以便仅返回满足特定条件的字段。在这个例子中，它被设置为null，表示没有过滤器。
            FetchSourceContext fetchSourceContext = new FetchSourceContext(true, fields, null);
            getRequest.fetchSourceContext(fetchSourceContext);
            GetResponse response = getClient(esIndexInfo.getClusterName()).get(getRequest, COMMON_OPTIONS);
            Map<String, Object> source = response.getSource();
            return source;
        } catch (Exception e) {
            log.error("isExistDocById.exception:{}", e.getMessage(), e);
        }
        return null;
    }

    public static SearchResponse searchWithTermQuery(EsIndexInfo esIndexInfo,
                                                     EsSearchRequest esSearchRequest) {
        try {
            BoolQueryBuilder bq = esSearchRequest.getBq();
            String[] fields = esSearchRequest.getFields();
            int from = esSearchRequest.getFrom();
            int size = esSearchRequest.getSize();
            Long minutes = esSearchRequest.getMinutes();
            Boolean needScroll = esSearchRequest.getNeedScroll();
            String sortName = esSearchRequest.getSortName();
            SortOrder sortOrder = esSearchRequest.getSortOrder();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(bq);
            // 设置返回字段和分页信息
            searchSourceBuilder.fetchSource(fields, null).from(from).size(size);

            if (Objects.nonNull(esSearchRequest.getHighlightBuilder())) {
                searchSourceBuilder.highlighter(esSearchRequest.getHighlightBuilder());
            }

            // 如果存在排序字段名，则设置排序
            if (StringUtils.isNotBlank(sortName)) {
                searchSourceBuilder.sort(sortName);
            }

            searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));

            SearchRequest searchRequest = new SearchRequest();
            searchRequest.searchType(SearchType.DEFAULT);
            searchRequest.indices(esIndexInfo.getIndexName());
            searchRequest.source(searchSourceBuilder);
            // 如果需要滚动，则设置滚动时间
//            当查询结果集较大时，一次性返回所有结果可能会导致性能问题。为了解决这个问题，
//            Elasticsearch提供了滚动API，允许你分批次地获取大量数据。
            if (needScroll) {
                Scroll scroll = new Scroll(TimeValue.timeValueMinutes(minutes));
                searchRequest.scroll(scroll);
            }
            SearchResponse search = getClient(esIndexInfo.getClusterName()).search(searchRequest, COMMON_OPTIONS);
            return search;
        } catch (Exception e) {
            log.error("searchWithTermQuery.exception:{}", e.getMessage(), e);
        }
        return null;
    }

    public static boolean batchInsertDoc(EsIndexInfo esIndexInfo, List<EsSourceData> esSourceDataList) {
        if (log.isInfoEnabled()) {
            log.info("批量新增ES:" + esSourceDataList.size());
            log.info("indexName:" + esIndexInfo.getIndexName());
        }
        try {
            boolean flag = false;
            BulkRequest bulkRequest = new BulkRequest();

            for (EsSourceData source : esSourceDataList) {
                String docId = source.getDocId();
                if (StringUtils.isNotBlank(docId)) {
                    IndexRequest indexRequest = new IndexRequest(esIndexInfo.getIndexName());
                    indexRequest.id(docId);
                    indexRequest.source(source.getData());
                    bulkRequest.add(indexRequest);
                    flag = true;
                }
            }

            if (flag) {
                BulkResponse response = getClient(esIndexInfo.getClusterName()).bulk(bulkRequest, COMMON_OPTIONS);
                if (response.hasFailures()) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("batchInsertDoc.error", e);
        }

        return true;
    }

//    Script script：一个用于更新文档的脚本对象。
//    int batchSize：每次批量更新的文档数量。
    public static boolean updateByQuery(EsIndexInfo esIndexInfo, QueryBuilder queryBuilder, Script script, int batchSize) {
        if (log.isInfoEnabled()) {
            log.info("updateByQuery.indexName:" + esIndexInfo.getIndexName());
        }
        try {
            UpdateByQueryRequest updateByQueryRequest = new UpdateByQueryRequest(esIndexInfo.getIndexName());
            updateByQueryRequest.setQuery(queryBuilder);
            updateByQueryRequest.setScript(script);
            updateByQueryRequest.setBatchSize(batchSize);
//            设置abortOnVersionConflict为false，表示在版本冲突时不中止操作。
            updateByQueryRequest.setAbortOnVersionConflict(false);
            BulkByScrollResponse response = getClient(esIndexInfo.getClusterName()).updateByQuery(updateByQueryRequest, RequestOptions.DEFAULT);
//            从响应中获取失败的批量操作列表。
            List<BulkItemResponse.Failure> failures = response.getBulkFailures();
        } catch (Exception e) {
            log.error("updateByQuery.error", e);
        }
        return true;
    }



}
