package com.rengu.project.integrationoperations.configuration;

import com.rengu.project.integrationoperations.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsUtils;

/**
 * @author hanchangming
 * @date 2019-03-21
 */
@Order(value = -1)
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final MyAuthenticationSuccessHandler authenticationSuccessHandler;
    private final MyAuthenticationFailHandler authenticationFailHandler;

    @Lazy
    @Autowired
    private UserService userService;
    SessionRegistry sessionRegistry;

    public WebSecurityConfig(MyAuthenticationFailHandler authenticationFailHandler, MyAuthenticationSuccessHandler authenticationSuccessHandler) {
//        this.userService = userService;
        this.authenticationFailHandler = authenticationFailHandler;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
    }

    @Bean
    public SessionRegistry getSessionRegistry() {
        SessionRegistry sessionRegistry = new SessionRegistryImpl();
        return sessionRegistry;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and().authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .anyRequest().authenticated()
                .antMatchers("/users/**").permitAll()
                //  前端访问的登录地址
//                .requestMatchers().antMatchers(HttpMethod.OPTIONS, "/**")
                .and().formLogin().loginProcessingUrl("/user/login")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailHandler)
                .and().csrf().disable();
        http.sessionManagement().maximumSessions(1).maxSessionsPreventsLogin(true).sessionRegistry(sessionRegistry);
        http.csrf().disable();
        http.httpBasic();
        http.logout()
                .deleteCookies("JSESSIONID");
//                // 触发注销操作的URL
//                .logoutUrl("/logout")
//                //  注销成功后跳转的URL
////                .logoutSuccessUrl("/login.html")
//                //  指定是否在注销时让JSESSIONID无效
//
//                .invalidateHttpSession(true);
    }

}
