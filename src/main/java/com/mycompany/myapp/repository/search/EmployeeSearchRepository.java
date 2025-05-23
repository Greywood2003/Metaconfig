package com.mycompany.myapp.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.mycompany.myapp.domain.Employee;
import com.mycompany.myapp.repository.EmployeeRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link Employee} entity.
 */
public interface EmployeeSearchRepository extends ElasticsearchRepository<Employee, Long>, EmployeeSearchRepositoryInternal {}

interface EmployeeSearchRepositoryInternal {
    Page<Employee> search(String query, Pageable pageable);

    Page<Employee> search(Query query);

    @Async
    void index(Employee entity);

    @Async
    void deleteFromIndexById(Long id);
}

class EmployeeSearchRepositoryInternalImpl implements EmployeeSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final EmployeeRepository repository;

    EmployeeSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, EmployeeRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Employee> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<Employee> search(Query query) {
        SearchHits<Employee> searchHits = elasticsearchTemplate.search(query, Employee.class);
        List<Employee> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Employee entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Employee.class);
    }
}
