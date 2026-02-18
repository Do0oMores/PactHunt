package top.mores.pactHunt.match;

import java.util.*;

public class Match {
    private final UUID id=UUID.randomUUID();
    private MatchState state=MatchState.WAITING;

    private final String worldName;
    private final long createAt=System.currentTimeMillis();
    private long startAt=-1;

    private final Set<UUID> players=new HashSet<>();
    private final Set<UUID> alive=new HashSet<>();
    private final Map<UUID,PlayerSession> sessions=new HashMap<>();

    private int countdownTaskId=-1;
    private int timeoutTaskId=-1;

    public Match(String worldName){
        this.worldName=worldName;
    }

    public UUID getId(){
        return id;
    }

    public MatchState getState() {
        return state;
    }

    public void setState(MatchState state){
        this.state=state;
    }

    public String getWorldName() {
        return worldName;
    }

    public long getCreateAt() {
        return createAt;
    }

    public long getStartAt() {
        return startAt;
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public Set<UUID> getAlive() {
        return alive;
    }

    public Map<UUID, PlayerSession> getSessions() {
        return sessions;
    }

    public int getCountdownTaskId() {
        return countdownTaskId;
    }

    public int getTimeoutTaskId() {
        return timeoutTaskId;
    }

    public void setStartAt(long startAt) {
        this.startAt = startAt;
    }

    public void setCountdownTaskId(int countdownTaskId) {
        this.countdownTaskId = countdownTaskId;
    }

    public void setTimeoutTaskId(int timeoutTaskId) {
        this.timeoutTaskId = timeoutTaskId;
    }
}
