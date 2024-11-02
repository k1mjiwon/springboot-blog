package me.jiwon.springbootdeveloper.config;

import lombok.RequiredArgsConstructor;
import me.jiwon.springbootdeveloper.service.UserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final UserDetailService userService;

    // 스프링 시큐리티의 모든 기능을 사용하지 않게 설정하는 코드
    // 즉, 인증/인가 서비스를 모든 곳에 적용하지는 않음
    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring()
                .requestMatchers(toH2Console())
                .requestMatchers(new AntPathRequestMatcher("/static/**"));
        // static 하위 경로에 있는 리소스와 h2-console 하위 url을 대상으로 ignoring() 메서드 사용
    }

    // 특정 HTTP 요청에 대한 웹 기반 보안 구성
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeRequests(auth -> auth // 특정 경로에 대한 액세스 설정
                        .requestMatchers( // 특정 요청과 일치하는 url에 대한 액세스 설정
                                new AntPathRequestMatcher("/login"),
                                new AntPathRequestMatcher("/signup"),
                                new AntPathRequestMatcher("/user")
                                // login, signup, user로 요청이 오면 인증/인가 없이도 접근 가능
                        ).permitAll()
                        .anyRequest().authenticated())
                        // anyRequest(): 위 url 이외의 요청에 대해서 설정
                        // authenticated(): 별도 인가는 필요없지만 인증이 성공된 상태여야 접근 가능
                .formLogin(formLogin -> formLogin // 폼 기반 로그인 설정
                        .loginPage("/login") // 로그인 페이지 경로 설정
                        .defaultSuccessUrl("/articles") // 로그인 완료시 이동할 경로 설정
                )
                .logout(logout -> logout // 로그아웃 설정
                        .logoutSuccessUrl("/login") // 로그아웃 완료시 이동할 경로 설정
                        .invalidateHttpSession(true) // 로그아웃 후 세션을 전체 삭제할지 여부 설정
                )
                .csrf(AbstractHttpConfigurer::disable) // CSRF 공격 방지 설정 비활성화
                .build();
    }

    // 인증 관리자 관련 설정으로 사용자 정보를 가져올 서비스를 재정의하거나
    // LDAP, JDBC 기반 인증 등의 인증 방법을 설정할 때 사용
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder, UserDetailService userDetailService)
        throws Exception {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService); // 사용자 정보 서비스 설정
        authProvider.setPasswordEncoder(bCryptPasswordEncoder); // 비밀번호 암호화를 하기 위한 인코더 설정
        return new ProviderManager(authProvider);
    }

    // 패스워드 인코더로 사용할 빈 등록
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
