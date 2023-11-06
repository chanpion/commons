package com.chenpp.common.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.HistogramBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.SearchTemplateResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.util.ObjectBuilder;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author April.Chen
 * @date 2023/8/31 3:33 下午
 **/
public class EsClient {
    private static Logger logger = LoggerFactory.getLogger(EsClient.class);

    public static final String ES_ID = "_id";
    public static final String NUMBER_OF_SHARDS = "number_of_shards";
    public static final String NUMBER_OF_REPLICAS = "number_of_replicas";
    public static final String MAX_INNER_RESULT_WINDOW = "max_inner_result_window";

    private ElasticsearchClient esClient;

    public void buildClient() throws IOException {
        // Create the low-level client
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200)).build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        ElasticsearchClient client = new ElasticsearchClient(transport);
        logger.info("elasticsearch info: {}", client.info());
        this.esClient = client;
    }

    public void search() throws IOException {
        SearchResponse<Product> search = esClient.search(s -> s
                        .index("products")
                        .query(q -> q
                                .term(t -> t
                                        .field("name")
                                        .value(v -> v.stringValue("bicycle"))
                                )),
                Product.class);

        for (Hit<Product> hit : search.hits().hits()) {
            processProduct(hit.source());
        }
    }

    public void createIndex(String index, int numberOfShards, int numberOfReplicas) throws IOException {
        esClient.indices().create(c -> c.index(index));

    }

    public <T> void addDoc(String index, T data) {
        try {
            IndexResponse response = esClient.index(i -> i
                    .index(index)
                    .document(data)
            );
            logger.info("Indexed with version " + response.version());
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
                logger.info("Product not found");
            }
            return response.source();
        } catch (IOException e) {
            logger.error("es get doc error", e);
        }
        return null;
    }

    public <T> T getDocById(String index, String docId, Class<T> clazz) throws Exception {
        GetResponse<T> response = esClient.get(g -> g.index(index).id(docId), clazz);

        if (response.found()) {
            return response.source();
        } else {
            logger.info("Product not found");
        }
        return null;
    }

    public <T> List<T> search(String index, String field, String searchText, Class<T> clazz) throws IOException {
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
            logger.info("There are " + total.value() + " results");
        } else {
            logger.info("There are more than " + total.value() + " results");
        }

        return response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    }

    public <T> List<T> search(String index, ObjectBuilder<Query> queryBuilder, Class<T> clazz) {
        try {
            SearchResponse<T> response = esClient.search(s -> s
                            .index(index)
                            .query(q -> queryBuilder),
                    clazz
            );
            return response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("es search error", e);
        }
        return Collections.emptyList();
    }

    public void searchNested() throws Exception {
        //tag::search-nested
        String searchText = "bike";
        double maxPrice = 200.0;

        // Search by product name
        MatchQuery byName = MatchQuery.of(m -> m // <1>
                .field("name")
                .query(searchText)
        ); // <2>

        // Search by max price
        RangeQuery byMaxPrice = RangeQuery.of(r -> r
                .field("price")
                .gte(JsonData.of(maxPrice)) // <3>
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
