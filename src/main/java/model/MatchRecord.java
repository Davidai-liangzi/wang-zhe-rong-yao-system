package model;

import java.io.Serializable;
import java.time.LocalDate;

public class MatchRecord implements Serializable, Identifiable {
    private static final long serialVersionUID = 1L;
    private String id;
    private Team teamA;
    private Team teamB;
    private int scoreA;
    private int scoreB;
    private LocalDate matchDate;

    public MatchRecord() {}

    public MatchRecord(String id, Team teamA, Team teamB, int scoreA, int scoreB, LocalDate matchDate) {
        this.id = id;
        this.teamA = teamA;
        this.teamB = teamB;
        this.scoreA = scoreA;
        this.scoreB = scoreB;
        this.matchDate = matchDate;
    }

    @Override
    public String getId() { return id; }
    @Override
    public String getName() {
        if (teamA == null || teamB == null) return "Unknown Match";
        return teamA.getTeamName() + " vs " + teamB.getTeamName();
    }
    public void setId(String id) { this.id = id; }

    public Team getTeamA() { return teamA; }
    public void setTeamA(Team teamA) { this.teamA = teamA; }
    public Team getTeamB() { return teamB; }
    public void setTeamB(Team teamB) { this.teamB = teamB; }
    public int getScoreA() { return scoreA; }
    public void setScoreA(int scoreA) { this.scoreA = scoreA; }
    public int getScoreB() { return scoreB; }
    public void setScoreB(int scoreB) { this.scoreB = scoreB; }
    public LocalDate getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDate matchDate) { this.matchDate = matchDate; }
}
