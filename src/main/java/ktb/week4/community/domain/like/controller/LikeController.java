package ktb.week4.community.domain.like.controller;

import ktb.week4.community.domain.like.dto.LikeResponseDto;
import ktb.week4.community.domain.like.service.LikeCommandService;
import ktb.week4.community.domain.like.service.LikeQueryService;
import ktb.week4.community.global.annotation.AuthUser;
import ktb.week4.community.global.apiPayload.ApiResponse;
import ktb.week4.community.global.apiPayload.SuccessCode;
import ktb.week4.community.global.apiSpecification.LikeApiSpecification;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/articles/{articleId}/likes")
@AllArgsConstructor
public class LikeController implements LikeApiSpecification {

    private final LikeQueryService likeQueryService;
    private final LikeCommandService likeCommandService;
	
	@Override
    @GetMapping
    public ApiResponse<LikeResponseDto> getLikes(
            @PathVariable Long articleId,
            @AuthUser Long userId) {
        return ApiResponse.onSuccess(SuccessCode.SUCCESS, likeQueryService.getLikeStatus(articleId, userId));
    }
	
	@Override
	@PostMapping
    public ApiResponse<LikeResponseDto> createLike(
			@PathVariable Long articleId,
			@AuthUser Long userId) {
        return ApiResponse.onCreateSuccess(SuccessCode.CREATE_SUCCESS, likeCommandService.likeArticle(articleId, userId));
    }
	
	@Override
    @DeleteMapping
    public ResponseEntity<Void> deleteLike(
            @PathVariable Long articleId,
			@AuthUser Long userId) {
        likeCommandService.unlikeArticle(articleId, userId);
		return ApiResponse.onDeleteSuccess();
    }
}
