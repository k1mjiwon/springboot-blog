package me.jiwon.springbootdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.jiwon.springbootdeveloper.domain.Article;
import me.jiwon.springbootdeveloper.dto.AddArticleRequest;
import me.jiwon.springbootdeveloper.dto.UpdateArticleRequest;
import me.jiwon.springbootdeveloper.repository.BlogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // MockMvc 생성 및 자동 구성
class BlogApiControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper; // 직렬화, 역직렬화를 위한 클래스

    @Autowired
    private WebApplicationContext context;

    @Autowired
    BlogRepository blogRepository;

    @BeforeEach // 테스트 전 실행 메서드
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        blogRepository.deleteAll();
    }

    @DisplayName("addArticle: 블로그 글 추가 성공")
    @Test
    public void addArticle() throws Exception {
        // 블로그 글 추가에 필요한 요청 객체 생성
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";
        final AddArticleRequest userRequest = new AddArticleRequest(title, content);

        // 객체를 JSON으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(userRequest);

        // JSON으로 글 추가 API에 요청 전송하며 생성한 객체를 본문으로 함께 전달
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // 요청코드가 201 Created인지 확인
        result.andExpect(status().isCreated());

        List<Article> articles = blogRepository.findAll(); // 블로그 전체 조회

        assertThat(articles.size()).isEqualTo(1); // 크기가 1인지 검증
        // 실제로 저장된 데이터와 요청 값을 비교
        assertThat(articles.get(0).getTitle()).isEqualTo(title);
        assertThat(articles.get(0).getContent()).isEqualTo(content);
    }

    @DisplayName("findAllArticles: 블로그 글 목록 조회 성공")
    @Test
    public void findAllArticles() throws Exception {
        // 블로그 글을 저장할 객체 생성
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";

        // 블로그 글 저장
        blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        // 목록 조회 API 호출
        final ResultActions resultActions = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON));

        // 응답 코드가 200 OK이며
        // 반환받은 값의 0번째 요소의 제목과 내용이 저장된 값과 동일한지 검증
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value(content))
                .andExpect(jsonPath("$[0].title").value(title));
    }

    @DisplayName("findArticle: 블로그 글 조회 성공")
    @Test
    public void findArticle() throws Exception {
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        // 블로그 글 저장
        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        // 저장한 블로그 글의 id값으로 API 호출
        final ResultActions resultActions = mockMvc.perform(get(url, savedArticle.getId()));

        // 응답 코드가 200 OK이며 반환받은 제목과 내용이 저장된 값과 동일한지 검증
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.title").value(title));
    }

    @DisplayName("deleteArticle: 블로그 글 삭제 성공")
    @Test
    public void deleteArticle() throws Exception {
        // 블로그 글 저장
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        // 저장한 블로그 글의 id값으로 삭제 API 호출
        mockMvc.perform(delete(url, savedArticle.getId()))
                .andExpect(status().isOk());

        // 응답 코드 200 OK 이며,
        // 블로그 글 리스트를 전체 조회해 조회한 배열 크기가 0인지 검증
        List<Article> articles = blogRepository.findAll();
        assertThat(articles).isEmpty();
    }

    @DisplayName("updateArticle: 블로그 글 수정 성공")
    @Test
    public void updateArticle() throws Exception {
        // 객체 생성
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        final String newTitle = "new title";
        final String newContent = "new content";

        UpdateArticleRequest request = new UpdateArticleRequest(newTitle, newContent);

        // JSON으로 위에서 생성한 객체를 update API로 요청 전달
        ResultActions result = mockMvc.perform(put(url, savedArticle.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        // 응답코드 200 OK 확인 및 수정여부 검증
        result.andExpect(status().isOk());

        Article article = blogRepository.findById(savedArticle.getId()).get();

        assertThat(article.getTitle()).isEqualTo(newTitle);
        assertThat(article.getContent()).isEqualTo(newContent);
    }
}