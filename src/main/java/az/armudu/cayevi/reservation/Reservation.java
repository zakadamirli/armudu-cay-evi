package az.armudu.cayevi.reservation;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reservations", indexes = @Index(name = "idx_reservation_slot", columnList = "visitDate, visitTime"))
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 25)
    private String phone;

    @Column(nullable = false)
    private LocalDate visitDate;

    @Column(nullable = false)
    private LocalTime visitTime;

    @Column(nullable = false)
    private int guests;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Spot spot;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private Instant createdAt;

    protected Reservation() {
    }

    public Reservation(String name, String phone, LocalDate visitDate, LocalTime visitTime, int guests, Spot spot, String note) {
        this.name = name;
        this.phone = phone;
        this.visitDate = visitDate;
        this.visitTime = visitTime;
        this.guests = guests;
        this.spot = spot;
        this.note = note;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public LocalDate getVisitDate() { return visitDate; }
    public LocalTime getVisitTime() { return visitTime; }
    public int getGuests() { return guests; }
    public Spot getSpot() { return spot; }
    public String getNote() { return note; }
    public Instant getCreatedAt() { return createdAt; }
}
