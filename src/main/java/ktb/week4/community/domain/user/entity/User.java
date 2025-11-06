package ktb.week4.community.domain.user.entity;

import jakarta.persistence.*;
import ktb.week4.community.domain.article.entity.Article;
import ktb.week4.community.domain.comment.entity.Comment;
import ktb.week4.community.domain.user.enums.Status;
import ktb.week4.community.global.common.BaseEntity;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@RequiredArgsConstructor
@DynamicInsert
public class User extends BaseEntity {
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;
	
	@Column(nullable = false)
	private String nickname;
	
	@Column(nullable = false)
	private String email;
	
	@Column(nullable = false)
	private String password;
	
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column(nullable = false)
	@ColumnDefault("'default_image.png'")
	private String profileImage;
	
	private LocalDateTime deletedAt;
	
	@OneToMany(mappedBy = "user")
	private List<Article> articles;
	
	@OneToMany(mappedBy = "user")
	private List<Comment> comments;
	
	
	public User(String nickname, String password, String email, String profileImage) {
		this.nickname = nickname;
		this.password = password;
		this.email = email;
		this.profileImage = profileImage;
		this.status = Status.ACTIVE;
	}
	
	public void changePassword(String newPassword) {
		this.password = newPassword;
	}
	
	public void changeNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public void changeProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}
	
	public void deleteUser() {
		this.status = Status.INACTIVE;
		this.deletedAt = LocalDateTime.now();
	}
}
