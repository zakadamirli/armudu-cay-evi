package demo.wehare.project.reservation;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationRequest(
        @NotBlank(message = "Adınızı yazın")
        @Size(max = 80, message = "Ad 80 simvoldan uzun ola bilməz")
        String name,

        @NotBlank(message = "Telefon nömrəsini yazın")
        @Pattern(regexp = "^\\+?[0-9 ()-]{9,20}$", message = "Telefon nömrəsi ən azı 9 rəqəm olmalıdır")
        String phone,

        @NotNull(message = "Tarixi seçin")
        @FutureOrPresent(message = "Keçmiş tarixə rezerv etmək olmur")
        LocalDate date,

        @NotNull(message = "Saatı seçin")
        LocalTime time,

        @Min(value = 1, message = "Ən azı 1 qonaq olmalıdır")
        @Max(value = 12, message = "12 nəfərdən çox qonaq üçün bizə zəng edin")
        int guests,

        @NotNull(message = "Yeri seçin")
        Spot spot,

        @Size(max = 500, message = "Qeyd 500 simvoldan uzun ola bilməz")
        String note
) {
}
