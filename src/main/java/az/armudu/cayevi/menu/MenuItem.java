package az.armudu.cayevi.menu;

import az.armudu.cayevi.i18n.Text;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "az", column = @Column(name = "name_az", nullable = false)),
            @AttributeOverride(name = "en", column = @Column(name = "name_en")),
            @AttributeOverride(name = "ru", column = @Column(name = "name_ru"))
    })
    private Text name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "az", column = @Column(name = "desc_az", nullable = false, length = 600)),
            @AttributeOverride(name = "en", column = @Column(name = "desc_en", length = 600)),
            @AttributeOverride(name = "ru", column = @Column(name = "desc_ru", length = 600))
    })
    private Text description;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "az", column = @Column(name = "tag_az")),
            @AttributeOverride(name = "en", column = @Column(name = "tag_en")),
            @AttributeOverride(name = "ru", column = @Column(name = "tag_ru"))
    })
    private Text tag;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    /** Çaylar üçün tövsiyə olunan dəmləmə müddəti (dəqiqə); digər məhsullarda null. */
    private Integer brewMinutes;

    @Column(nullable = false)
    private int sortOrder;

    protected MenuItem() {
    }

    public MenuItem(Category category, Text name, Text description, Text tag, BigDecimal price, Integer brewMinutes, int sortOrder) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.tag = tag;
        this.price = price;
        this.brewMinutes = brewMinutes;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public Category getCategory() { return category; }
    public String getName(String lang) { return name.get(lang); }
    public String getDescription(String lang) { return description.get(lang); }
    public String getTag(String lang) { return tag == null ? null : tag.get(lang); }
    public BigDecimal getPrice() { return price; }
    public Integer getBrewMinutes() { return brewMinutes; }
    public int getSortOrder() { return sortOrder; }
}
