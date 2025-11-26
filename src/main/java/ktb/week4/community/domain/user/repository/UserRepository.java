package ktb.week4.community.domain.user.repository;

import ktb.week4.community.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Boolean existsByEmail(String email);
	Boolean existsByNickname(String nickname);
	Optional<User> findByEmail(String email);
	List<User> findAllByIdIn(List<Long> userIds);
}