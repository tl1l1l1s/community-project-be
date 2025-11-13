package ktb.week4.community.domain.user.repository;

import ktb.week4.community.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	Optional<User> findByNickname(String nickname);
	List<User> findAllByIdIn(List<Long> userIds);
}