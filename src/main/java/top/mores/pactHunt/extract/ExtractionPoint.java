package top.mores.pactHunt.extract;

import org.bukkit.Location;

public class ExtractionPoint {
    private final String id;
    private final Location center;
    private final double radius;

    public ExtractionPoint(String id, Location center, double radius) {
        this.id = id;
        this.center = center;
        this.radius = radius;
    }

    public String getId() {
        return id;
    }

    public Location getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public boolean contains(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().getName().equals(center.getWorld().getName())) return false;
        return loc.distanceSquared(center) <= radius * radius;
    }
}
