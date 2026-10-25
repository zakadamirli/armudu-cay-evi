package az.armudu.cayevi.reservation;

import az.armudu.cayevi.i18n.Lang;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody ReservationRequest request, Locale locale) {
        return ReservationResponse.from(service.create(request), Lang.of(locale));
    }

    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable Long id, Locale locale) {
        return ReservationResponse.from(service.get(id), Lang.of(locale));
    }

    @GetMapping
    public List<ReservationResponse> upcoming(Locale locale) {
        String lang = Lang.of(locale);
        return service.upcoming().stream().map(r -> ReservationResponse.from(r, lang)).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        service.cancel(id);
    }

    @GetMapping("/slots")
    public List<SlotResponse> slots(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.slots(date).stream()
                .map(s -> new SlotResponse(s.time().format(HHMM), s.remaining()))
                .toList();
    }

    @GetMapping("/spots")
    public List<SpotResponse> spots(Locale locale) {
        String lang = Lang.of(locale);
        return Arrays.stream(Spot.values()).map(s -> new SpotResponse(s, s.label(lang))).toList();
    }

    public record SlotResponse(String time, int remaining) {
    }

    public record SpotResponse(Spot value, String label) {
    }

    public record ReservationResponse(Long id, String name, LocalDate date, String time, int guests,
                                      Spot spot, String spotLabel, String note) {
        static ReservationResponse from(Reservation r, String lang) {
            return new ReservationResponse(r.getId(), r.getName(), r.getVisitDate(), r.getVisitTime().format(HHMM),
                    r.getGuests(), r.getSpot(), r.getSpot().label(lang), r.getNote());
        }
    }
}
