package com.ApiarioSamano.MicroServiceAlmacen.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Interceptor para agregar el JWT en cada request
        restTemplate.getInterceptors().add((request, body, execution) -> {
            String token = jwtTokenProvider.getCurrentJwtToken();
            if (token != null) {
                request.getHeaders().add("Authorization", "Bearer " + token);
            }
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
