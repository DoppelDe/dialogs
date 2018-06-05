package de.ghci.dialog.model.soccer;

import java.time.LocalDate;

/**
 * @author Dominik
 */
public class Fixture {

    private LocalDate matchDate;
    private Team homeTeam;
    private Team awayTeam;
    private int fullTimeHomeGoals;
    private int fullTimeAwayGoals;
    private int halfTimeHomeGoals;
    private int halfTimeAwayGoals;
    private int homeShots;
    private int awayShots;
    private int homeShotsOnTarget;
    private int awayShotsOnTarget;
    private int homeFouls;
    private int awayFouls;
    private int homeCorners;
    private int awayCorners;
    private int homeYellows;
    private int awayYellows;
    private int homeReds;
    private int awayReds;

    public LocalDate getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDate matchDate) {
        this.matchDate = matchDate;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public int getFullTimeHomeGoals() {
        return fullTimeHomeGoals;
    }

    public void setFullTimeHomeGoals(int fullTimeHomeGoals) {
        this.fullTimeHomeGoals = fullTimeHomeGoals;
    }

    public int getFullTimeAwayGoals() {
        return fullTimeAwayGoals;
    }

    public void setFullTimeAwayGoals(int fullTimeAwayGoals) {
        this.fullTimeAwayGoals = fullTimeAwayGoals;
    }

    public int getHalfTimeHomeGoals() {
        return halfTimeHomeGoals;
    }

    public void setHalfTimeHomeGoals(int halfTimeHomeGoals) {
        this.halfTimeHomeGoals = halfTimeHomeGoals;
    }

    public int getHalfTimeAwayGoals() {
        return halfTimeAwayGoals;
    }

    public void setHalfTimeAwayGoals(int halfTimeAwayGoals) {
        this.halfTimeAwayGoals = halfTimeAwayGoals;
    }

    public int getHomeShots() {
        return homeShots;
    }

    public void setHomeShots(int homeShots) {
        this.homeShots = homeShots;
    }

    public int getAwayShots() {
        return awayShots;
    }

    public void setAwayShots(int awayShots) {
        this.awayShots = awayShots;
    }

    public int getHomeShotsOnTarget() {
        return homeShotsOnTarget;
    }

    public void setHomeShotsOnTarget(int homeShotsOnTarget) {
        this.homeShotsOnTarget = homeShotsOnTarget;
    }

    public int getAwayShotsOnTarget() {
        return awayShotsOnTarget;
    }

    public void setAwayShotsOnTarget(int awayShotsOnTarget) {
        this.awayShotsOnTarget = awayShotsOnTarget;
    }

    public int getHomeFouls() {
        return homeFouls;
    }

    public void setHomeFouls(int homeFouls) {
        this.homeFouls = homeFouls;
    }

    public int getAwayFouls() {
        return awayFouls;
    }

    public void setAwayFouls(int awayFouls) {
        this.awayFouls = awayFouls;
    }

    public int getHomeCorners() {
        return homeCorners;
    }

    public void setHomeCorners(int homeCorners) {
        this.homeCorners = homeCorners;
    }

    public int getAwayCorners() {
        return awayCorners;
    }

    public void setAwayCorners(int awayCorners) {
        this.awayCorners = awayCorners;
    }

    public int getHomeYellows() {
        return homeYellows;
    }

    public void setHomeYellows(int homeYellows) {
        this.homeYellows = homeYellows;
    }

    public int getAwayYellows() {
        return awayYellows;
    }

    public void setAwayYellows(int awayYellows) {
        this.awayYellows = awayYellows;
    }

    public int getHomeReds() {
        return homeReds;
    }

    public void setHomeReds(int homeReds) {
        this.homeReds = homeReds;
    }

    public int getAwayReds() {
        return awayReds;
    }

    public void setAwayReds(int awayReds) {
        this.awayReds = awayReds;
    }

    @Override
    public String toString() {
        return "Fixture{" +
                "matchDate=" + matchDate +
                ", homeTeam=" + homeTeam +
                ", awayTeam=" + awayTeam +
                ", fullTimeHomeGoals=" + fullTimeHomeGoals +
                ", fullTimeAwayGoals=" + fullTimeAwayGoals +
                '}';
    }

    public int getHomeWins() {
        if(fullTimeHomeGoals > fullTimeAwayGoals) {
            return 1;
        } else {
            return 0;
        }
    }

    public int getAwayWins() {
        if(fullTimeHomeGoals < fullTimeAwayGoals) {
            return 1;
        } else {
            return 0;
        }
    }

    public int getHomeFairness() {
        return getFairnessScore(homeFouls, homeYellows, homeReds);
    }

    public int getAwayFairness() {
        return getFairnessScore(awayFouls, awayYellows, awayReds);
    }

    private int getFairnessScore(int fouls, int yellows, int reds) {
        return fouls + yellows * 3 + reds * 8;
    }

    public int getHomeChances() {
        return getChancesScore(homeShots, homeShotsOnTarget, homeCorners);
    }

    public int getAwayChances() {
        return getChancesScore(awayShots, awayShotsOnTarget, awayCorners);
    }

    private int getChancesScore(int shots, int shotsOnTarget, int corners) {
        return shots + shotsOnTarget + corners;
    }

    public int getHomePerformance() {
        return getPerformanceScore(getHomeWins(), getFullTimeHomeGoals(), getFullTimeAwayGoals(), getHomeChances(),
                getHomeFairness());
    }

    public int getAwayPerformance() {
        return getPerformanceScore(getAwayWins(), getFullTimeAwayGoals(), getFullTimeHomeGoals(), getAwayChances(),
                getAwayFairness());
    }

    private int getPerformanceScore(int win, int goals, int goalsAgainst, int chances, int fairness) {
        int score = win * 3 + goals * 3 - goalsAgainst * 4 + chances - (fairness / 2);
        return score > 0 ? score : 0;
    }

}
