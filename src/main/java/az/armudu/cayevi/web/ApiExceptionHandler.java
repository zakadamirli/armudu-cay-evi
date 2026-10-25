package az.armudu.cayevi.web;

import az.armudu.cayevi.reservation.ReservationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalid(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiError("Formada səhvlər var", fields));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> unreadable(Exception ex) {
        return ResponseEntity.badRequest().body(new ApiError("Göndərilən məlumat düzgün formatda deyil", Map.of()));
    }

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ApiError> reservation(ReservationException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ApiError(ex.getMessage(), Map.of()));
    }

    public record ApiError(String message, Map<String, String> fields) {
    }
}
