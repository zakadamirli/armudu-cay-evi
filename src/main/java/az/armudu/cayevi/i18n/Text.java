package az.armudu.cayevi.i18n;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Üç dildə mətn (az/en/ru). Tərcümə yoxdursa Azərbaycan dilinə düşür. */
@Embeddable
public class Text {

    @Column(length = 600)
    private String az;
    @Column(length = 600)
    private String en;
    @Column(length = 600)
    private String ru;

    protected Text() {
    }

    public Text(String az, String en, String ru) {
        this.az = az;
        this.en = en;
        this.ru = ru;
    }

    /** lang: "az" | "en" | "ru" (başqası → az). Boş tərcümə az-a düşür. */
    public String get(String lang) {
        String v = switch (lang == null ? "az" : lang) {
            case "en" -> en;
            case "ru" -> ru;
            default -> az;
        };
        return v == null || v.isBlank() ? az : v;
    }
}
