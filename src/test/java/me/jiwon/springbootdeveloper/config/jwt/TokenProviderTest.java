package me.jiwon.springbootdeveloper.config.jwt;

import io.jsonwebtoken.Jwts;
import me.jiwon.springbootdeveloper.domain.User;
import me.jiwon.springbootdeveloper.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class TokenProviderTest {
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtProperties jwtProperties;

    @DisplayName("generateToken(): 유저 정보와 만료 기간을 전달해 토큰 생성")
    @Test
    void generateToken() {
        // 토큰에 유저 정보를 추가하기 위하여 테스트 유저 생성
        User testUser = userRepository.save(User.builder()
                .email("user@gmail.com")
                .password("test")
                .build());

        // 토큰 제공자의 generateToken() 메서드를 호출하여 토큰 생성
        String token = tokenProvider.generateToken(testUser, Duration.ofDays(14));

        // jjwt 라이브러리를 사용하여 토큰 복호화
        // 토큰 생성시 클레임으로 넣어둔 id값이 유저 ID와 동일한지 확인
        Long userId = Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .get("id", Long.class);

        assertThat(userId).isEqualTo(testUser.getId());
    }

    @DisplayName("validaToken(): 만료된 토큰일 경우 유효성 검증에 실패")
    @Test
    void validToken_invalidToken() {
        // jjwt 라이브러리로 토큰 생성(만료된 토큰으로 생성)
        String token = JwtFactory.builder()
                .expiration(new Date(new Date().getTime() - Duration.ofDays(7).toMillis()))
                .build()
                .createToken(jwtProperties);

        // 토큰 제공자의 validToken() 메서드를 호출해 유효한 토큰인지 검증 뒤 결과값 반환
        boolean result = tokenProvider.validToken(token);

        // 반환갑싱 false인 것을 확인
        assertThat(result).isFalse();
    }

    @DisplayName("validToken(): 유효한 토큰일 경우 유효성 검증에 성공")
    @Test
    void validToken_validToken() {
        // jjwt 라이브러리를 사용하여 만료되지 않은 토큰 생성
        String token = JwtFactory.withDefaultValues().createToken(jwtProperties);

        // validToken() 메서드를 호출하여 유효한 토큰인지 검증 뒤 결괏값 반환
        boolean result = tokenProvider.validToken(token);

        // 반환값이 true인 것을 확인
        assertThat(result).isTrue();
    }

    @DisplayName("getAuthentication(): 토큰 기반으로 인증 정보 조회")
    @Test
    void getAuthentication() {
        // 토큰 생성
        String userEmail = "user@email.com";
        String token = JwtFactory.builder()
                .subject(userEmail)
                .build()
                .createToken(jwtProperties);

        // 인증 객체를 반환
        Authentication authentication = tokenProvider.getAuthentication(token);

        // 인증 객체의 유저 이름이 앞에서 설정한 subject값과 같은지 확인
        assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).isEqualTo(userEmail);
    }

    @DisplayName("getUserId(): 토큰으로 유저 ID 조회")
    @Test
    void getUserId() {
        // 토큰 생성
        Long userId = 1L;
        String token = JwtFactory.builder().claims(Map.of("id", userId))
                .build()
                .createToken(jwtProperties);

        // 유저 ID 반환
        Long userIdByToken = tokenProvider.getUserId(token);

        // 반환받은 유저 ID와 최초 생성한 ID값이 동일한지 확인
        assertThat(userIdByToken).isEqualTo(userId);
    }
}
