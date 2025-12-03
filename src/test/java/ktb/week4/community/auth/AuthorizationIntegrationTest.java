package ktb.week4.community.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import ktb.week4.community.domain.user.dto.LoginRequestDto;
import ktb.week4.community.domain.user.entity.UserTestBuilder;
import ktb.week4.community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthorizationIntegrationTest {
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	UserRepository userRepository;
	
	String email  = "test@test.t";
	String password = "Aa#12345";
	String wrongEmail = "failTest@test.t";
	String wrongPassword = "Zz#12345";
	
	@BeforeEach
	void setUp() {
		userRepository.save(UserTestBuilder.aUser()
				.withEmail(email)
				.withPassword(passwordEncoder.encode(password))
				.build());
	}
	
	@Test
	@DisplayName("로그인 성공 시 성공 및 토큰 반환")
	void shouldReturnCookieAndSuccessToUsersIfLoginSuccess() throws Exception {
		
		mockMvc.perform(
						post("/auth/login")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(
										new LoginRequestDto(email,  password)
								))
				).andExpect(status().is2xxSuccessful())
				.andExpect(cookie().exists("accessToken"))
				.andExpect(cookie().exists("refreshToken"));
	}
	
	@Test
	@DisplayName("잘못된 이메일로 로그인 실패 시 실패 응답, 토큰 미반환")
	void shouldNotReturnCookieIfLoginFailUsingWrongEmail() throws Exception {
		
		mockMvc.perform(
						post("/auth/login")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(
										new LoginRequestDto(wrongEmail,  password)
								))
				).andExpect(status().is4xxClientError())
				.andExpect(cookie().doesNotExist("accessToken"))
				.andExpect(cookie().doesNotExist("refreshToken"))
				.andReturn();
		
		mockMvc.perform(
				get("/users")
		).andExpect(status().is4xxClientError());
	}
	
	@Test
	@DisplayName("잘못된 비밀번호로 로그인 실패 시 실패 응답, 토큰 미반환")
	void shouldNotReturnCookieIfLoginFailUsingWrongPassword() throws Exception {
		
		mockMvc.perform(
						post("/auth/login")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(
										new LoginRequestDto(email, wrongPassword)
								))
				).andExpect(status().is4xxClientError())
				.andExpect(cookie().doesNotExist("accessToken"))
				.andExpect(cookie().doesNotExist("refreshToken"))
				.andReturn();
		
		mockMvc.perform(
				get("/users")
		).andExpect(status().is4xxClientError());
	}
	
	@Test
	@DisplayName("로그아웃 성공 시 토큰 제거/만료")
	void shouldExpireCookieIfLogoutSuccess() throws Exception {
		// given
		MvcResult loginResult = mockMvc.perform(
						post("/auth/login")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(
										new LoginRequestDto(email, password)
								))
				)
				.andExpect(status().is2xxSuccessful())
				.andReturn();
		
		Cookie accessTokenCookie = loginResult.getResponse().getCookie("accessToken");
		Cookie refreshTokenCookie = loginResult.getResponse().getCookie("refreshToken");
		
		// when
		MvcResult logoutResult = mockMvc.perform(
						post("/auth/logout")
								.cookie(accessTokenCookie, refreshTokenCookie)
				)
				.andExpect(status().is2xxSuccessful())
				.andReturn();
		
		Cookie expiredAccess = logoutResult.getResponse().getCookie("accessToken");
		Cookie expiredRefresh = logoutResult.getResponse().getCookie("refreshToken");
		
		//then
		assertEquals(0, expiredAccess.getMaxAge());
		assertEquals(0, expiredRefresh.getMaxAge());
	}
}
