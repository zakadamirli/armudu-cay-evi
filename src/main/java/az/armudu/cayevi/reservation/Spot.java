package az.armudu.cayevi.reservation;

import az.armudu.cayevi.i18n.Text;

public enum Spot {
    HEYET(new Text("Həyətdə, tut ağacının altında", "In the courtyard, under the mulberry tree", "Во дворе, под тутовым деревом")),
    ICERI(new Text("İçəridə, samovarın yanında", "Indoors, by the samovar", "В зале, у самовара")),
    EYVAN(new Text("Eyvanda, Qız qalasına baxan", "On the balcony, facing the Maiden Tower", "На балконе, с видом на Девичью башню"));

    private final Text label;

    Spot(Text label) {
        this.label = label;
    }

    public String label(String lang) {
        return label.get(lang);
    }
}
