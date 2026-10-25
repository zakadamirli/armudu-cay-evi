package az.armudu.cayevi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Admin girişi: serverdə ADMIN_USERNAME / ADMIN_PASSWORD mühit dəyişənləri ilə verilir. */
@ConfigurationProperties(prefix = "armudu.admin")
public record AdminProperties(String username, String password) {
}
