package ktb.week4.community.security.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb.week4.community.domain.user.dto.LoginRequestDto;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.io.IOException;

public class JsonAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	
	private final ObjectMapper objectMapper;
	
	public JsonAuthenticationFilter(ObjectMapper objectMapper) {
		super(
				PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/auth/login")
		);
		this.objectMapper = objectMapper;
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException {
		
		if(request.getContentType() != null && !request.getContentType().equals("application/json")) {
			throw new AuthenticationServiceException("Not supported Content-Type :  " + request.getContentType());
		}
		
		LoginRequestDto loginReqDto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
		
		return this.getAuthenticationManager().authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(
						loginReqDto.email(), loginReqDto.password()
				)
		);
	}
}
