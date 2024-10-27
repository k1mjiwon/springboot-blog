package me.jiwon.springbootdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.jiwon.springbootdeveloper.domain.Article;
import me.jiwon.springbootdeveloper.dto.AddArticleRequest;
import me.jiwon.springbootdeveloper.dto.UpdateArticleRequest;
import me.jiwon.springbootdeveloper.repository.BlogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BlogService {
    private final BlogRepository blogRepository;

    public Article save(AddArticleRequest request) {
        return blogRepository.save(request.toEntity());
    }

    public List<Article> findAll() {
        return blogRepository.findAll();
    }

    public Article findById(long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
    }

    public void delete(long id) {
        blogRepository.deleteById(id);
    }
    
    @Transactional // 매칭한 메서드를 하나의 트랜잭션으로 묶는 역할
    public Article update(long id, UpdateArticleRequest request) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found: " + id));
        
        article.update(request.getTitle(), request.getContent());
        return article;
    }
}
