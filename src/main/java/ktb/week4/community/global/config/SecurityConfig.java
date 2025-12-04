package ktb.week4.community.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.week4.community.domain.user.dto.LoginResponseDto;
import ktb.week4.community.global.apiPayload.ApiResponse;
import ktb.week4.community.global.apiPayload.SuccessCode;
import ktb.week4.community.security.handler.CustomAuthenticationEntryPoint;
import ktb.week4.community.security.principal.CustomUserDetails;
import ktb.week4.community.security.jwt.JwtTokenProvider;
import ktb.week4.community.security.filter.JsonAuthenticationFilter;
import ktb.week4.community.security.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final AuthenticationConfiguration authenticationConfiguration;
	private final ObjectMapper objectMapper;
	private final JwtTokenProvider jwtTokenProvider;
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(sessionManagement ->
						sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.exceptionHandling(exception -> {
							exception.authenticationEntryPoint(new CustomAuthenticationEntryPoint(objectMapper));
						}
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/uploads/**", "/error/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/auth/login/**", "/users/**").anonymous()
						.requestMatchers(HttpMethod.GET, "/articles/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
						.anyRequest().authenticated()
				)
				.addFilterAt(jsonAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new JwtFilter(jwtTokenProvider), AuthorizationFilter.class)
				.logout(logout -> {
					logout
							.logoutUrl("/auth/logout")
							.logoutSuccessHandler((request, response, authentication) -> {
								response.setStatus(204);
								response.setContentType("application/json;charset=UTF-8");
							})
							.addLogoutHandler((request, response, authentication) -> {
								ResponseCookie accessToken = ResponseCookie.from("accessToken", "")
										.path("/")
										.maxAge(0)
										.httpOnly(true)
										.sameSite("None")
										.secure(true)
										.build();
								response.addHeader(HttpHeaders.SET_COOKIE, accessToken.toString());
								ResponseCookie refreshToken = ResponseCookie.from("refreshToken", "")
										.path("/auth/")
										.maxAge(0)
										.httpOnly(true)
										.sameSite("None")
										.secure(true)
										.build();
								response.addHeader(HttpHeaders.SET_COOKIE, refreshToken.toString());
							});
				});
		
		return http.build();
	}
	
	private JsonAuthenticationFilter jsonAuthenticationFilter() throws Exception {
		JsonAuthenticationFilter filter = new JsonAuthenticationFilter(objectMapper);
		
		filter.setAuthenticationManager(authenticationConfiguration.getAuthenticationManager());
		filter.setFilterProcessesUrl("/auth/login");
		filter.setAuthenticationSuccessHandler((request, response, authentication) -> {
			response.setStatus(200);
			response.setContentType("application/json;charset=UTF-8");
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			
			ResponseCookie accessToken = ResponseCookie.from("accessToken", jwtTokenProvider.createToken(authentication, 60))
					.path("/")
					.maxAge(60 * 60)
					.httpOnly(true)
					.sameSite("None")
					.secure(true)
					.build();
			response.addHeader(HttpHeaders.SET_COOKIE, accessToken.toString());
			
			ResponseCookie refreshToken = ResponseCookie.from("refreshToken", jwtTokenProvider.createToken(authentication, 3 * 24 * 60))
					.path("/auth/")
					.maxAge(7 * 24 * 60 * 60)
					.httpOnly(true)
					.sameSite("None")
					.secure(true)
					.build();
			response.addHeader(HttpHeaders.SET_COOKIE, refreshToken.toString());
			
			response.getWriter().write(objectMapper.writeValueAsString(
					ApiResponse.onSuccess(SuccessCode.SUCCESS, new LoginResponseDto(
							userDetails.getUserId(), userDetails.getUserProfileImage())
					))
			);
		});
		
		filter.setAuthenticationFailureHandler((request,  response, exception) -> {
			response.setStatus(401);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(objectMapper.writeValueAsString(
					ApiResponse.onFailure("401",  "로그인에 실패하였습니다.")
			));
		});
		
		return filter;
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		
		corsConfiguration.setAllowCredentials(true);
		corsConfiguration.setAllowedOrigins(List.of("http://127.0.0.1:5500", "http://localhost:5173", "http://localhost:5174"));
		corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		corsConfiguration.addAllowedHeader("*");
		corsConfiguration.addExposedHeader("*");
		corsConfiguration.setMaxAge(3600L);
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfiguration);
		return source;
	}
}
