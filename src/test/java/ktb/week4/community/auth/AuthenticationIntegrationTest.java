package ktb.week4.community.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import ktb.week4.community.domain.article.controller.ArticleController;
import ktb.week4.community.domain.article.service.ArticleCommandService;
import ktb.week4.community.domain.article.service.ArticleQueryService;
import ktb.week4.community.domain.auth.controller.AuthController;
import ktb.week4.community.domain.user.controller.UserController;
import ktb.week4.community.domain.user.dto.LoginRequestDto;
import ktb.week4.community.domain.user.service.UserCommandService;
import ktb.week4.community.domain.user.service.UserQueryService;
import ktb.week4.community.global.config.SecurityConfig;
import ktb.week4.community.security.jwt.JwtTokenProvider;
import ktb.week4.community.security.principal.CustomUserDetails;
import ktb.week4.community.security.principal.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = {
		AuthController.class,
		UserController.class,
		ArticleController.class
	},
	excludeAutoConfiguration = {
		HibernateJpaAutoConfiguration.class,
		JpaRepositoriesAutoConfiguration .class
    }
)
@Import({SecurityConfig.class, JwtTokenProvider.class})
@ActiveProfiles("test")
class AuthenticationIntegrationTest {
	
	@Autowired
	private MockMvcTester mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	AuthenticationConfiguration authenticationConfiguration;
	
	@MockitoBean
	UserCommandService userCommandService;
	
	@MockitoBean
	UserQueryService  userQueryService;
	
	@MockitoBean
	ArticleCommandService articleCommandService;
	
	@MockitoBean
	ArticleQueryService articleQueryService;
	
	@MockitoBean
	CustomUserDetailsService customUserDetailsService;
	
	String email = "test@test.t";
	String password = "Aa#12345";
	Long userId = 1L;
	
	@BeforeEach
	void setUp() {
		CustomUserDetails userDetails = new CustomUserDetails(
				email,
				passwordEncoder.encode(password),
				new ArrayList<>(),
				userId,
				""
		);
		
		when(customUserDetailsService.loadUserByUsername(email))
				.thenReturn(userDetails);
	}
	
	private String createTestAccessToken(Long userId, long validMinutes) {
		CustomUserDetails principal = new CustomUserDetails(
				email,
				password,
				new ArrayList<>(),
				userId,
				""
		);
		
		Authentication authentication =
				new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		
		return jwtTokenProvider.createToken(authentication, validMinutes);
	}
	
	@Test
	@DisplayName("/auth/refresh를 통해 토큰 재발급")
	void givenValidRefreshToken_whenRefresh_thenReissuesTokens() {
		
		assertThat(mockMvc.post().uri("/auth/refresh")
				.cookie(new Cookie("accessToken", createTestAccessToken(userId, 60)))
				.cookie(new Cookie("refreshToken", createTestAccessToken(userId, 120)))
				.exchange()
		)
				.hasStatus2xxSuccessful()
				.cookies()
				.isHttpOnly("accessToken", true)
				.hasPath("accessToken", "/")
				.isHttpOnly("refreshToken", true)
				.hasPath("refreshToken", "/auth/");
		
	}
	
	@Test
	@DisplayName("토큰이 유효한 경우 authenticated url에 접속 가능")
	void givenValidAccessToken_whenAccessAuthenticatedEndpoint_thenSucceeds() {
		
		when(userQueryService.getUser(1L)).thenReturn(null);
		assertThat(mockMvc
				.get().uri("/users")
				.cookie(new Cookie("accessToken", createTestAccessToken(userId, 60)))
		).hasStatus2xxSuccessful();
	}
	
	@Test
	@DisplayName("토큰이 만료된 경우 authenticated url에 접속 불가능")
	void givenExpiredAccessToken_whenAccessAuthenticatedEndpoint_thenReturnsUnauthorized() {
		
		when(userQueryService.getUser(1L)).thenReturn(null);
		assertThat(mockMvc
				.get().uri("/users")
				.cookie(new Cookie("accessToken", createTestAccessToken(userId, -1)))
		).hasStatus(HttpStatus.UNAUTHORIZED);
	}
	
	@Test
	@DisplayName("토큰이 없는 경우 authenticated url에 접속 불가능")
	@WithAnonymousUser
	void givenNoAccessToken_whenAccessAuthenticatedEndpoint_thenReturnsUnauthorized() {
		
		when(userQueryService.getUser(1L)).thenReturn(null);
		assertThat(mockMvc
				.get().uri("/users")
		).hasStatus(HttpStatus.UNAUTHORIZED);
	}
	
	@Test
	@DisplayName("토큰이 유효한 경우 anonymous url에 접속 가능")
	void givenValidAccessToken_whenAccessAnonymousEndpoint_thenSucceeds() throws Exception {
		
		assertThat(mockMvc
				.post().uri("/auth/login")
				.cookie(new Cookie("accessToken", createTestAccessToken(userId, 60)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequestDto(email, password)))
		).hasStatus2xxSuccessful();
	}
	
	@Test
	@DisplayName("토큰이 만료된 경우 anonymous url에 접속 가능")
	void givenExpiredAccessToken_whenAccessAnonymousEndpoint_thenSucceeds() throws Exception {
		
		assertThat(mockMvc
				.post().uri("/auth/login")
				.cookie(new Cookie("accessToken", createTestAccessToken(userId, -1)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequestDto(email, password)))
		).hasStatus2xxSuccessful();
	}
	
	@Test
	@DisplayName("토큰이 없는 경우 anonymous url에 접속 가능")
	@WithAnonymousUser
	void givenNoAccessToken_whenAccessAnonymousEndpoint_thenSucceeds() throws Exception {
		
		assertThat(mockMvc
				.post().uri("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new LoginRequestDto(email, password)))
		).hasStatus2xxSuccessful();
	}
	
	@Test
	@DisplayName("토큰이 유효한 경우 permitAll url에 접속 가능")
	void givenValidAccessToken_whenAccessPermitAllEndpoint_thenSucceeds() {
		
		when(articleQueryService.getArticles(1, 7)).thenReturn(null);
		assertThat(mockMvc
				.get().uri("/articles")
				.param("page", "1")
				.cookie(new Cookie("accessToken", createTestAccessToken(userId, 60)))
		).hasStatus2xxSuccessful();
	}
	
	@Test
	@DisplayName("토큰이 만료된 경우 permitAll url에 접속 가능")
	@WithMockUser
	void givenExpiredAccessToken_whenAccessPermitAllEndpoint_thenSucceeds() {
		
		when(articleQueryService.getArticles(1, 7)).thenReturn(null);
		assertThat(mockMvc
				.get().uri("/articles")
				.param("page", "1")
				.cookie(new Cookie("accessToken", createTestAccessToken(userId, -1)))
		).hasStatus2xxSuccessful();
	}
	
	@Test
	@DisplayName("토큰이 없는 경우 permitAll url에 접속 가능")
	@WithAnonymousUser
	void givenNoAccessToken_whenAccessPermitAllEndpoint_thenSucceeds() {
		
		when(articleQueryService.getArticles(1, 7)).thenReturn(null);
		assertThat(mockMvc
				.get().uri("/articles")
				.param("page", "1")
		).hasStatus2xxSuccessful();
	}
}