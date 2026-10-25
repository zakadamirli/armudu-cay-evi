package az.armudu.cayevi.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final int SLOT_MINUTES = 30;

    private final ReservationRepository repository;
    private final ReservationProperties props;

    public ReservationService(ReservationRepository repository, ReservationProperties props) {
        this.repository = repository;
        this.props = props;
    }

    /** Günün bütün yarım saatlıq slotları və hər birində qalan yer sayı. Keçmiş saatlarda yer 0 göstərilir. */
    @Transactional(readOnly = true)
    public List<Slot> slots(LocalDate date) {
        Map<LocalTime, Integer> booked = repository.findByVisitDate(date).stream()
                .collect(Collectors.groupingBy(Reservation::getVisitTime, Collectors.summingInt(Reservation::getGuests)));
        LocalDateTime now = LocalDateTime.now();
        List<Slot> result = new ArrayList<>();
        LocalTime t = props.openingTime();
        while (!t.isAfter(props.lastSeating())) {
            int remaining = Math.max(0, props.slotCapacity() - booked.getOrDefault(t, 0));
            result.add(new Slot(t, date.atTime(t).isBefore(now) ? 0 : remaining));
            LocalTime next = t.plusMinutes(SLOT_MINUTES);
            if (next.isBefore(t)) break; // gecə yarısını keçdi
            t = next;
        }
        return result;
    }

    // synchronized: eyni slota eyni anda gələn iki rezerv tutumu aşmasın (tək instans üçün kifayətdir)
    @Transactional
    public synchronized Reservation create(ReservationRequest req) {
        LocalTime time = req.time();
        if (time.isBefore(props.openingTime()) || time.isAfter(props.lastSeating())
                || time.getMinute() % SLOT_MINUTES != 0 || time.getSecond() != 0 || time.getNano() != 0) {
            throw new ReservationException(HttpStatus.BAD_REQUEST,
                    "Rezerv " + props.openingTime() + " ilə " + props.lastSeating() + " arasında, yarım saatlıq aralıqlarla mümkündür");
        }
        if (req.date().atTime(time).isBefore(LocalDateTime.now())) {
            throw new ReservationException(HttpStatus.BAD_REQUEST, "Bu saat artıq keçib, başqa saat seçin");
        }
        if (req.date().isAfter(LocalDate.now().plusDays(props.maxDaysAhead()))) {
            throw new ReservationException(HttpStatus.BAD_REQUEST,
                    "Ən çox " + props.maxDaysAhead() + " gün qabağa rezerv etmək olar");
        }
        int remaining = props.slotCapacity() - repository.sumGuests(req.date(), time);
        if (req.guests() > remaining) {
            throw new ReservationException(HttpStatus.CONFLICT, remaining <= 0
                    ? "Saat " + time + " üçün yer qalmayıb, başqa saat seçin"
                    : "Saat " + time + " üçün cəmi " + remaining + " yer qalıb");
        }
        String note = req.note() == null || req.note().isBlank() ? null : req.note().trim();
        return repository.save(new Reservation(req.name().trim(), req.phone().trim(), req.date(), time, req.guests(), req.spot(), note));
    }

    @Transactional(readOnly = true)
    public Reservation get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ReservationException(HttpStatus.NOT_FOUND, "Belə rezerv tapılmadı"));
    }

    @Transactional(readOnly = true)
    public List<Reservation> upcoming() {
        return repository.findByVisitDateGreaterThanEqualOrderByVisitDateAscVisitTimeAsc(LocalDate.now());
    }

    @Transactional
    public void cancel(Long id) {
        if (!repository.existsById(id)) {
            throw new ReservationException(HttpStatus.NOT_FOUND, "Belə rezerv tapılmadı");
        }
        repository.deleteById(id);
    }

    public record Slot(LocalTime time, int remaining) {
    }
}
