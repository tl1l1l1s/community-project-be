package ktb.week4.community.domain.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb.week4.community.domain.user.dto.LoginRequestDto;
import ktb.week4.community.domain.user.dto.LoginResponseDto;
import ktb.week4.community.global.apiPayload.ApiResponse;
import ktb.week4.community.global.apiPayload.ErrorCode;
import ktb.week4.community.global.apiPayload.SuccessCode;
import ktb.week4.community.global.apiSpecification.AuthApiSpecification;
import ktb.week4.community.security.jwt.JwtTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController implements AuthApiSpecification {
	
	private final JwtTokenProvider jwtTokenProvider;
	
	@Override
	@PostMapping("/login")
	public ApiResponse<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
		return ApiResponse.onSuccess(SuccessCode.SUCCESS, null);
	}
	
	@Override
	@PostMapping("/logout")
	public ResponseEntity<Void> logout() {
		return ApiResponse.onDeleteSuccess();
	}
	
	@Override
	@PostMapping("/refresh")
	public ApiResponse<Object> refreshToken(HttpServletRequest request, HttpServletResponse  response) {
		Cookie[] cookies = request.getCookies();
		
		if (cookies == null) {
			return ApiResponse.onFailure(ErrorCode.UNAUTHORIZED_REQUEST.getCode(), ErrorCode.UNAUTHORIZED_REQUEST.getMessage());
		}
		
		String refreshTokenString = Arrays.stream(cookies)
				.filter(cookie -> cookie.getName().equals("refreshToken"))
				.map(Cookie::getValue)
				.findFirst()
				.orElse(null);
		
		if (refreshTokenString == null) {
			return ApiResponse.onFailure(ErrorCode.UNAUTHORIZED_REQUEST.getCode(), ErrorCode.UNAUTHORIZED_REQUEST.getMessage());
		}
		
		try {
			jwtTokenProvider.validateToken(refreshTokenString);
			
			Authentication auth = jwtTokenProvider.resolveToken(refreshTokenString);
			
			ResponseCookie accessCookie = ResponseCookie.from("accessToken", jwtTokenProvider.createToken(auth, 60))
					.path("/")
					.maxAge(60 * 60)
					.httpOnly(true)
					.sameSite("Strict")
					.build();
			response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
			
			ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", jwtTokenProvider.createToken(auth, 3 * 24 * 60))
					.path("/auth/")
					.httpOnly(true)
					.secure(false)
					.sameSite("Strict")
					.maxAge(7 * 24 * 60 * 60)
					.build();
			
			response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
			response.setStatus(200);
			return ApiResponse.onSuccess(SuccessCode.SUCCESS, "토큰 재발급 성공");
		} catch (Exception e) {
			return ApiResponse.onFailure(ErrorCode.UNAUTHORIZED_REQUEST.getCode(), ErrorCode.UNAUTHORIZED_REQUEST.getMessage());
		}
	}
}
