package ktb.week4.community.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import ktb.week4.community.domain.article.entity.ArticleTestBuilder;
import ktb.week4.community.domain.article.repository.ArticleRepository;
import ktb.week4.community.domain.user.dto.LoginRequestDto;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.entity.UserTestBuilder;
import ktb.week4.community.domain.user.repository.UserRepository;
import ktb.week4.community.security.jwt.JwtTokenProvider;
import ktb.week4.community.security.principal.CustomUserDetails;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthenticationIntegrationTest {
	
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ArticleRepository articleRepository;
	
	@Autowired
	ObjectMapper objectMapper;
	
	String email = "test@test.t";
	String password = "Aa#12345";
	Long userId;
	
	@BeforeEach
	void setUp() {
		User user = UserTestBuilder.aUser()
				.withEmail(email)
				.withPassword(passwordEncoder.encode(password))
				.build();
		userRepository.save(user);
		userId = user.getId();
		
		articleRepository.save(ArticleTestBuilder.anArticle()
				.withUser(userRepository.findById(userId).orElseThrow())
				.build());
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
	void shouldRefreshTokenIfRefreshTokenExists() throws Exception {
		
		mockMvc.perform(
				post("/auth/refresh")
						.cookie(new Cookie("accessToken", createTestAccessToken(userId, 60)))
						.cookie(new Cookie("refreshToken", createTestAccessToken(userId, 120)))
		).andExpect(status().is2xxSuccessful())
				.andExpect(result -> result.getResponse().getCookie("accessToken"))
				.andExpect(result -> result.getResponse().getCookie("refreshToken"));
	}
	
	@Test
	@DisplayName("토큰이 유효한 경우 authenticated url에 접속 가능")
	void shouldBeAbleToAccessAtAuthenticatedIfValidatedTokenExists() throws Exception {
		mockMvc.perform(
				get("/users")
						.cookie(new Cookie("accessToken", createTestAccessToken(userId, 60)))
		).andExpect(status().is2xxSuccessful());
	}
	
	@Test
	@DisplayName("토큰이 만료된 경우 authenticated url에 접속 블가능")
	void shouldBeAbleToAccessAtAuthenticatedIfExpiredTokenExists() throws Exception {
		mockMvc.perform(
				get("/users")
						.cookie(new Cookie("accessToken", createTestAccessToken(userId, -1)))
		).andExpect(status().isUnauthorized());
	}
	
	@Test
	@DisplayName("토큰이 없는 경우 authenticated url에 접속 불가능")
	void shouldBeAbleToAccessAtAuthenticatedIfTokenUnexists() throws Exception {
		mockMvc.perform(
				get("/users")
		).andExpect(status().isUnauthorized());
	}
	
	@Test
	@DisplayName("토큰이 유효한 경우 anonymous url에 접속 가능")
	void shouldBeAbleToAccessAtAnonymousIfValidatedTokenExists() throws Exception {
		mockMvc.perform(
				post("/auth/login")
						.cookie(new Cookie("accessToken", createTestAccessToken(userId, 60)))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequestDto(email, password)))
		).andExpect(status().is2xxSuccessful());
	}
	
	@Test
	@DisplayName("토큰이 만료된 경우 anonymous url에 접속 가능")
	void shouldBeAbleToAccessAtAnonymousIfExpiredTokenExists() throws Exception {
		mockMvc.perform(
				post("/auth/login")
						.cookie(new Cookie("accessToken", createTestAccessToken(userId, -1)))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequestDto(email, password)))
		).andExpect(status().is2xxSuccessful());
	}
	
	@Test
	@DisplayName("토큰이 없는 경우 anonymous url에 접속 가능")
	void shouldBeAbleToAccessAtAnonymousIfTokenUnexists() throws Exception {
		mockMvc.perform(
				post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequestDto(email, password)))
		).andExpect(status().is2xxSuccessful());
	}
	
	@Test
	@DisplayName("토큰이 유효한 경우 permitAll url에 접속 가능")
	void shouldBeAbleToAccessAtPermitAllIfValidatedTokenExists() throws Exception {
		mockMvc.perform(
				get("/articles")
						.param("page", "1")
						.cookie(new Cookie("accessToken", createTestAccessToken(userId, 60)))
		).andExpect(status().is2xxSuccessful());
	}
	
	@Test
	@DisplayName("토큰이 만료된 경우 permitAll url에 접속 가능")
	void shouldBeAbleToAccessAtPermitAllIfExpiredTokenExists() throws Exception {
		mockMvc.perform(
				get("/articles")
						.param("page", "1")
						.cookie(new Cookie("accessToken", createTestAccessToken(userId, -1)))
		).andExpect(status().is2xxSuccessful());
	}
	
	@Test
	@DisplayName("토큰이 없는 경우 permitAll url에 접속 가능")
	void shouldBeAbleToAccessAtPermitAllIfTokenUnexists() throws Exception {
		mockMvc.perform(
				get("/articles")
						.param("page", "1")
		).andExpect(status().is2xxSuccessful());
	}
	
}
