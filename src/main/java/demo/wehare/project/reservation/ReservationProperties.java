package demo.wehare.project.reservation;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalTime;

@ConfigurationProperties(prefix = "armudu")
public record ReservationProperties(int slotCapacity, LocalTime openingTime, LocalTime lastSeating, int maxDaysAhead) {
}
