package ktb.week4.community.domain.comment.controller;

import ktb.week4.community.domain.comment.dto.CommentResponseDto;
import ktb.week4.community.domain.comment.dto.CreateCommentRequestDto;
import ktb.week4.community.domain.comment.dto.GetCommentsResponseDto;
import ktb.week4.community.domain.comment.dto.UpdateCommentRequestDto;
import ktb.week4.community.domain.comment.service.CommentCommandService;
import ktb.week4.community.domain.comment.service.CommentQueryService;
import ktb.week4.community.global.annotation.AuthUser;
import ktb.week4.community.global.apiPayload.ApiResponse;
import ktb.week4.community.global.apiPayload.SuccessCode;
import ktb.week4.community.global.apiSpecification.CommentApiSpecification;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/articles/{articleId}/comments")
@AllArgsConstructor
public class CommentController implements CommentApiSpecification {
	
	private final CommentCommandService commentCommandService;
	private final CommentQueryService commentQueryService;
	
	@Override
	@GetMapping
	public ApiResponse<GetCommentsResponseDto> getComments(
			@PathVariable Long articleId,
			@RequestParam(name = "page") int page,
			@RequestParam(name = "size", defaultValue = "7") int size) {
		return ApiResponse.onSuccess(SuccessCode.SUCCESS, commentQueryService.getComments(articleId, page, size));
	}
	
	@Override
	@PostMapping
	public ApiResponse<CommentResponseDto> createComment(
			@PathVariable Long articleId, @AuthUser Long userId,
			@RequestBody CreateCommentRequestDto request) {
		return ApiResponse.onCreateSuccess(SuccessCode.CREATE_SUCCESS, commentCommandService.createComment(userId, articleId, request));
	}
	
	@Override
	@PatchMapping("/{commentId}")
	public ApiResponse<CommentResponseDto> updateComment(
			@PathVariable Long articleId,
			@PathVariable Long commentId, @AuthUser Long userId,
			@RequestBody UpdateCommentRequestDto request) {
		return ApiResponse.onSuccess(SuccessCode.UPDATE_SUCCESS, commentCommandService.updateComment(articleId, userId, commentId, request));
	}
	
	@Override
	@DeleteMapping("/{commentId}")
	public ResponseEntity<Void> deleteComment(
			@PathVariable Long articleId,
			@PathVariable Long commentId,
			@AuthUser Long userId) {
		commentCommandService.deleteComment(articleId, userId, commentId);
		return ApiResponse.onDeleteSuccess();
	}
}