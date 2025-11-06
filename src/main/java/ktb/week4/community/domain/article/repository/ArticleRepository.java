package ktb.week4.community.domain.article.repository;

import ktb.week4.community.domain.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
	@EntityGraph(attributePaths = {"user"})
	Page<Article> findAllBy(Pageable pageable);
}
