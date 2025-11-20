package ktb.week4.community.domain.article.service;

import ktb.week4.community.domain.article.dto.ArticleResponseDto;
import ktb.week4.community.domain.article.dto.CreateArticleRequestDto;
import ktb.week4.community.domain.article.dto.UpdateArticleRequestDto;
import ktb.week4.community.domain.article.entity.Article;
import ktb.week4.community.domain.article.loader.ArticleLoader;
import ktb.week4.community.domain.article.policy.ArticlePolicy;
import ktb.week4.community.domain.article.repository.ArticleRepository;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.user.loader.UserLoader;
import ktb.week4.community.global.file.FileStorageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
@Transactional
public class ArticleCommandService {
	
	private final ArticleRepository articleRepository;
	private final UserLoader userLoader;
	private final ArticleLoader articleLoader;
	private final ArticlePolicy articlePolicy;
	private final FileStorageService fileStorageService;
	
	public ArticleResponseDto createArticle(Long userId, CreateArticleRequestDto request, MultipartFile image) {
		User writtenBy = userLoader.getUserById(userId);
		String articleImage = request.articleImage();
		String storedImagePath = fileStorageService.store(image, "articles");
		if (StringUtils.hasText(storedImagePath)) {
			articleImage = storedImagePath;
		} else if (!StringUtils.hasText(articleImage)) {
			articleImage = null;
		}
		
		Article article = articleRepository.save(
				new Article(
						request.title(),
						request.content(),
						articleImage,
						writtenBy
				)
		);
		
		return ArticleResponseDto.fromEntity(article, writtenBy);
	}
	
	public ArticleResponseDto updateArticle(Long userId, Long articleId, UpdateArticleRequestDto request, MultipartFile image) {
		User writtenBy = userLoader.getUserById(userId);
		Article article = articleLoader.getArticleById(articleId);
		
		articlePolicy.checkWrittenBy(article, userId);
		
		if(StringUtils.hasText(request.title())) {
			article.changeTitle(request.title());
		}
		if(StringUtils.hasText(request.content())) {
			article.changeContent(request.content());
		}
		if (image != null && !image.isEmpty()) {
			fileStorageService.delete(article.getArticleImage());
			String storedImagePath = fileStorageService.store(image, "articles");
			if (StringUtils.hasText(storedImagePath)) {
				article.changeArticleImage(storedImagePath);
			}
		} else if (request.articleImage() != null) {
			if (!StringUtils.hasText(request.articleImage())) {
				fileStorageService.delete(article.getArticleImage());
				article.changeArticleImage(null);
			} else {
				article.changeArticleImage(request.articleImage());
			}
		}
		
		Article updatedArticle = articleRepository.save(article);
		return ArticleResponseDto.fromEntity(updatedArticle, writtenBy);
	}
	
	public void deleteArticle(Long userId, Long articleId) {
		Article article = articleLoader.getArticleById(articleId);
		articlePolicy.checkWrittenBy(article, userId);
		fileStorageService.delete(article.getArticleImage());
		
		article.deleteArticle();
		articleRepository.save(article);
	}
}
