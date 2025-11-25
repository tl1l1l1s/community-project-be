package ktb.week4.community.domain.user.service;

import ktb.week4.community.domain.user.dto.UserResponseDto;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.loader.UserLoader;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {
	private final UserLoader userLoader;
	
	public UserResponseDto getUser(Long userId) {
		User user = userLoader.getUserById(userId);
		return UserResponseDto.fromEntity(user);
	}
}
