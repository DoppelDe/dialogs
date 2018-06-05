package de.ghci.dialog.process.soccer;

import de.ghci.dialog.io.soccer.SoccerDataLoader;
import de.ghci.dialog.model.Entity;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.soccer.*;
import de.ghci.dialog.model.statement.OpinionAspect;
import de.ghci.dialog.model.statement.OpinionContext;
import de.ghci.dialog.process.opinion.DomainOpinionGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Dominik
 */

public class SoccerStatementGenerator implements DomainOpinionGenerator {

    @Override
    public OpinionAspect getRandomAspect(Entity entity) {
        if(entity instanceof Team) {
            return getRandomElement(TeamAspect.values(), 1);
        } else if (entity instanceof Person){
            return PersonAspect.PERFORMANCE;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public OpinionContext getRandomContext(Entity entity) {
        return getRandomElement(SoccerContext.values(), 1);
    }

    @Override
    public Entity getRandomEntity(Entity entity, Information information) {
        if(entity instanceof Team) {
            return getRandomTeam((Team) entity, ((SoccerInformation) information).getCompetitions());
        } else if(entity instanceof Person) {
            return getRandomPlayer((Person) entity, ((SoccerInformation) information).getCurrentMatch());
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Team getRandomTeam(Team team, List<Competition> competitions) {
        Random random = new Random();
        for (Competition competition : competitions) {
            if (SoccerDataLoader.CURRENT_YEAR == competition.getYear() && competition.getTeams().contains(team)) {
                return competition.getTeams().get(random.nextInt(competition.getTeams().size()));
            }
        }
        for (Competition competition : competitions) {
            if (competition.getTeams().contains(team)) {
                return competition.getTeams().get(random.nextInt(competition.getTeams().size()));
            }
        }
        Competition competition = competitions.get(random.nextInt(competitions.size()));
        return competition.getTeams().get(random.nextInt(competition.getTeams().size()));
    }


    private Player getRandomPlayer(Person player, MatchInformation matchInformation) {
        ArrayList<Player> players = new ArrayList<>();
        players.addAll(matchInformation.getPlayers());
        return players.get(new Random().nextInt(players.size()));
    }

    private <T> T getRandomElement(T[] array, int ignoreLastElements) {
        return array[new Random().nextInt(array.length - ignoreLastElements)];
    }
}
