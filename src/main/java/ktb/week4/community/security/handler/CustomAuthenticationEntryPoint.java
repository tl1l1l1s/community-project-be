package ktb.week4.community.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb.week4.community.global.apiPayload.ApiResponse;
import ktb.week4.community.global.apiPayload.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
	
	private final ObjectMapper objectMapper;
	
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
		response.setStatus(401);
		response.setContentType("application/json;charset=UTF-8");
		
		response.getWriter().write(objectMapper.writeValueAsString(
				ApiResponse.onFailure(
						ErrorCode.UNAUTHORIZED_REQUEST.getCode(),
						ErrorCode.UNAUTHORIZED_REQUEST.getMessage()
				)
		));
	}
}
