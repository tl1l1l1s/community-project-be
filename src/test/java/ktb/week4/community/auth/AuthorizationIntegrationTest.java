package ktb.week4.community.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import ktb.week4.community.domain.auth.controller.AuthController;
import ktb.week4.community.domain.user.dto.LoginRequestDto;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.entity.UserTestBuilder;
import ktb.week4.community.domain.user.repository.UserRepository;
import ktb.week4.community.global.config.SecurityConfig;
import ktb.week4.community.security.jwt.JwtTokenProvider;
import ktb.week4.community.security.principal.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers ={
		AuthController.class
		},
	excludeAutoConfiguration = {
		HibernateJpaAutoConfiguration .class,
		JpaRepositoriesAutoConfiguration .class
    	}
)
@Import({SecurityConfig.class, JwtTokenProvider.class})
@ActiveProfiles("test")
public class AuthorizationIntegrationTest {
	@Autowired
	private MockMvcTester mockMvc;
	
	@MockitoBean
	AuthenticationManager authenticationManager;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@MockitoBean
	UserRepository userRepository;
	
	User user;
	String email  = "test@test.t";
	String password = "Aa#12345";
	String wrongEmail = "failTest@test.t";
	String wrongPassword = "Zz#12345";
	
	@BeforeEach
	void setUp() {
		user = UserTestBuilder.aUser()
				.withEmail(email)
				.withPassword(passwordEncoder.encode(password))
				.build();
	}
	
	@Test
	@DisplayName("로그인 성공 시 성공 및 토큰 반환")
	void givenValidCredentials_whenLogin_thenSetsAuthCookies() throws Exception {
		
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		
		CustomUserDetails principal = new CustomUserDetails(email, password, Collections.emptyList(), 1L, null);
		Authentication auth = new UsernamePasswordAuthenticationToken(
				principal, null, principal.getAuthorities());
		when(authenticationManager.authenticate(any(Authentication.class)))
				.thenReturn(auth);
		
		assertThat(
				mockMvc.post().uri("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new LoginRequestDto(email,  password)
						))
		).hasStatus2xxSuccessful()
						.cookies()
						.isHttpOnly("accessToken", true)
						.hasPath("accessToken", "/")
						.isHttpOnly("refreshToken", true)
						.hasPath("refreshToken", "/auth/");
	}
	
	@Test
	@DisplayName("잘못된 이메일로 로그인 실패 시 실패 응답, 토큰 미반환")
	void givenInvalidEmail_whenLogin_thenFailsAndDoesNotSetCookies() throws Exception {
		
		when(authenticationManager.authenticate(any(Authentication.class)))
				.thenThrow(new BadCredentialsException("bad credentials"));
		
		assertThat(
				mockMvc.post().uri("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new LoginRequestDto(wrongEmail,  password)
						))
		).hasStatus4xxClientError()
				.cookies().isEmpty();
	}
	
	@Test
	@DisplayName("잘못된 비밀번호로 로그인 실패 시 실패 응답, 토큰 미반환")
	void givenInvalidPassword_whenLogin_thenFailsAndDoesNotSetCookies() throws Exception {
		
		when(authenticationManager.authenticate(any(Authentication.class)))
				.thenThrow(new BadCredentialsException("bad credentials"));
		
		assertThat(
				mockMvc.post().uri("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new LoginRequestDto(email,  wrongPassword)
						))
		).hasStatus4xxClientError()
				.cookies().isEmpty();
	}
	
	@Test
	@DisplayName("로그아웃 성공 시 토큰 제거/만료")
	void givenAuthenticatedUser_whenLogout_thenExpiresAuthCookies() throws Exception {
		// given
		CustomUserDetails principal =
				new CustomUserDetails(email, password, Collections.emptyList(), 1L, null);
		Authentication auth =
				new UsernamePasswordAuthenticationToken(
						principal, null, principal.getAuthorities());
		when(authenticationManager.authenticate(any(Authentication.class)))
				.thenReturn(auth);
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		
		MvcTestResult loginResult = mockMvc.post().uri("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
						new LoginRequestDto(email, password)
				)).exchange();
		
		Cookie accessTokenCookie = loginResult.getResponse().getCookie("accessToken");
		Cookie refreshTokenCookie = loginResult.getResponse().getCookie("refreshToken");
		
		// when
		MvcTestResult logoutResult = mockMvc
				.post().uri("/auth/logout")
				.cookie(accessTokenCookie, refreshTokenCookie)
				.exchange();
		
		Cookie expiredAccess = logoutResult.getResponse().getCookie("accessToken");
		Cookie expiredRefresh = logoutResult.getResponse().getCookie("refreshToken");
		
		//then
		assertEquals(0, expiredAccess.getMaxAge());
		assertEquals(0, expiredRefresh.getMaxAge());
	}
}
