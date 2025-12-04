package ktb.week4.community.global.apiPayload;

import ktb.week4.community.global.exception.Reason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode implements BaseErrorCode {
	
	_BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "invalid_request"),
	_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "internal_server_error"),
	
	UNAUTHORIZED_REQUEST(HttpStatus.UNAUTHORIZED, "AUTH401", "unauthorized_request"),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH401", "token_expired"),
	FORBIDDEN_REQUEST(HttpStatus.FORBIDDEN, "AUTH403", "forbidden_request"),
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH409", "email_already_exists"),
	NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH409", "nickname_already_exists"),
	
	FILE_UPLOAD_ERROR(HttpStatus.CONFLICT, "AUTH409", "upload_error"),
	FILE_DELETE_ERROR(HttpStatus.CONFLICT, "AUTH409", "file_delete_error"),
	
	// 유저
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404", "User not found"),
	
	// 게시글
	ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "ARTICLE404", "Article not found"),
	
	// 댓글
	COMMENT_NOT_BELONG_TO_ARTICLE(HttpStatus.NOT_FOUND, "COMMENT400", "Comment does not belong to article"),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT404", "Comment not found"),
	;
	
	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
	
	@Override
	public Reason getReason() {
		return new Reason(this.httpStatus, this.code, this.message);
	}
}
