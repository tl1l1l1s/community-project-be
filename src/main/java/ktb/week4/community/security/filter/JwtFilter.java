package ktb.week4.community.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb.week4.community.global.apiPayload.ApiResponse;
import ktb.week4.community.global.apiPayload.ErrorCode;
import ktb.week4.community.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
	
	private final ObjectMapper objectMapper;
	private final JwtTokenProvider jwtTokenProvider;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
	
		Cookie[] cookies = request.getCookies();
		if(cookies != null && Arrays.stream(cookies).anyMatch(cookie -> cookie.getName().equals("accessToken"))) {
			String accessToken = Arrays.stream(cookies)
					.filter(cookie -> cookie.getName().equals("accessToken"))
					.map(Cookie::getValue)
					.findFirst()
					.orElse(null);

			try {
				jwtTokenProvider.validateToken(accessToken);
				
				Authentication auth = jwtTokenProvider.resolveToken(accessToken);
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
			catch (ExpiredJwtException e) {
				response.setStatus(401);
				response.setContentType("application/json;charset=UTF-8");
				response.getWriter().write(objectMapper.writeValueAsString(
						ApiResponse.onFailure(ErrorCode.TOKEN_EXPIRED.getCode(),  ErrorCode.TOKEN_EXPIRED.getMessage())
				));
				return;
			}
			catch (JwtException e) {
				response.setStatus(401);
				response.setContentType("application/json;charset=UTF-8");
				response.getWriter().write(objectMapper.writeValueAsString(
						ApiResponse.onFailure(ErrorCode.UNAUTHORIZED_REQUEST.getCode(),  ErrorCode.UNAUTHORIZED_REQUEST.getMessage())
				));
				return;
			}
		}
		
		filterChain.doFilter(request, response);
	}
	
}
