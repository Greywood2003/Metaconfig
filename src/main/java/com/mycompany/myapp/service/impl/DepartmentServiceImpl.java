package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Department;
import com.mycompany.myapp.repository.DepartmentRepository;
import com.mycompany.myapp.repository.search.DepartmentSearchRepository;
import com.mycompany.myapp.service.DepartmentService;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Department}.
 */
@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger LOG = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    private final DepartmentRepository departmentRepository;

    private final DepartmentSearchRepository departmentSearchRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, DepartmentSearchRepository departmentSearchRepository) {
        this.departmentRepository = departmentRepository;
        this.departmentSearchRepository = departmentSearchRepository;
    }

    @Override
    public Department save(Department department) {
        LOG.debug("Request to save Department : {}", department);
        department = departmentRepository.save(department);
        departmentSearchRepository.index(department);
        return department;
    }

    @Override
    public Department update(Department department) {
        LOG.debug("Request to update Department : {}", department);
        department = departmentRepository.save(department);
        departmentSearchRepository.index(department);
        return department;
    }

    @Override
    public Optional<Department> partialUpdate(Department department) {
        LOG.debug("Request to partially update Department : {}", department);

        return departmentRepository
            .findById(department.getId())
            .map(existingDepartment -> {
                if (department.getDepartmentName() != null) {
                    existingDepartment.setDepartmentName(department.getDepartmentName());
                }

                return existingDepartment;
            })
            .map(departmentRepository::save)
            .map(savedDepartment -> {
                departmentSearchRepository.index(savedDepartment);
                return savedDepartment;
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> findAll() {
        LOG.debug("Request to get all Departments");
        return departmentRepository.findAll();
    }

    /**
     *  Get all the departments where JobHistory is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Department> findAllWhereJobHistoryIsNull() {
        LOG.debug("Request to get all departments where JobHistory is null");
        return StreamSupport.stream(departmentRepository.findAll().spliterator(), false)
            .filter(department -> department.getJobHistory() == null)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Department> findOne(Long id) {
        LOG.debug("Request to get Department : {}", id);
        return departmentRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Department : {}", id);
        departmentRepository.deleteById(id);
        departmentSearchRepository.deleteFromIndexById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> search(String query) {
        LOG.debug("Request to search Departments for query {}", query);
        try {
            return StreamSupport.stream(departmentSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
