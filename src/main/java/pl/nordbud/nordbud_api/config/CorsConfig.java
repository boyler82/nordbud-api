package pl.nordbud.nordbud_api.config;


import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        // PROD + preview z Vercela + lokalny dev
                        .allowedOriginPatterns("https://*.vercel.app", "http://localhost:5173")
                        // preflight musi mieć POST i OPTIONS
                        .allowedMethods("GET", "POST", "OPTIONS")
                        // WAŻNE: zezwól na nagłówki używane w preflight
                        .allowedHeaders("Content-Type", "Authorization")
                        // (opcjonalnie) .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
