package top.mores.pactHunt.match;

import java.util.UUID;

public class PlayerSession {
    private final UUID uuid;
    private boolean extracted = false;
    private int kills = 0;
    private double damageDealt = 0;
    private double damageTaken = 0;

    public PlayerSession(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isExtracted() {
        return extracted;
    }

    public int getKills() {
        return kills;
    }

    public double getDamageDealt() {
        return damageDealt;
    }

    public double getDamageTaken() {
        return damageTaken;
    }
}
