package ktb.week4.community.security.principal;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {
	
	private final Long userId;
	private final String userProfileImage;
	
	public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities,
							 Long userId, String profileImage) {
		super(username, password, authorities);
		this.userId = userId;
		this.userProfileImage = profileImage;
	}
}
