package ktb.week4.community.global.apiSpecification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb.week4.community.domain.user.dto.*;
import ktb.week4.community.global.apiPayload.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "유저 관련 API")
public interface UserApiSpecification {
	
	@Operation(summary = "회원가입합니다.")
	ApiResponse<SignUpResponseDto> registerUser(@Valid SignUpRequestDto request);
	
	@Operation(summary = "사용자의 정보를 조회합니다.")
	ApiResponse<UserResponseDto> getUser(
			@Parameter(description = "정보를 조회할 사용자의 id", required = true, example = "1") Long userId);
	
	@Operation(summary = "사용자의 정보를 수정합니다.")
	ApiResponse<UserResponseDto> updateUser(
			@Parameter(description = "정보를 수정할 사용자의 id", required = true, example = "1") Long userId,
			UpdateUserRequestDto request,
			MultipartFile profileImage);
	
	@Operation(summary = "사용자의 비밀번호를 변경합니다.")
	ApiResponse<Void> updatePassword(
			@Parameter(description = "비밀번호를 변경할 사용자의 id", required = true, example = "1") Long userId,
			@Valid UpdatePasswordRequestDto request);
	
	@Operation(summary = "사용자를 탈퇴 처리합니다.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "탈퇴 성공"),
	})
	ResponseEntity<Void> deleteUser(
			@Parameter(description = "탈퇴 처리를 진행할 사용자의 id", required = true, example = "1") Long userId);
	
	@Operation(summary = "로그인합니다.")
	ApiResponse<LoginResponseDto> login(@Valid LoginRequestDto loginRequestDto);
	
	@Operation(summary = "로그아웃합니다.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "로그아웃 성공"),
	})
	ResponseEntity<Void> logout(
			@Parameter(description = "로그아웃 할 사용자의 id", required = true, example = "1") Long userId);
}
