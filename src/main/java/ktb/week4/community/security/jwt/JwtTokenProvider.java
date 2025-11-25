package ktb.week4.community.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import ktb.week4.community.security.principal.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
	private final String secret;
	
	public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
		this.secret = secret;
	}
	
	public String createToken(Authentication authentication, long validMinutes) {
		String authorities = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		
		long now = (new Date()).getTime();
		Date expiresAt = new Date(now + validMinutes * 60 * 1000);
		
		return Jwts.builder()
				.subject(authentication.getName())
				.claim("authorities", authorities)
				.claim("userId", userDetails.getUserId())
				.claim("userName", userDetails.getUsername())
				.claim("profileImage", userDetails.getUserProfileImage())
				.signWith(getSecretKey())
				.expiration(expiresAt)
				.compact();
	}
	
	public void validateToken(String token) throws ExpiredJwtException, MalformedJwtException, UnsupportedJwtException  {
		Jwts.parser()
				.verifyWith(getSecretKey())
				.build()
				.parseSignedClaims(token);
	}
	
	public Authentication resolveToken(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(getSecretKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		
		String authoritiesString = claims.get("authorities", String.class);
		
		Collection<? extends GrantedAuthority> authorities;
		
		if (authoritiesString == null || authoritiesString.isEmpty()) {
			authorities = Collections.emptyList();
		} else {
			authorities = Arrays.stream(authoritiesString.split(","))
					.map(SimpleGrantedAuthority::new)
					.toList();
		}
		Long id =  claims.get("userId", Long.class);
		String userName = claims.get("userName").toString();
		String userProfileImage = claims.get("profileImage").toString();
		CustomUserDetails principal = new CustomUserDetails(
				userName, "", authorities, id, userProfileImage
		);
		
		return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
	}
	
	private SecretKey getSecretKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
