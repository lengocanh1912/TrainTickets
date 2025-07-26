package t3h.edu.vn.traintickets.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // bước 1: Cáu hình securityFilterchain
    // Bước 2: Cấu hình UserDetailService
    // bước 3: Cấu hinh passwordEncoder

    //test
//    @Autowired
//    private CustomAuthenticationProvider customAuthenticationProvider;

//    @Autowired
//    private CustomAuthenticationSuccessHandler successHandler;

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
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeHttpRequests((requests) -> requests
                // 👇 Admin phải có role
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "CUSTOMER")

                // 👇 Cho phép truy cập chi tiết chuyến tàu không cần login
                .requestMatchers("/api/trips/*/detail").permitAll()

                // 👇 Đặt vé phải login
                .requestMatchers("/api/trips/booking").authenticated()

                // 👇 Các trang còn lại (bao gồm cả modal, trang home...) không yêu cầu login
                .anyRequest().permitAll()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/doLogin")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(successHandler)  // ✅ dùng custom handler
//                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .permitAll()
                        .logoutSuccessUrl("/user/home")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }


}
