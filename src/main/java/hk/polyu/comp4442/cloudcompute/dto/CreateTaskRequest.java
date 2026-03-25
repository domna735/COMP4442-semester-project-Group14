package hk.polyu.comp4442.cloudcompute.dto;

import hk.polyu.comp4442.cloudcompute.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 1, max = 120, message = "Task title must be between 1 and 120 characters")
    private String title;

    @Size(max = 1000, message = "Task description must not exceed 1000 characters")
    private String description;

    private TaskStatus status;

    public CreateTaskRequest() {
    }

    public CreateTaskRequest(String title, String description, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
