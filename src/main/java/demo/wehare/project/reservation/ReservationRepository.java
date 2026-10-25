package demo.wehare.project.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("select coalesce(sum(r.guests), 0) from Reservation r where r.visitDate = :date and r.visitTime = :time")
    int sumGuests(@Param("date") LocalDate date, @Param("time") LocalTime time);

    List<Reservation> findByVisitDate(LocalDate date);

    List<Reservation> findByVisitDateGreaterThanEqualOrderByVisitDateAscVisitTimeAsc(LocalDate from);
}
