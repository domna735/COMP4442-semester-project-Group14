package hk.polyu.comp4442.cloudcompute.repository;

import hk.polyu.comp4442.cloudcompute.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByUserIdOrderByUpdatedAtDesc(Long userId);

	Optional<Task> findByIdAndUserId(Long id, Long userId);
}
