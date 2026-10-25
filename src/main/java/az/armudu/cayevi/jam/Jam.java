package az.armudu.cayevi.jam;

import az.armudu.cayevi.i18n.Text;
import jakarta.persistence.*;

@Entity
@Table(name = "jams")
public class Jam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "az", column = @Column(name = "name_az", nullable = false)),
            @AttributeOverride(name = "en", column = @Column(name = "name_en")),
            @AttributeOverride(name = "ru", column = @Column(name = "name_ru"))
    })
    private Text name;

    @Column(nullable = false, length = 7)
    private String color;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "az", column = @Column(name = "desc_az", nullable = false)),
            @AttributeOverride(name = "en", column = @Column(name = "desc_en")),
            @AttributeOverride(name = "ru", column = @Column(name = "desc_ru"))
    })
    private Text description;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "az", column = @Column(name = "pairing_az", nullable = false, length = 600)),
            @AttributeOverride(name = "en", column = @Column(name = "pairing_en", length = 600)),
            @AttributeOverride(name = "ru", column = @Column(name = "pairing_ru", length = 600))
    })
    private Text pairing;

    @Column(nullable = false)
    private int sortOrder;

    protected Jam() {
    }

    public Jam(Text name, String color, Text description, Text pairing, int sortOrder) {
        this.name = name;
        this.color = color;
        this.description = description;
        this.pairing = pairing;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public String getName(String lang) { return name.get(lang); }
    public String getColor() { return color; }
    public String getDescription(String lang) { return description.get(lang); }
    public String getPairing(String lang) { return pairing.get(lang); }
    public int getSortOrder() { return sortOrder; }
}
