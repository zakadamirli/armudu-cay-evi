package az.armudu.cayevi.reservation;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

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
    public ReservationResponse create(@Valid @RequestBody ReservationRequest request) {
        return ReservationResponse.from(service.create(request));
    }

    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable Long id) {
        return ReservationResponse.from(service.get(id));
    }

    @GetMapping
    public List<ReservationResponse> upcoming() {
        return service.upcoming().stream().map(ReservationResponse::from).toList();
    }

    @GetMapping("/slots")
    public List<SlotResponse> slots(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.slots(date).stream()
                .map(s -> new SlotResponse(s.time().format(HHMM), s.remaining()))
                .toList();
    }

    @GetMapping("/spots")
    public List<SpotResponse> spots() {
        return Arrays.stream(Spot.values()).map(s -> new SpotResponse(s, s.getLabel())).toList();
    }

    public record SlotResponse(String time, int remaining) {
    }

    public record SpotResponse(Spot value, String label) {
    }

    public record ReservationResponse(Long id, String name, LocalDate date, String time, int guests,
                                      Spot spot, String spotLabel, String note) {
        static ReservationResponse from(Reservation r) {
            return new ReservationResponse(r.getId(), r.getName(), r.getVisitDate(), r.getVisitTime().format(HHMM),
                    r.getGuests(), r.getSpot(), r.getSpot().getLabel(), r.getNote());
        }
    }
}
