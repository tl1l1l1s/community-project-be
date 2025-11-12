package ktb.week4.community.domain.comment.service;

import ktb.week4.community.domain.article.entity.Article;
import ktb.week4.community.domain.article.repository.ArticleRepository;
import ktb.week4.community.domain.comment.dto.CommentResponseDto;
import ktb.week4.community.domain.comment.dto.CreateCommentRequestDto;
import ktb.week4.community.domain.comment.dto.UpdateCommentRequestDto;
import ktb.week4.community.domain.comment.entity.Comment;
import ktb.week4.community.domain.comment.policy.CommentPolicy;
import ktb.week4.community.domain.comment.repository.CommentRepository;
import ktb.week4.community.domain.user.entity.User;
import ktb.week4.community.domain.article.loader.ArticleLoader;
import ktb.week4.community.domain.comment.loader.CommentLoader;
import ktb.week4.community.domain.user.loader.UserLoader;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class CommentCommandService {
	
	private final CommentRepository commentRepository;
	private final UserLoader userLoader;
	private final ArticleLoader articleLoader;
	private final CommentLoader commentLoader;
	private final CommentPolicy commentPolicy;
	
	private final ArticleRepository articleRepository;
	
	public CommentResponseDto createComment(Long userId, Long articleId, CreateCommentRequestDto request) {
		User user = userLoader.getUserById(userId);
		Article article = articleLoader.getArticleById(articleId);
		
		Comment comment = new Comment(request.content(), user, article);
		Comment savedComment = commentRepository.save(comment);

		article.increaseCommentCount();
		articleRepository.save(article);
		return CommentResponseDto.fromEntity(savedComment, user);
	}
	
	public CommentResponseDto updateComment(Long articleId, Long userId, Long commentId, UpdateCommentRequestDto request) {
		Comment comment = commentLoader.getCommentById(commentId);
		
		commentPolicy.checkWrittenBy(comment, userId);
		commentPolicy.checkBelongsTo(comment, articleId);
		
		comment.changeContent(request.content());
		Comment updatedComment = commentRepository.save(comment);
		
		User user = userLoader.getUserById(userId);
		
		return CommentResponseDto.fromEntity(updatedComment, user);
	}
	
	public void deleteComment(Long articleId, Long userId, Long commentId) {
		Comment comment = commentLoader.getCommentById(commentId);
		Article article = articleLoader.getArticleById(articleId);
		
		commentPolicy.checkWrittenBy(comment, userId);
		commentPolicy.checkBelongsTo(comment, articleId);
		
		commentRepository.delete(comment);
		article.decreaseCommentCount();
		articleRepository.save(article);
	}
}
