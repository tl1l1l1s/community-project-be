package ktb.week4.community.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ktb.week4.community.domain.user.entity.User;

import java.time.LocalDateTime;

public record UserResponseDto(
		@JsonProperty("user_id")
		Long userId,
		
		String email,
		String nickname,
		
		@JsonProperty("profile_image")
		String profileImage,
		
		LocalDateTime createdAt
) {
	public static UserResponseDto fromEntity(User user) {
		return new UserResponseDto(
				user.getId(),
				user.getEmail(),
				user.getNickname(),
				user.getProfileImage(),
				user.getCreatedAt()
		);
	}
}
