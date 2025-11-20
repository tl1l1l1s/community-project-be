package ktb.week4.community.global.apiSpecification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import ktb.week4.community.domain.article.dto.ArticleResponseDto;
import ktb.week4.community.domain.article.dto.CreateArticleRequestDto;
import ktb.week4.community.domain.article.dto.GetArticlesResponseDto;
import ktb.week4.community.domain.article.dto.UpdateArticleRequestDto;
import ktb.week4.community.global.apiPayload.ApiResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

@Tag(name = "Article", description = "게시글 관련 API")
public interface ArticleApiSpecification {
	
	@Operation(summary = "글 작성 시간을 기준으로 하여, 최신 순으로  게시글들을 조회합니다.")
	ApiResponse<GetArticlesResponseDto> getArticles(
			@Parameter(description = "조회할 게시글의 페이지", required = true, example = "1") @Min(value = 1, message = "Page parameter must be at least 1") int page,
			@Parameter(description = "조회할 게시글의 페이지 당 사이즈", required = true, example = "7") @Min(value = 1, message = "Size parameter must be at least 1") int size);
	
	@Operation(summary = "게시글을 생성합니다.")
	ApiResponse<ArticleResponseDto> createArticle(
			@Parameter(description = "게시글을 생성하는 유저의 id", required = true, example = "1")
			Long userId,
			@Valid CreateArticleRequestDto request,
			MultipartFile image);
	
	@Operation(summary = "게시글을 수정합니다.")
	ApiResponse<ArticleResponseDto> updateArticle(
			@Parameter(description = "수정하고자 하는 게시글의 id", required = true, example = "1") Long articleId,
			@Parameter(description = "게시글을 수정하는 유저의 id", required = true, example = "1") Long userId,
			@Valid UpdateArticleRequestDto request,
			MultipartFile image);
	
	@Operation(summary = "게시글을 삭제합니다.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
	})
	ResponseEntity<Void> deleteArticle(
			@Parameter(description = "삭제하고자 하는 게시글의 id", required = true, example = "1") Long articleId,
			@Parameter(description = "게시글을 삭제하는 유저의 id", required = true, example = "1") Long userId);
	
	
	@Operation(summary = "특정 게시글을 조회합니다.")
	ApiResponse<ArticleResponseDto> getArticle(
			@Parameter(description = "구체적으로 조회하고자 하는 게시글의 id", required = true, example = "1") Long articleId
	);
}
