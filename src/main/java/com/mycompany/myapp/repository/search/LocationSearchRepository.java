package com.mycompany.myapp.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.mycompany.myapp.domain.Location;
import com.mycompany.myapp.repository.LocationRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Location} entity.
 */
public interface LocationSearchRepository extends ElasticsearchRepository<Location, Long>, LocationSearchRepositoryInternal {}

interface LocationSearchRepositoryInternal {
    Stream<Location> search(String query);

    Stream<Location> search(Query query);

    @Async
    void index(Location entity);

    @Async
    void deleteFromIndexById(Long id);
}

class LocationSearchRepositoryInternalImpl implements LocationSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final LocationRepository repository;

    LocationSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, LocationRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Location> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Location> search(Query query) {
        return elasticsearchTemplate.search(query, Location.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Location entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Location.class);
    }
}
