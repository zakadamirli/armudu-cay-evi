package az.armudu.cayevi.reservation;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationRequest(
        @NotBlank(message = "{reservation.name.blank}")
        @Size(max = 80, message = "{reservation.name.size}")
        String name,

        @NotBlank(message = "{reservation.phone.blank}")
        @Pattern(regexp = "^\\+?[0-9 ()-]{9,20}$", message = "{reservation.phone.pattern}")
        String phone,

        @NotNull(message = "{reservation.date.null}")
        @FutureOrPresent(message = "{reservation.date.past}")
        LocalDate date,

        @NotNull(message = "{reservation.time.null}")
        LocalTime time,

        @Min(value = 1, message = "{reservation.guests.min}")
        @Max(value = 12, message = "{reservation.guests.max}")
        int guests,

        @NotNull(message = "{reservation.spot.null}")
        Spot spot,

        @Size(max = 500, message = "{reservation.note.size}")
        String note
) {
}
