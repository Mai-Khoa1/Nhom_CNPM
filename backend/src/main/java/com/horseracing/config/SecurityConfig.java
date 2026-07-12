package com.horseracing.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Cấu hình Spring Security: JWT stateless, CORS, phân quyền theo Role (ADMIN, ORGANIZER, HORSE_OWNER, SPECTATOR).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // Auth công khai
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/health").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html",
                                "/v3/api-docs/**",
                                "/api/v1/swagger-ui/**",
                                "/api/v1/swagger-ui.html",
                                "/api/v1/swagger-ui/index.html",
                                "/api/v1/v3/api-docs/**"
                        ).permitAll()

                        // Xem công khai (Guest) - danh sách/chi tiết ngựa, jockey, chặng đua, kết quả,
                        // mùa giải, bảng xếp hạng
                        .requestMatchers(HttpMethod.GET,
                                "/horses/**", "/jockeys/**", "/races/**", "/results/**",
                                "/seasons/**", "/rankings/**").permitAll()

                        // Quản lý người dùng - chỉ Admin (trừ /users/me)
                        .requestMatchers("/users/me").authenticated()
                        .requestMatchers("/users/**").hasRole("ADMIN")

                        // Thống kê công khai (dùng cho trang chủ)
                        .requestMatchers(HttpMethod.GET, "/dashboard/stats", "/dashboard/upcoming").permitAll()

                        // Danh sách Ban tổ chức - Chủ ngựa cần để chọn khi tạo ngựa/nài/đăng ký thi đấu
                        .requestMatchers(HttpMethod.GET, "/organizers/**").authenticated()

                        // Nhật ký hoạt động, dashboard - Admin và Ban tổ chức
                        .requestMatchers("/nhat-ky-hoat-dong/**").hasAnyRole("ADMIN", "ORGANIZER")
                        .requestMatchers("/dashboard/**").hasAnyRole("ADMIN", "ORGANIZER")

                        // Tạo/sửa/xóa ngựa, jockey, hồ sơ chủ ngựa - Chủ ngựa, Ban tổ chức, Admin
                        .requestMatchers(HttpMethod.POST, "/horses/**", "/jockeys/**", "/chu-ngua/**")
                            .hasAnyRole("HORSE_OWNER", "ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/horses/**", "/jockeys/**", "/chu-ngua/**")
                            .hasAnyRole("HORSE_OWNER", "ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/horses/**", "/jockeys/**", "/chu-ngua/**")
                            .hasAnyRole("HORSE_OWNER", "ORGANIZER", "ADMIN")

                        // Mùa giải, chặng đua, kết quả, làn đua - chỉ Ban tổ chức và Admin được tạo/sửa/xóa
                        .requestMatchers(HttpMethod.POST, "/seasons/**", "/races/**", "/results/**", "/lanes/**")
                            .hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/seasons/**", "/races/**", "/results/**", "/lanes/**")
                            .hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/seasons/**", "/races/**", "/results/**")
                            .hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/seasons/**", "/races/**", "/lanes/**")
                            .hasAnyRole("ORGANIZER", "ADMIN")

                        // Đăng ký thi đấu - tạo bởi Chủ ngựa, duyệt/từ chối bởi Ban tổ chức/Admin
                        .requestMatchers(HttpMethod.GET, "/registrations/my").hasRole("HORSE_OWNER")
                        .requestMatchers(HttpMethod.GET, "/registrations/**").hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/registrations/**").hasRole("HORSE_OWNER")
                        .requestMatchers(HttpMethod.PATCH, "/registrations/**").hasAnyRole("ORGANIZER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/registrations/**").hasRole("HORSE_OWNER")

                        // Yêu cầu cập nhật thông tin ngựa/nài - chỉ Ban tổ chức/Admin xem và duyệt/từ chối
                        .requestMatchers("/update-requests/**").hasAnyRole("ORGANIZER", "ADMIN")

                        // Thông báo - mọi người dùng đã đăng nhập (tự xem của mình)
                        .requestMatchers("/notifications/**").authenticated()

                        // Tệp tin - chỉ Chủ ngựa (CRUD tệp của chính mình) và Ban tổ chức (xem/duyệt/từ chối).
                        // Admin KHÔNG có quyền trên API tệp tin, kể cả gọi trực tiếp qua URL.
                        .requestMatchers(HttpMethod.POST, "/upload").hasRole("HORSE_OWNER")
                        .requestMatchers(HttpMethod.PUT, "/upload/**").hasRole("HORSE_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/upload/**").hasRole("HORSE_OWNER")
                        .requestMatchers(HttpMethod.GET, "/upload/**").hasAnyRole("HORSE_OWNER", "ORGANIZER")

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
