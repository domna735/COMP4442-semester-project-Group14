package hk.polyu.comp4442.cloudcompute.repository;

import hk.polyu.comp4442.cloudcompute.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
