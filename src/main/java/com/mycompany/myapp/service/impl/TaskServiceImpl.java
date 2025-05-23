package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Task;
import com.mycompany.myapp.repository.TaskRepository;
import com.mycompany.myapp.repository.search.TaskSearchRepository;
import com.mycompany.myapp.service.TaskService;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Task}.
 */
@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;

    private final TaskSearchRepository taskSearchRepository;

    public TaskServiceImpl(TaskRepository taskRepository, TaskSearchRepository taskSearchRepository) {
        this.taskRepository = taskRepository;
        this.taskSearchRepository = taskSearchRepository;
    }

    @Override
    public Task save(Task task) {
        LOG.debug("Request to save Task : {}", task);
        task = taskRepository.save(task);
        taskSearchRepository.index(task);
        return task;
    }

    @Override
    public Task update(Task task) {
        LOG.debug("Request to update Task : {}", task);
        task = taskRepository.save(task);
        taskSearchRepository.index(task);
        return task;
    }

    @Override
    public Optional<Task> partialUpdate(Task task) {
        LOG.debug("Request to partially update Task : {}", task);

        return taskRepository
            .findById(task.getId())
            .map(existingTask -> {
                if (task.getTitle() != null) {
                    existingTask.setTitle(task.getTitle());
                }
                if (task.getDescription() != null) {
                    existingTask.setDescription(task.getDescription());
                }

                return existingTask;
            })
            .map(taskRepository::save)
            .map(savedTask -> {
                taskSearchRepository.index(savedTask);
                return savedTask;
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findAll() {
        LOG.debug("Request to get all Tasks");
        return taskRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Task> findOne(Long id) {
        LOG.debug("Request to get Task : {}", id);
        return taskRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Task : {}", id);
        taskRepository.deleteById(id);
        taskSearchRepository.deleteFromIndexById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> search(String query) {
        LOG.debug("Request to search Tasks for query {}", query);
        try {
            return StreamSupport.stream(taskSearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
