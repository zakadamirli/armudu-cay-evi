package az.armudu.cayevi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:armudu-test;DB_CLOSE_DELAY=-1",
        "armudu.slot-capacity=10",
        "armudu.admin.username=admin",
        "armudu.admin.password=test-admin-pass"
})
@AutoConfigureMockMvc
@Transactional
class ArmuduApplicationTests {

    @Autowired
    MockMvc mvc;

    private final String tomorrow = LocalDate.now().plusDays(1).toString();

    private String body(String name, String time, int guests) {
        return full(name, "+994 50 123 45 67", tomorrow, time, guests, "HEYET", "");
    }

    private String full(String name, String phone, String date, String time, int guests, String spot, String note) {
        return """
                {"name":"%s","phone":"%s","date":"%s","time":"%s","guests":%d,"spot":"%s","note":%s}
                """.formatted(name, phone, date, time, guests, spot,
                note == null ? "null" : "\"" + note + "\"");
    }

    private MockHttpServletRequestBuilder postJson(String content) {
        return post("/api/reservations").contentType(MediaType.APPLICATION_JSON).content(content);
    }

    // ---------- menyu / mürəbbə ----------

    @Test
    void menuIsSeededAndFilterable() throws Exception {
        mvc.perform(get("/api/menu")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(10)));
        mvc.perform(get("/api/menu").param("category", "CAY"))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].name").value("Lənkəran qara çayı"));
    }

    @Test
    void rejectsUnknownMenuCategory() throws Exception {
        mvc.perform(get("/api/menu").param("category", "XYZ")).andExpect(status().isBadRequest());
    }

    @Test
    void jamsAreSeeded() throws Exception {
        mvc.perform(get("/api/jams")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(6)));
    }

    // ---------- rezerv: əsas axın ----------

    @Test
    void createsReservationAndReducesSlot() throws Exception {
        mvc.perform(postJson(body("Leyla", "19:00", 4)))
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
    void acceptsBoundaryTimes() throws Exception {
        mvc.perform(postJson(body("Açılış", "10:00", 2))).andExpect(status().isCreated());
        mvc.perform(postJson(body("Son masa", "23:30", 2))).andExpect(status().isCreated());
    }

    @Test
    void acceptsExactlyAtCapacityThenRejectsOver() throws Exception {
        mvc.perform(postJson(body("Tam", "18:00", 10))).andExpect(status().isCreated());
        mvc.perform(postJson(body("Artıq", "18:00", 1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("yer qalmayıb")));
    }

    @Test
    void rejectsOverbookedSlot() throws Exception {
        mvc.perform(postJson(body("Orxan", "20:30", 8))).andExpect(status().isCreated());
        mvc.perform(postJson(body("Nigar", "20:30", 3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("2 yer")));
    }

    // ---------- rezerv: validasiya ----------

    @Test
    void rejectsInvalidInput() throws Exception {
        mvc.perform(postJson(body("", "19:00", 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists())
                .andExpect(jsonPath("$.fields.guests").exists());
        mvc.perform(postJson(body("Kamran", "19:15", 2))).andExpect(status().isBadRequest());
        mvc.perform(postJson(body("Kamran", "08:00", 2))).andExpect(status().isBadRequest());
    }

    @Test
    void rejectsGuestsAboveMax() throws Exception {
        mvc.perform(postJson(body("Böyük qrup", "19:00", 13)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.guests").exists());
    }

    @Test
    void rejectsInvalidPhoneAndAcceptsFormattedValid() throws Exception {
        mvc.perform(postJson(full("Qısa", "12", tomorrow, "19:00", 2, "HEYET", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.phone").exists());
        mvc.perform(postJson(full("Düzgün", "+994 (50) 123-45-67", tomorrow, "19:30", 2, "HEYET", "")))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsBeyondMaxDaysAhead() throws Exception {
        String tooFar = LocalDate.now().plusDays(61).toString();
        mvc.perform(postJson(full("Uzaq", "+994 50 123 45 67", tooFar, "12:00", 2, "HEYET", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("60 gün")));
    }

    @Test
    void acceptsAtMaxDaysAhead() throws Exception {
        String maxDay = LocalDate.now().plusDays(60).toString();
        mvc.perform(postJson(full("Sərhəd", "+994 50 123 45 67", maxDay, "12:00", 2, "HEYET", "")))
                .andExpect(status().isCreated());
    }

    @Test
    void trimsNoteAndNullsBlank() throws Exception {
        mvc.perform(postJson(full("Qeydli", "+994 50 123 45 67", tomorrow, "14:00", 2, "EYVAN", "  Ad günü  ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.note").value("Ad günü"));
        mvc.perform(postJson(full("Boş qeyd", "+994 50 123 45 67", tomorrow, "14:30", 2, "EYVAN", "   ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.note").value(nullValue()));
    }

    @Test
    void getReservationByIdFoundAndMissing() throws Exception {
        String location = mvc.perform(postJson(body("Tapılan", "15:00", 2)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = Integer.parseInt(location.replaceAll(".*\"id\":(\\d+).*", "$1"));
        mvc.perform(get("/api/reservations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tapılan"));
        mvc.perform(get("/api/reservations/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ---------- çoxdillilik ----------

    @Test
    void menuLocalizedByAcceptLanguage() throws Exception {
        mvc.perform(get("/api/menu").param("category", "CAY").header("Accept-Language", "en"))
                .andExpect(jsonPath("$[0].name").value("Lankaran black tea"));
        mvc.perform(get("/api/menu").param("category", "CAY").header("Accept-Language", "ru"))
                .andExpect(jsonPath("$[0].name").value("Ланкяранский чёрный чай"));
        mvc.perform(get("/api/menu").param("category", "CAY"))
                .andExpect(jsonPath("$[0].name").value("Lənkəran qara çayı"));
    }

    @Test
    void errorMessagesLocalized() throws Exception {
        mvc.perform(postJson(body("Full EN", "18:00", 10)).header("Accept-Language", "en"))
                .andExpect(status().isCreated());
        mvc.perform(postJson(body("Over EN", "18:00", 1)).header("Accept-Language", "en"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("No seats left")));
        mvc.perform(postJson(body("Over RU", "18:00", 1)).header("Accept-Language", "ru"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("мест")));
    }

    // ---------- təhlükəsizlik ----------

    @Test
    void reservationListRequiresAdmin() throws Exception {
        mvc.perform(get("/api/reservations")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/reservations").with(httpBasic("admin", "test-admin-pass")))
                .andExpect(status().isOk());
    }

    @Test
    void cancelRequiresAdminAndRestoresSlot() throws Exception {
        String res = mvc.perform(postJson(body("Ləğv", "21:00", 5)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = Integer.parseInt(res.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mvc.perform(delete("/api/reservations/{id}", id)).andExpect(status().isUnauthorized());

        mvc.perform(delete("/api/reservations/{id}", id).with(httpBasic("admin", "test-admin-pass")))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/reservations/slots").param("date", tomorrow))
                .andExpect(jsonPath("$[?(@.time == '21:00')].remaining").value(contains(10)));
    }

    @Test
    void securityHeadersArePresent() throws Exception {
        mvc.perform(get("/api/menu"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().exists("Content-Security-Policy"));
    }
}
