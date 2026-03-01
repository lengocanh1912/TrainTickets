package t3h.edu.vn.traintickets.config;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import t3h.edu.vn.traintickets.security.LogoutHandlerImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // bước 1: Cáu hình securityFilterchain
    // Bước 2: Cấu hình UserDetailService
    // bước 3: Cấu hinh passwordEncoder

    @Autowired
    private UserDetailsService userDetailsService;

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.csrf().disable()
////                .authenticationProvider(customAuthenticationProvider)
//                .authorizeHttpRequests((requests) -> requests
////                        .requestMatchers("/admin/**").authenticated()
//                        .requestMatchers("/admin/**").hasAnyRole("ADMIN","CUSTOMER")
////                        .requestMatchers("/user/**").hasRole("CUSTOMER")
////                        .requestMatchers("/user/**")
////                        .authenticated()
//                        .anyRequest().permitAll())
//                .formLogin((form) -> form
//                        .loginPage("/login")
//                        .loginProcessingUrl("/doLogin")
//                        .usernameParameter("username")
//                        .passwordParameter("password")
//                        .defaultSuccessUrl("/user/home")
////                        .successHandler(successHandler) // dùng custom handler ở đây
//                        .failureUrl("/login?error=true")
//                        .permitAll())
//                .logout(
//                        logout -> logout
//                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
//                                .permitAll()
//                                .logoutSuccessUrl("/user/home")
//                                .invalidateHttpSession(true)
//                                .deleteCookies("JSESSIONID")
//
//                );
//
//        return http.build();
//    }

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private LogoutHandlerImpl logoutHandlerImpl;
    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;
    @Autowired
    private CustomAuthFailureHandler customAuthFailureHandler;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http
                .getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(customAuthenticationProvider)
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf
                        .ignoringRequestMatchers("/ws-chat/**", "/api/**") // Bỏ CSRF cho WebSocket và Chat API
                )
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/sounds/**",
                                "/webjars/**"
                        ).permitAll()
                        // Admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // WebSocket & API chat
                        .requestMatchers("/ws-chat/**").permitAll()
                        .requestMatchers("/api/chat/**").permitAll()

                        // Public API
                        .requestMatchers("/api/trips/*/detail").permitAll()

                        // User cần login
                        .requestMatchers(
                                  "/user/support",
                                            "/user/account/**",
                                            "/user/order/**",
                                            "/user/payment/**"
                        ).authenticated()

                        // Còn lại cho phép
                        .anyRequest().permitAll()
                )

                // 👉 Xử lý khi chưa login
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {

                            String uri = request.getRequestURI();

                            // ==============================
                            // 1️⃣ Nếu là API → trả JSON 401
                            // ==============================
                            if (uri.startsWith(request.getContextPath() + "/api/")) {

                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");

                                response.getWriter().write("""
                                    {
                                      "status": 401,
                                      "error": "UNAUTHORIZED",
                                      "message": "Bạn cần đăng nhập để truy cập tài nguyên này"
                                    }
                                """);

                                return;
                            }

                            // ==================================
                            // 2️⃣ Nếu là UI → redirect về login
                            // ==================================

                            // Lưu full URL (bao gồm query param)
                            String fullUrl = request.getRequestURI()
                                    + (request.getQueryString() != null
                                    ? "?" + request.getQueryString()
                                    : "");

                            request.getSession().setAttribute("REDIRECT_AFTER_LOGIN", fullUrl);
                            request.getSession().setAttribute("LOGIN_REQUIRED", true);

                            response.sendRedirect(request.getContextPath() + "/login");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {

                            String uri = request.getRequestURI();

                            if (uri.startsWith(request.getContextPath() + "/api/")) {

                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType("application/json;charset=UTF-8");

                                response.getWriter().write("""
                                    {
                                      "status": 403,
                                      "error": "FORBIDDEN",
                                      "message": "Bạn không có quyền truy cập tài nguyên này"
                                    }
                                """);

                            } else {
                                response.sendRedirect(request.getContextPath() + "/403");
                            }
                        })

                )

                // 👉 Form login
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/doLogin")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureHandler(customAuthFailureHandler)
//                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                // 👉 Logout
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/user/home")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .addLogoutHandler(logoutHandlerImpl)
                        .permitAll()
                );

        return http.build();
    }
}
