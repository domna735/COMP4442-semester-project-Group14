package hk.polyu.comp4442.cloudcompute.service;

import hk.polyu.comp4442.cloudcompute.entity.Task;
import hk.polyu.comp4442.cloudcompute.entity.TaskStatus;
import hk.polyu.comp4442.cloudcompute.exception.TaskNotFoundException;
import hk.polyu.comp4442.cloudcompute.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task create(Task task) {
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        return taskRepository.save(task);
    }

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public Task getById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task update(Long id, Task updatedTask) {
        Task existingTask = getById(id);
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus() == null ? TaskStatus.TODO : updatedTask.getStatus());
        return taskRepository.save(existingTask);
    }

    public void delete(Long id) {
        Task existingTask = getById(id);
        taskRepository.delete(existingTask);
    }
}
