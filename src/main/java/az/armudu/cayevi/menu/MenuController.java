package az.armudu.cayevi.menu;

import az.armudu.cayevi.i18n.Lang;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuItemRepository repository;

    public MenuController(MenuItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<MenuItemResponse> list(@RequestParam(required = false) Category category, Locale locale) {
        String lang = Lang.of(locale);
        List<MenuItem> items = category == null
                ? repository.findAllByOrderBySortOrderAsc()
                : repository.findByCategoryOrderBySortOrderAsc(category);
        return items.stream().map(m -> MenuItemResponse.from(m, lang)).toList();
    }

    public record MenuItemResponse(Long id, Category category, String name, String description, BigDecimal price,
                                   String tag, Integer brewMinutes) {
        static MenuItemResponse from(MenuItem m, String lang) {
            return new MenuItemResponse(m.getId(), m.getCategory(), m.getName(lang), m.getDescription(lang),
                    m.getPrice(), m.getTag(lang), m.getBrewMinutes());
        }
    }
}
