package de.ghci.dialog.io.soccer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.ghci.dialog.model.soccer.Team;

import java.util.List;

/**
 * @author Dominik
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamList {
    private int count;
    private List<Team> teams;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }
}
