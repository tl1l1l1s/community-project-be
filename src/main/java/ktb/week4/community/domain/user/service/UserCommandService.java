package ktb.week4.community.domain.user.service;

import ktb.week4.community.domain.user.dto.*;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.loader.UserLoader;
import ktb.week4.community.domain.user.repository.UserRepository;
import ktb.week4.community.domain.user.validator.UserValidator;
import ktb.week4.community.global.file.FileStorageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
@Transactional
public class UserCommandService {
	private final UserRepository userRepository;
	private final UserLoader userLoader;
	private final UserValidator userValidator;
	private final FileStorageService fileStorageService;
	private static final String DEFAULT_PROFILE_IMAGE = "/assets/images/user.svg";
	
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
	
	public UserResponseDto updateUser(Long userId, UpdateUserRequestDto request, MultipartFile profileImageFile) {
		User user = userLoader.getUserById(userId);
		if (StringUtils.hasText(request.nickname()) && !request.nickname().equals(user.getNickname())) {
			userValidator.validateNicknameIsNotTaken(request.nickname());
			user.changeNickname(request.nickname());
		}

		if (profileImageFile != null && !profileImageFile.isEmpty()) {
			fileStorageService.delete(user.getProfileImage());
			String storedImagePath = fileStorageService.store(profileImageFile, "profiles");
			user.changeProfileImage(storedImagePath);
		} else if (request.profileImage() != null) {
			if (!StringUtils.hasText(request.profileImage())) {
				fileStorageService.delete(user.getProfileImage());
				user.changeProfileImage(DEFAULT_PROFILE_IMAGE);
			} else {
				user.changeProfileImage(request.profileImage());
			}
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
