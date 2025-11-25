package ktb.week4.community.security.principal;

import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.loader.UserLoader;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	
	private final UserLoader userLoader;
	
	@Override
	public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userLoader.getUserByEmail(username);
		
		return new CustomUserDetails(
				user.getEmail(),
				user.getPassword(),
				Collections.emptyList(),
				user.getId(),
				user.getProfileImage()
		);
	}
}
