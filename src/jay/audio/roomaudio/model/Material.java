package jay.audio.roomaudio.model;

public enum Material {
    AIR(0, "Air", 1.0, 1.0),
    CONCRETE(1, "Concrete", 0.95, 0.995),
    DRYWALL(2, "Drywall", 0.85, 0.99),
    CARPET(3, "Carpet", 0.7, 0.98),
    FURNITURE(4, "Furniture", 0.8, 0.97);

    private int id;
    private String name;
    private double reflection;
    private double damping;

    Material(int id, String name, double reflection, double damping) {
        this.id = id;
        this.name = name;
        this.reflection = reflection;
        this.damping = damping;
    }

    public int getId() {
        return id;
    }

    public double getReflection() {
        return reflection;
    }

    public double getDamping() {
        return damping;
    }

    public String getDisplayName() {
        return name;
    }

    public static Material fromId(int id) {
        for (Material m : values()) {
            if (m.id == id) return m;
        }
        return AIR;
    }
}
