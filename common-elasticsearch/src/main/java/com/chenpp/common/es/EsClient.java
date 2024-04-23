package com.chenpp.common.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.SearchTemplateResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.util.ObjectBuilder;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author April.Chen
 * @link https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/getting-started-java.html
 * @date 2023/8/31 3:33 下午
 **/
public class EsClient {
    private static Logger logger = LoggerFactory.getLogger(EsClient.class);

    public static final String ES_ID = "_id";
    public static final String NUMBER_OF_SHARDS = "number_of_shards";
    public static final String NUMBER_OF_REPLICAS = "number_of_replicas";
    public static final String MAX_INNER_RESULT_WINDOW = "max_inner_result_window";

    private ElasticsearchClient esClient;


    public void buildClient(ElasticSearchProperties properties) throws IOException {
        this.esClient = ElasticsearchClientFactory.buildClient(properties);
    }

    public ElasticsearchClient getElasticsearchClient(){
        return esClient;
    }
    public void createIndexWithMapping(String indexName, Map<String, Object> mapping) throws IOException {
        CreateIndexResponse createIndexResponse = esClient.indices()
                .create(createIndexRequest ->
                        createIndexRequest.index(indexName)
                                // 用 lambda 的方式 下面的 mapping 会覆盖上面的 mapping
                                .mappings(typeMapping ->
                                        typeMapping.properties("name", objectBuilder ->
                                                objectBuilder.text(textProperty -> textProperty.fielddata(true))
                                        ).properties("age", objectBuilder ->
                                                objectBuilder.integer(integerNumberProperty -> integerNumberProperty.index(true))
                                        ).properties("birthday", objectBuilder -> objectBuilder.date(dateProperty -> dateProperty.format("yyyy-MM-dd HH:mm:ss")))
                                )
                );

        logger.info("== {} 索引创建是否成功: {}", indexName, createIndexResponse.acknowledged());
    }


    public void createIndex(String index, int numberOfShards, int numberOfReplicas) throws IOException {
        esClient.indices().create(c -> c.settings(s -> s.numberOfShards(String.valueOf(numberOfShards)).numberOfReplicas(String.valueOf(numberOfReplicas))).index(index));

    }

    public void existsIndex(String indexName) throws IOException {
        BooleanResponse booleanResponse = esClient.indices()
                .exists(existsRequest ->
                        existsRequest.index(indexName)
                );

        logger.info("== {} 索引创建是否存在: {}", indexName, booleanResponse.value());
    }

    public void indexDetail(String indexName) throws IOException {
        GetIndexResponse getIndexResponse = esClient.indices()
                .get(getIndexRequest ->
                        getIndexRequest.index(indexName)
                );

        Map<String, Property> properties = getIndexResponse.get(indexName).mappings().properties();

        for (String key : properties.keySet()) {
            logger.info("== {} 索引的详细信息为: == key: {}, Property: {}", indexName, key, properties.get(key)._kind());
        }

    }

    public void putMapping(String indexName, Map<String, Property> mapping) throws IOException {
        esClient.indices()
                .putMapping(putMappingRequest ->
                        putMappingRequest.index(indexName)
                                .properties(mapping)
                );
    }


    public void deleteIndex(String indexName) throws IOException {
        DeleteIndexResponse deleteIndexResponse = esClient.indices()
                .delete(deleteIndexRequest ->
                        deleteIndexRequest.index(indexName)
                );

        logger.info("== {} delete index successfully: {}", indexName, deleteIndexResponse.acknowledged());
    }

    public <T> void addDoc(String index, T data) {
        try {
            IndexResponse response = esClient.index(i -> i
                    .index(index)
                    .document(data)
            );
            logger.info("Indexed with version {}", response.version());
        } catch (IOException e) {
            logger.error("es add doc error", e);
        }
    }

    public <T> void addDocs(String index, List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            logger.warn("document list is empty");
            return;
        }
        try {
            BulkRequest.Builder br = new BulkRequest.Builder();
            list.forEach(d -> br.operations(op -> op
                    .index(idx -> idx
                            .index(index)
                            .document(d)
                    )
            ));
            BulkResponse result = esClient.bulk(br.build());
            // Log errors, if any
            if (result.errors()) {
                logger.error("Bulk add docs had errors");
                for (BulkResponseItem item : result.items()) {
                    if (item.error() != null) {
                        logger.error(item.error().reason());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("es add doc list error", e);
        }
    }

    public ObjectNode getDoc(String index, String docId) {
        try {
            GetResponse<ObjectNode> response = esClient.get(g -> g
                            .index(index)
                            .id(docId),
                    ObjectNode.class
            );

            if (!response.found()) {
                logger.info("document not found");
            }
            return response.source();
        } catch (IOException e) {
            logger.error("es get document error", e);
        }
        return null;
    }

    public <T> T getDocById(String index, String docId, Class<T> clazz) throws IOException {
        GetResponse<T> response = esClient.get(g -> g.index(index).id(docId), clazz);
        if (response.found()) {
            return response.source();
        } else {
            logger.info("document not found");
        }
        return null;
    }

    public <T> void updateDoc(String indexName, String docId, T data) throws IOException {
        UpdateResponse<Product> updateResponse = esClient.update(updateRequest ->
                updateRequest.index(indexName).id(docId)
                        .doc(data), Product.class
        );
        logger.info("== response: {}, responseStatus: {}", updateResponse, updateResponse.result());
    }

    public void deleteDoc(String indexName, String docId) throws IOException {
        DeleteResponse deleteResponse = esClient.delete(deleteRequest ->
                deleteRequest.index(indexName).id(docId)
        );
        logger.info("== response: {}, result:{}", deleteResponse, deleteResponse.result());

    }

    public <T> List<T> termQuery(String indexName, String field, String value, Class<T> clazz) throws IOException {
        SearchResponse<T> response = esClient.search(s -> s
                        .index(indexName)
                        .query(q -> q
                                .term(t -> t
                                        .field(field)
                                        .value(v -> v.stringValue(value))
                                )),
                clazz);
        return response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    }

    public <T> List<T> matchQuery(String index, String field, String searchText, Class<T> clazz) throws IOException {
        SearchResponse<T> response = esClient.search(s -> s
                        .index(index)
                        .query(q -> q
                                .match(t -> t
                                        .field(field)
                                        .query(searchText)
                                )
                        ),
                clazz
        );
        TotalHits total = response.hits().total();
        if (total == null) {
            return Collections.emptyList();
        }
        boolean isExactResult = total.relation() == TotalHitsRelation.Eq;

        if (isExactResult) {
            logger.info("There are {} results", total.value());
        } else {
            logger.info("There are more than {} results", total.value());
        }

        return response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    }

    public <T> List<T> search(String index, ObjectBuilder<Query> queryBuilder, Class<T> clazz) {
        try {
            SearchResponse<T> response = esClient.search(s -> s.index(index).query(q -> queryBuilder), clazz);
            return response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("es search error", e);
        }
        return Collections.emptyList();
    }

    public void multipleConditionSearch(String indexName) throws IOException {

        SearchRequest request = SearchRequest.of(searchRequest ->
                searchRequest.index(indexName).from(0).size(20).sort(s -> s.field(f -> f.field("age").order(SortOrder.Desc)))
                        // 如果有多个 .query 后面的 query 会覆盖前面的 query
                        .query(query ->
                                query.bool(boolQuery ->
                                        boolQuery
                                                // 在同一个 boolQuery 中 must 会将 should 覆盖
                                                .must(must -> must.range(
                                                        e -> e.field("age").gte(JsonData.of("21")).lte(JsonData.of("25"))
                                                ))
                                                .mustNot(mustNot -> mustNot.term(
                                                        e -> e.field("name").value(value -> value.stringValue("lisi1"))
                                                ))
                                                .should(must -> must.term(
                                                        e -> e.field("name").value(value -> value.stringValue("lisi2"))
                                                ))
                                )
                        )

        );

        SearchRequest shouldRequest = SearchRequest.of(searchRequest ->
                searchRequest.index(indexName).from(0).size(20).sort(s -> s.field(f -> f.field("age").order(SortOrder.Desc)))
                        .query(query ->
                                query.bool(boolQuery ->
                                        boolQuery
                                                // 两个 should 连用是没有问题的
                                                .should(must -> must.term(
                                                        e -> e.field("age").value(value -> value.stringValue("22"))
                                                ))
                                                .should(must -> must.term(
                                                        e -> e.field("age").value(value -> value.stringValue("23"))
                                                ))
                                )
                        ));


        SearchResponse<Product> searchResponse = esClient.search(request, Product.class);


        logger.info("返回的总条数有：{}", searchResponse.hits().total().value());
        List<Hit<Product>> hitList = searchResponse.hits().hits();
        for (Hit<Product> hit : hitList) {
            logger.info("== hit: {}, id: {}", hit.source(), hit.id());
        }

    }


    public void searchNested() throws Exception {
        //tag::search-nested
        String searchText = "bike";
        double maxPrice = 200.0;

        // Search by product name
        MatchQuery byName = MatchQuery.of(m -> m
                .field("name")
                .query(searchText)
        );

        // Search by max price
        RangeQuery byMaxPrice = RangeQuery.of(r -> r
                .field("price")
                .gte(JsonData.of(maxPrice))
        );

        BoolQuery boolQuery = BoolQuery.of(b -> b.must(builder -> builder.match(byName)).must(builder -> builder.range(byMaxPrice)));

        // Combine name and price queries to search the product index
        SearchResponse<Product> response = esClient.search(s -> s
                        .index("products")
                        .query(q -> q.bool(boolQuery)
                        ),
                Product.class
        );

        List<Hit<Product>> hits = response.hits().hits();
        for (Hit<Product> hit : hits) {
            Product product = hit.source();
            logger.info("Found product " + product.getSku() + ", score " + hit.score());
        }
        //end::search-nested
    }

    public void searchTemplate() throws Exception {


        //tag::search-template-script
        // Create a script
        esClient.putScript(r -> r
                .id("query-script") // <1>
                .script(s -> s
                        .lang("mustache")
                        .source("{\"query\":{\"match\":{\"{{field}}\":\"{{value}}\"}}}")
                ));
        //end::search-template-script

        //tag::search-template-query
        SearchTemplateResponse<Product> response = esClient.searchTemplate(r -> r
                        .index("some-index")
                        .id("query-script") // <1>
                        .params("field", JsonData.of("some-field")) // <2>
                        .params("value", JsonData.of("some-data")),
                Product.class
        );

        List<Hit<Product>> hits = response.hits().hits();
        for (Hit<Product> hit : hits) {
            Product product = hit.source();
            logger.info("Found product " + product.getSku() + ", score " + hit.score());
        }
        //end::search-template-query
    }

    public void priceHistogram() throws Exception {


        //tag::price-histo-request
        String searchText = "bike";

        Query query = MatchQuery.of(m -> m
                .field("name")
                .query(searchText)
        )._toQuery();

        SearchResponse<Void> response = esClient.search(b -> b
                        .index("products")
                        .size(0) // <1>
                        .query(query) // <2>
                        .aggregations("price-histogram", a -> a // <3>
                                .histogram(h -> h // <4>
                                        .field("price")
                                        .interval(50.0)
                                )
                        ),
                Void.class // <5>
        );
        //end::price-histo-request

        //tag::price-histo-response
        List<HistogramBucket> buckets = response.aggregations()
                .get("price-histogram") // <1>
                .histogram() // <2>
                .buckets().array(); // <3>

        for (HistogramBucket bucket : buckets) {
            logger.info("There are " + bucket.docCount() +
                    " bikes under " + bucket.key());
        }

        //end::price-histo-response
    }

    private void processProduct(Product source) {
    }

    @Data
    public static class Product {
        private String sku;
        private String name;
    }
}
