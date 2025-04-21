package t3h.edu.vn.traintickets.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // bước 1: Cáu hình security Filterchain
    // Bước 2: Cấu hình UserDetailService
    // bước 3: Cấu hinh passwordEncoder
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeHttpRequests((requests)
                        -> requests
                        .requestMatchers("/admin/**").authenticated()
                        .anyRequest().permitAll())
                .formLogin((
                        form) ->
                        form.loginPage("/login")
                                .usernameParameter("username")
                                .passwordParameter("password")
                                .loginProcessingUrl("/doLogin")
                                .defaultSuccessUrl("/admin/user/view")
                                .failureUrl("/login?error=true")
                                .permitAll())
                .logout((logout) -> logout.permitAll());
        return http.build();
    }
}
