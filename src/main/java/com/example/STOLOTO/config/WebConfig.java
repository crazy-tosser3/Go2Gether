package com.example.STOLOTO.config;

import java.io.IOException;
import java.util.Locale;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new CaseInsensitiveEnumConverterFactory());
    }

    // Собранный фронт лежит в classpath:/static. Неизвестный путь вне API отдаём как index.html —
    // маршрутизацию дальше делает vue-router (history mode).
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new SpaResourceResolver());
    }

    private static final class SpaResourceResolver extends PathResourceResolver {
        private static final Resource INDEX = new ClassPathResource("static/index.html");

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource resource = super.getResource(resourcePath, location);
            if (resource != null) {
                return resource;
            }
            if (resourcePath.startsWith("api/") || resourcePath.startsWith("actuator")
                    || resourcePath.startsWith("v3/") || resourcePath.startsWith("swagger-ui")) {
                return null;
            }
            return INDEX.exists() ? INDEX : null;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class CaseInsensitiveEnumConverterFactory implements ConverterFactory<String, Enum> {
        @Override
        public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
            return source -> source.isBlank()
                    ? null
                    : (T) Enum.valueOf(targetType, source.trim().toUpperCase(Locale.ROOT));
        }
    }
}
