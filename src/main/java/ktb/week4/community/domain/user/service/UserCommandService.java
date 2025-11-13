package ktb.week4.community.domain.user.service;

import ktb.week4.community.domain.user.dto.*;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.repository.UserRepository;
import ktb.week4.community.domain.user.loader.UserLoader;
import ktb.week4.community.domain.user.validator.UserValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class UserCommandService {
	private final UserRepository userRepository;
	private final UserLoader userLoader;
	private final UserValidator userValidator;
	
	public SignUpResponseDto createUser(SignUpRequestDto request) {
		userValidator.validateEmailIsNotTaken(request.email());
		userValidator.validateNicknameIsNotTaken(request.nickname());
		
		User user = userRepository.save(new User(
				request.nickname(),
				request.password(),
				request.email(),
				request.profileImage()
		));
		
		return new SignUpResponseDto(user.getId());
	}
	
	public UserResponseDto updateUser(Long userId, UpdateUserRequestDto request) {
		User user = userLoader.getUserById(userId);
		if(!request.nickname().isEmpty()) {
			user.changeNickname(request.nickname());
		}
		if(!request.profileImage().isEmpty()) {
			user.changeProfileImage(request.profileImage());
		}

		return UserResponseDto.fromEntity(userRepository.save(user));
	}
	
	public void deleteUser(Long userId) {
		User user = userLoader.getUserById(userId);
		user.deleteUser();
		userRepository.save(user);
	}
	
	public void updatePassword(Long userId, UpdatePasswordRequestDto request) {
		User user = userLoader.getUserById(userId);
		user.changePassword(request.password());
		userRepository.save(user);
	}
}
