package az.armudu.cayevi.menu;

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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    private String tag;

    /** Çaylar üçün tövsiyə olunan dəmləmə müddəti (dəqiqə); digər məhsullarda null. */
    private Integer brewMinutes;

    @Column(nullable = false)
    private int sortOrder;

    protected MenuItem() {
    }

    public MenuItem(Category category, String name, String description, BigDecimal price, String tag, Integer brewMinutes, int sortOrder) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.tag = tag;
        this.brewMinutes = brewMinutes;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public Category getCategory() { return category; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getTag() { return tag; }
    public Integer getBrewMinutes() { return brewMinutes; }
    public int getSortOrder() { return sortOrder; }
}
