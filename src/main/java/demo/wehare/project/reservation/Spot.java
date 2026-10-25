package demo.wehare.project.reservation;

public enum Spot {
    HEYET("Həyətdə, tut ağacının altında"),
    ICERI("İçəridə, samovarın yanında"),
    EYVAN("Eyvanda, Qız qalasına baxan");

    private final String label;

    Spot(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
