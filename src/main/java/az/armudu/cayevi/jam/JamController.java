package az.armudu.cayevi.jam;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jams")
public class JamController {

    private final JamRepository repository;

    public JamController(JamRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<JamResponse> list() {
        return repository.findAllByOrderBySortOrderAsc().stream().map(JamResponse::from).toList();
    }

    public record JamResponse(Long id, String name, String color, String description, String pairing) {
        static JamResponse from(Jam j) {
            return new JamResponse(j.getId(), j.getName(), j.getColor(), j.getDescription(), j.getPairing());
        }
    }
}
