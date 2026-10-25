package az.armudu.cayevi.jam;

import az.armudu.cayevi.i18n.Lang;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/jams")
public class JamController {

    private final JamRepository repository;

    public JamController(JamRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<JamResponse> list(Locale locale) {
        String lang = Lang.of(locale);
        return repository.findAllByOrderBySortOrderAsc().stream().map(j -> JamResponse.from(j, lang)).toList();
    }

    public record JamResponse(Long id, String name, String color, String description, String pairing) {
        static JamResponse from(Jam j, String lang) {
            return new JamResponse(j.getId(), j.getName(lang), j.getColor(), j.getDescription(lang), j.getPairing(lang));
        }
    }
}
