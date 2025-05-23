package com.mycompany.myapp.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.mycompany.myapp.domain.JobHistory;
import com.mycompany.myapp.repository.JobHistoryRepository;
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
 * Spring Data Elasticsearch repository for the {@link JobHistory} entity.
 */
public interface JobHistorySearchRepository extends ElasticsearchRepository<JobHistory, Long>, JobHistorySearchRepositoryInternal {}

interface JobHistorySearchRepositoryInternal {
    Page<JobHistory> search(String query, Pageable pageable);

    Page<JobHistory> search(Query query);

    @Async
    void index(JobHistory entity);

    @Async
    void deleteFromIndexById(Long id);
}

class JobHistorySearchRepositoryInternalImpl implements JobHistorySearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final JobHistoryRepository repository;

    JobHistorySearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, JobHistoryRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<JobHistory> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<JobHistory> search(Query query) {
        SearchHits<JobHistory> searchHits = elasticsearchTemplate.search(query, JobHistory.class);
        List<JobHistory> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(JobHistory entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), JobHistory.class);
    }
}
