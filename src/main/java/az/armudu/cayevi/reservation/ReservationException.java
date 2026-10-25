package az.armudu.cayevi.reservation;

import org.springframework.http.HttpStatus;

/** Mesaj mətni deyil, mesaj kodu daşıyır; dilə uyğun mətn ApiExceptionHandler-də həll olunur. */
public class ReservationException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final transient Object[] args;

    public ReservationException(HttpStatus status, String code, Object... args) {
        super(code);
        this.status = status;
        this.code = code;
        this.args = args;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public Object[] getArgs() {
        return args;
    }
}
