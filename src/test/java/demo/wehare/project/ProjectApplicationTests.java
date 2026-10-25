package demo.wehare.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:armudu-test;DB_CLOSE_DELAY=-1",
        "armudu.slot-capacity=10"
})
@AutoConfigureMockMvc
class ProjectApplicationTests {

    @Autowired
    MockMvc mvc;

    private final String tomorrow = LocalDate.now().plusDays(1).toString();

    private String body(String name, String time, int guests) {
        return """
                {"name":"%s","phone":"+994 50 123 45 67","date":"%s","time":"%s","guests":%d,"spot":"HEYET","note":""}
                """.formatted(name, tomorrow, time, guests);
    }

    @Test
    void menuIsSeededAndFilterable() throws Exception {
        mvc.perform(get("/api/menu")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(10)));
        mvc.perform(get("/api/menu").param("category", "CAY"))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].name").value("Lənkəran qara çayı"));
    }

    @Test
    void jamsAreSeeded() throws Exception {
        mvc.perform(get("/api/jams")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void createsReservationAndReducesSlot() throws Exception {
        mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(body("Leyla", "19:00", 4)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.time").value("19:00"))
                .andExpect(jsonPath("$.spotLabel").value("Həyətdə, tut ağacının altında"));

        mvc.perform(get("/api/reservations/slots").param("date", tomorrow))
                .andExpect(jsonPath("$[?(@.time == '19:00')].remaining").value(contains(6)))
                .andExpect(jsonPath("$[0].time").value("10:00"))
                .andExpect(jsonPath("$[-1].time").value("23:30"));
    }

    @Test
    void rejectsOverbookedSlot() throws Exception {
        mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(body("Orxan", "20:30", 8)))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(body("Nigar", "20:30", 3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("2 yer")));
    }

    @Test
    void rejectsInvalidInput() throws Exception {
        mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(body("", "19:00", 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists())
                .andExpect(jsonPath("$.fields.guests").exists());
        mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(body("Kamran", "19:15", 2)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(body("Kamran", "08:00", 2)))
                .andExpect(status().isBadRequest());
    }
}
