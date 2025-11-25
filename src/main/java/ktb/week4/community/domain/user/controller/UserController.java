package ktb.week4.community.domain.user.controller;

import jakarta.validation.Valid;
import ktb.week4.community.domain.user.dto.*;
import ktb.week4.community.domain.user.service.UserCommandService;
import ktb.week4.community.domain.user.service.UserQueryService;
import ktb.week4.community.global.annotation.AuthUser;
import ktb.week4.community.global.apiPayload.ApiResponse;
import ktb.week4.community.global.apiPayload.SuccessCode;
import ktb.week4.community.global.apiSpecification.UserApiSpecification;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController implements UserApiSpecification {
	
	private final UserCommandService userCommandService;
	private final UserQueryService userQueryService;
	
	@Override
	@PostMapping
	public ApiResponse<SignUpResponseDto> registerUser(
			@RequestBody SignUpRequestDto request) {
		return ApiResponse.onCreateSuccess(SuccessCode.CREATE_SUCCESS, userCommandService.createUser(request));
	}
	
	@Override
	@GetMapping
	public ApiResponse<UserResponseDto> getUser(
			@AuthUser Long userId) {
		return ApiResponse.onSuccess(SuccessCode.SUCCESS, userQueryService.getUser(userId));
	}
	
	@Override
	@PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<UserResponseDto> updateUser(
			@AuthUser Long userId,
			@RequestPart("payload") @Valid UpdateUserRequestDto request,
			@RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
		return ApiResponse.onSuccess(SuccessCode.UPDATE_SUCCESS, userCommandService.updateUser(userId, request, profileImage));
	}
	
	@Override
	@PatchMapping("/password")
	public ApiResponse<Void> updatePassword(
			@AuthUser Long userId,
			@RequestBody UpdatePasswordRequestDto request) {
		userCommandService.updatePassword(userId, request);
		return ApiResponse.onSuccess(SuccessCode.UPDATE_SUCCESS, null);
	}
	
	@Override
	@DeleteMapping
	public ResponseEntity<Void> deleteUser(
			@AuthUser Long userId) {
		userCommandService.deleteUser(userId);
		return ApiResponse.onDeleteSuccess();
	}
}
