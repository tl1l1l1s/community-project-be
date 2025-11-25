package ktb.week4.community.global.apiSpecification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import ktb.week4.community.domain.user.dto.LoginRequestDto;
import ktb.week4.community.domain.user.dto.LoginResponseDto;
import ktb.week4.community.global.apiPayload.ApiResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "인증 관련 API")
public interface AuthApiSpecification {
	
	@Operation(summary = "로그인합니다.")
	ApiResponse<LoginResponseDto> login(@Valid LoginRequestDto loginRequestDto);
	
	@Operation(summary = "로그아웃합니다.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "로그아웃 성공"),
	})
	ResponseEntity<Void> logout();
	
	@Operation(summary = "리프레시 토큰을 받아옵니다.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리프레시 토큰 발급 성공"),
	})
	ApiResponse<Object> refreshToken(HttpServletRequest request, HttpServletResponse response);
}