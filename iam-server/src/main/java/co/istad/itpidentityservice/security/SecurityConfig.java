package co.istad.itpidentityservice.security;

import co.istad.itpidentityservice.config.CustomUserDetails;
import co.istad.itpidentityservice.feature.oauth2.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final PasswordEncoder passwordEncoder;

    @Value("${spring.security.oauth2.authorizationserver.issuer}")
    private String issuerUri;

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(issuerUri)
                .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http.apply(authorizationServerConfigurer);
        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, authorizationServer ->
                        authorizationServer
                                .oidc(Customizer.withDefaults())    // Enable OpenID Connect 1.0
                )
                .authorizeHttpRequests(authorize ->
                        authorize
                                .anyRequest().authenticated()
                )
                // Redirect to the login page when not authenticated
                // authorization endpoint
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );
        http.cors(Customizer.withDefaults());
        return http.build();
    }


    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                );
        http.cors(Customizer.withDefaults());
        return http.build();
    }

    // custom jwt
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            Authentication authentication = context.getPrincipal();
            if (authentication.getPrincipal() instanceof CustomUserDetails user) {

                JwtClaimsSet.Builder claims = context.getClaims();

                // Required: sub must never be null → fallback to username
                claims.claim("sub", nonNullOr(user.getUuid(), user.getUsername()));

                // Safe helper: only add claim if value is not null
                claimIfNotNull(claims, "email", user.getEmail());
                claimIfNotNull(claims, "name", user.getFullName());
                claimIfNotNull(claims, "given_name", user.getGivenName());
                claimIfNotNull(claims, "family_name", user.getFamilyName());
                claimIfNotNull(claims, "phone_number", user.getPhoneNumber());
                claimIfNotNull(claims, "gender", user.getGender());
                claimIfNotNull(claims, "picture", user.getProfileImage());
                claimIfNotNull(claims, "uuid", user.getUuid());

                // birthdate → format as String (ISO date), skip if null
                if (user.getDob() != null) {
                    claims.claim("birthdate", user.getDob().toString());
                }

                // Separate roles and permissions
                Set<String> allAuthorities = user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());

                // Roles: authorities starting with "ROLE_"
                Set<String> roles = allAuthorities.stream()
                        .filter(auth -> auth.startsWith("ROLE_"))
                        .collect(Collectors.toSet());
                claims.claim("roles", roles);

                // Authorities: all granted authorities (roles + permissions combined)
//                claims.claim("authorities", allAuthorities);

                // Scope (required for access token)
                if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                    String scope = context.getAuthorizedScopes() != null
                            ? String.join(" ", context.getAuthorizedScopes())
                            : String.join(" ", allAuthorities);
                    claims.claim("scope", scope);
                }
            }
        };
    }

    // cors config
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3333", "http://localhost:3333/"));  // your frontend origin
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS", "HEAD"));  // add what you need
        configuration.setAllowedHeaders(List.of("*"));  // or specific: "Authorization", "Content-Type"
        configuration.setAllowCredentials(true);  // important if using cookies/sessions
        configuration.setMaxAge(3600L);  // cache preflight 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // apply to all endpoints
        return source;
    }

    // methods — put them in the same class or as static utilities
    private static String nonNullOr(String value, String fallback) {
        return value != null ? value : fallback;
    }

    private static void claimIfNotNull(JwtClaimsSet.Builder claims, String name, Object value) {
        if (value != null) {
            claims.claim(name, value);
        }
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


}
