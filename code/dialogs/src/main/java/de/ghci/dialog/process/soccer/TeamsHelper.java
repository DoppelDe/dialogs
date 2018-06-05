package de.ghci.dialog.process.soccer;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.ghci.dialog.model.soccer.Team;
import de.ghci.dialog.process.ContainsMatcher;

import java.util.*;

/**
 * @author Dominik
 */
@Singleton
public class TeamsHelper {

    private static final Map<String, Integer> teamMap;

    static {
        teamMap = new HashMap<>();
        teamMap.put("FC Koln", 1);
        teamMap.put("Hoffenheim", 2);
        teamMap.put("Leverkusen", 3);
        teamMap.put("Dortmund", 4);
        teamMap.put("Bayern Munich", 5);
        teamMap.put("Schalke 04", 6);
        teamMap.put("Hamburg", 7);
        teamMap.put("Hannover", 8);
        teamMap.put("Hertha", 9);
        teamMap.put("Stuttgart", 10);
        teamMap.put("Wolfsburg", 11);
        teamMap.put("Werder Bremen", 12);
        teamMap.put("Kaiserslautern", 13);
        teamMap.put("Nurnberg", 14);
        teamMap.put("Mainz", 15);
        teamMap.put("Augsburg", 16);
        teamMap.put("Freiburg", 17);
        teamMap.put("M'gladbach", 18);
        teamMap.put("Ein Frankfurt", 19);
        teamMap.put("St Pauli", 20);
        teamMap.put("Greuther Furth", 21);
        teamMap.put("Erzgebirge Aue", 22);
        teamMap.put("Cottbus", 23);
        teamMap.put("Fortuna Dusseldorf", 24);
        teamMap.put("Duisburg", 25);
        teamMap.put("Aachen", 27);
        teamMap.put("Union Berlin", 28);
        teamMap.put("Paderborn", 29);
        teamMap.put("Ingolstadt", 31);
        teamMap.put("Karlsruhe", 32);
        teamMap.put("Braunschweig", 33);
        teamMap.put("Hansa Rostock", 34);
        teamMap.put("Dresden", 35);
        teamMap.put("Bochum", 36);
        teamMap.put("Bielefeld", 38);
        teamMap.put("Regensburg", 43);
        teamMap.put("Heidenheim", 44);
        teamMap.put("Sandhausen", 46);
        teamMap.put("Darmstadt", 55);
        teamMap.put("Holstein Kiel", 720);
        teamMap.put("RB Leipzig", 721);
    }

    private ContainsMatcher containsMatcher;

    @Inject
    public TeamsHelper(ContainsMatcher containsMatcher) {
        this.containsMatcher = containsMatcher;
    }

    public Team getTeamByApiName(Collection<Team> teams, String name) {
        Integer id = teamMap.get(name);
        if (id != null) {
            for (Team team : teams) {
                if (id == team.getId()) {
                    return team;
                }
            }
        }
        throw new IllegalArgumentException("Could not find team: " + name);
    }

    public Team getTeam(Collection<Team> teams, String target) {
        for (Team team : teams) {
            if ((!Strings.isNullOrEmpty(team.getName())
                    && (containsMatcher.matches(team.getName(), target)
                    || containsMatcher.matches(target, team.getName())))
                    || (!Strings.isNullOrEmpty(team.getShortName())
                    && (containsMatcher.matches(team.getShortName(), target)
                    || containsMatcher.matches(target, team.getShortName())))) {
                return team;
            }
        }
        for (Team team : teams) {
            if (!Strings.isNullOrEmpty(team.getCode()) &&
                    (target.equals(team.getCode())
                            || target.contains(" " + team.getCode().toUpperCase() + " "))) {
                return team;
            }
        }
        return null;
    }
}
