package roboleader;

public class RobotInfo implements Comparable<RobotInfo> {
    public String name;
    public double x;
    public double y;
    public double distanciaAlLider;

    public RobotInfo(String name, double x, double y, double distanciaAlLider) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.distanciaAlLider = distanciaAlLider;
    }

    @Override
    public int compareTo(RobotInfo other) {
        return Double.compare(this.distanciaAlLider, other.distanciaAlLider); // Ordena según la distancia al líder
    }
}
