package hk.polyu.comp4442.cloudcompute.service;

import hk.polyu.comp4442.cloudcompute.entity.AppUser;
import hk.polyu.comp4442.cloudcompute.entity.Task;
import hk.polyu.comp4442.cloudcompute.entity.TaskStatus;
import hk.polyu.comp4442.cloudcompute.exception.TaskNotFoundException;
import hk.polyu.comp4442.cloudcompute.repository.AppUserRepository;
import hk.polyu.comp4442.cloudcompute.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;

    public TaskService(TaskRepository taskRepository, AppUserRepository appUserRepository) {
        this.taskRepository = taskRepository;
        this.appUserRepository = appUserRepository;
    }

    public Task create(Long userId, Task task) {
        AppUser user = getUserById(userId);
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        task.setUser(user);
        return taskRepository.save(task);
    }

    public List<Task> getAll(Long userId) {
        return taskRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public Task getById(Long userId, Long id) {
        return taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task update(Long userId, Long id, Task updatedTask) {
        Task existingTask = getById(userId, id);
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus() == null ? TaskStatus.TODO : updatedTask.getStatus());
        return taskRepository.save(existingTask);
    }

    public void delete(Long userId, Long id) {
        Task existingTask = getById(userId, id);
        taskRepository.delete(existingTask);
    }

    private AppUser getUserById(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user does not exist."));
    }
}
