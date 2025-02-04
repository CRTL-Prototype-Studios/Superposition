package cn.crtlprototypestudios.spos.data.waypoint;

public class Waypoint {
    private String world;
    private String name;
    private double x;
    private double y;
    private double z;

    public Waypoint(String world, String name, double x, double y, double z) {
        this.world = world;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Getters and setters
    public String getWorld() { return world; }
    public String getName() { return name; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
}
