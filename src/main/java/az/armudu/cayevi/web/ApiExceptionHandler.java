package az.armudu.cayevi.web;

import az.armudu.cayevi.i18n.Lang;
import az.armudu.cayevi.reservation.ReservationException;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final MessageSource messages;

    public ApiExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    // Dil controller-lərlə eyni Accept-Language resolver-indən gəlir, dəstəklənən dilə normallaşır
    private String msg(Locale locale, String code, Object... args) {
        return messages.getMessage(code, args, code, Locale.of(Lang.of(locale)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalid(MethodArgumentNotValidException ex, Locale locale) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiError(msg(locale, "api.error.validation"), fields));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> unreadable(Exception ex, Locale locale) {
        return ResponseEntity.badRequest().body(new ApiError(msg(locale, "api.error.unreadable"), Map.of()));
    }

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ApiError> reservation(ReservationException ex, Locale locale) {
        return ResponseEntity.status(ex.getStatus()).body(new ApiError(msg(locale, ex.getCode(), ex.getArgs()), Map.of()));
    }

    public record ApiError(String message, Map<String, String> fields) {
    }
}
