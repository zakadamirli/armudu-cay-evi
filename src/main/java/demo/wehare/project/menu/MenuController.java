package demo.wehare.project.menu;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuItemRepository repository;

    public MenuController(MenuItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<MenuItemResponse> list(@RequestParam(required = false) Category category) {
        List<MenuItem> items = category == null
                ? repository.findAllByOrderBySortOrderAsc()
                : repository.findByCategoryOrderBySortOrderAsc(category);
        return items.stream().map(MenuItemResponse::from).toList();
    }

    public record MenuItemResponse(Long id, Category category, String name, String description, BigDecimal price, String tag) {
        static MenuItemResponse from(MenuItem m) {
            return new MenuItemResponse(m.getId(), m.getCategory(), m.getName(), m.getDescription(), m.getPrice(), m.getTag());
        }
    }
}
