package com.mycompany.myapp.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.mycompany.myapp.domain.Region;
import com.mycompany.myapp.repository.RegionRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Region} entity.
 */
public interface RegionSearchRepository extends ElasticsearchRepository<Region, Long>, RegionSearchRepositoryInternal {}

interface RegionSearchRepositoryInternal {
    Stream<Region> search(String query);

    Stream<Region> search(Query query);

    @Async
    void index(Region entity);

    @Async
    void deleteFromIndexById(Long id);
}

class RegionSearchRepositoryInternalImpl implements RegionSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final RegionRepository repository;

    RegionSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, RegionRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Region> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Region> search(Query query) {
        return elasticsearchTemplate.search(query, Region.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Region entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Region.class);
    }
}
