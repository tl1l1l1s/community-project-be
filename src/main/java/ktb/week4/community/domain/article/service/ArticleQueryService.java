package ktb.week4.community.domain.article.service;

import ktb.week4.community.domain.article.dto.ArticleResponseDto;
import ktb.week4.community.domain.article.dto.GetArticlesResponseDto;
import ktb.week4.community.domain.article.entity.Article;
import ktb.week4.community.domain.article.repository.ArticleRepository;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.article.loader.ArticleLoader;
import ktb.week4.community.domain.user.enums.Status;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ArticleQueryService {
    private final ArticleRepository articleRepository;
	private final ArticleLoader articleLoader;

    public GetArticlesResponseDto getArticles(int page, int size) {
        Page<Article> articles = articleRepository.findAllBy(PageRequest.of(page-1, size));
		
		List<ArticleResponseDto> responses = articles.stream()
                .map(article -> {
					User user = article.getUser();
					if(user == null || user.getStatus() == Status.INACTIVE) {
						return ArticleResponseDto.fromEntity(article);
					}
                    return ArticleResponseDto.fromEntity(article, user);
                })
                .collect(Collectors.toList());

        return new GetArticlesResponseDto(responses,
				page,
				articles.getTotalElements(),
				articles.getTotalPages(),
				articles.isLast());
    }

    public ArticleResponseDto getArticle(Long articleId) {
        Article article = articleLoader.getArticleById(articleId);
		
		article.increaseViewCount();
        return ArticleResponseDto.fromEntity(article, article.getUser());
    }
}
