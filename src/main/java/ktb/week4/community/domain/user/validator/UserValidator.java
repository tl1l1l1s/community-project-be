package ktb.week4.community.domain.user.validator;

import ktb.week4.community.domain.user.repository.UserRepository;
import ktb.week4.community.global.apiPayload.ErrorCode;
import ktb.week4.community.global.exception.GeneralException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserValidator {
	private final UserRepository userRepository;
	
	public void validateEmailIsNotTaken(String email) {
		if (userRepository.findByEmail(email).isPresent()) {
			throw new GeneralException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}
	}
	
	public void validateNicknameIsNotTaken(String nickname) {
		if (userRepository.findByNickname(nickname).isPresent()) {
			throw new GeneralException(ErrorCode.NICKNAME_ALREADY_EXISTS);
		}
	}
}
