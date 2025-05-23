package com.mycompany.myapp.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.mycompany.myapp.domain.Department;
import com.mycompany.myapp.repository.DepartmentRepository;
import java.util.stream.Stream;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Department} entity.
 */
public interface DepartmentSearchRepository extends ElasticsearchRepository<Department, Long>, DepartmentSearchRepositoryInternal {}

interface DepartmentSearchRepositoryInternal {
    Stream<Department> search(String query);

    Stream<Department> search(Query query);

    @Async
    void index(Department entity);

    @Async
    void deleteFromIndexById(Long id);
}

class DepartmentSearchRepositoryInternalImpl implements DepartmentSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final DepartmentRepository repository;

    DepartmentSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, DepartmentRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Stream<Department> search(String query) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery);
    }

    @Override
    public Stream<Department> search(Query query) {
        return elasticsearchTemplate.search(query, Department.class).map(SearchHit::getContent).stream();
    }

    @Override
    public void index(Department entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Department.class);
    }
}
