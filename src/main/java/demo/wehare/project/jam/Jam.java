package demo.wehare.project.jam;

import jakarta.persistence.*;

@Entity
@Table(name = "jams")
public class Jam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 7)
    private String color;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, length = 500)
    private String pairing;

    @Column(nullable = false)
    private int sortOrder;

    protected Jam() {
    }

    public Jam(String name, String color, String description, String pairing, int sortOrder) {
        this.name = name;
        this.color = color;
        this.description = description;
        this.pairing = pairing;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public String getDescription() { return description; }
    public String getPairing() { return pairing; }
    public int getSortOrder() { return sortOrder; }
}
