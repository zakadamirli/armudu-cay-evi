package az.armudu.cayevi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/** Dil Accept-Language başlığından seçilir; standart az. */
@Configuration
public class WebConfig {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(List.of(Locale.of("az"), Locale.ENGLISH, Locale.of("ru")));
        resolver.setDefaultLocale(Locale.of("az"));
        return resolver;
    }
}
