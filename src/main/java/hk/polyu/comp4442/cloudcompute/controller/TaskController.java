package hk.polyu.comp4442.cloudcompute.controller;

import hk.polyu.comp4442.cloudcompute.dto.CreateTaskRequest;
import hk.polyu.comp4442.cloudcompute.dto.TaskResponse;
import hk.polyu.comp4442.cloudcompute.entity.Task;
import hk.polyu.comp4442.cloudcompute.security.CustomUserDetails;
import hk.polyu.comp4442.cloudcompute.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Task Management", description = "APIs for managing tasks (create, retrieve, update, delete)")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task with the provided title, description, and status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload - validation error"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<TaskResponse> createTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        Task task = new Task(request.getTitle(), request.getDescription(), request.getStatus());
        Task savedTask = taskService.create(userDetails.getId(), task);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TaskResponse(savedTask));
    }

    @GetMapping
    @Operation(summary = "Retrieve all tasks", description = "Retrieves a list of all tasks in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<List<TaskResponse>> getAllTasks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Task> tasks = taskService.getAll(userDetails.getId());
        List<TaskResponse> responses = tasks.stream()
                .map(TaskResponse::new)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a task by ID", description = "Retrieves a specific task by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Task not found with the provided ID")
    })
    public ResponseEntity<TaskResponse> getTaskById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        Task task = taskService.getById(userDetails.getId(), id);
        return ResponseEntity.ok(new TaskResponse(task));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task", description = "Updates an existing task with the provided data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload - validation error"),
            @ApiResponse(responseCode = "404", description = "Task not found with the provided ID")
    })
    public ResponseEntity<TaskResponse> updateTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        Task task = new Task(request.getTitle(), request.getDescription(), request.getStatus());
        Task updatedTask = taskService.update(userDetails.getId(), id, task);
        return ResponseEntity.ok(new TaskResponse(updatedTask));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task", description = "Deletes a task by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
                        @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Task not found with the provided ID")
    })
        public ResponseEntity<Void> deleteTask(
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        @PathVariable Long id
        ) {
                taskService.delete(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
