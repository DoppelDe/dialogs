package de.ghci.dialog.io.soccer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.ghci.dialog.io.Resources;
import de.ghci.dialog.model.soccer.Competition;
import de.ghci.dialog.model.soccer.Fixture;
import de.ghci.dialog.model.soccer.Team;
import de.ghci.dialog.process.soccer.TeamsHelper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Dominik
 */
@Singleton
public class SoccerDataLoader {

    public static final int DATA_START_YEAR = 2006;
    public static final int CURRENT_YEAR = 2017;
    private static final String[] BL_STORAGE_FOLDER_PATH = new String[]{
            Resources.getResourcesPath() + "fixtures\\BL1\\",
            Resources.getResourcesPath() + "fixtures\\BL2\\"
    };
    public static final String[] BL_KEY = new String[]{"BL1", "BL2"};

    private TeamsHelper teamsHelper;

    private Collection<Team> teams;

    @Inject
    public SoccerDataLoader(TeamsHelper teamsHelper) throws IOException {
        this.teamsHelper = teamsHelper;
        teams = getAllTeams();
    }

    public Competition getCurrentBundesligaData() throws IOException {
        return getBundesligaData(CURRENT_YEAR, 0);
    }

    public List<Competition> getAllBundesligaData() throws IOException {
        List<Competition> data = new ArrayList<>();
        for (int year = DATA_START_YEAR; year <= CURRENT_YEAR; year++) {
            data.add(getBundesligaData(year, 0));
        }
        data.add(getBundesligaData(2017, 1));
        return data;
    }

    public Competition getBundesligaData(int seasonYear, int league) throws IOException {
        List<Competition> competitions = getCompetitionsOfYear(seasonYear);

        for (Competition competition : competitions) {
            if (competition.getLeague().equals(BL_KEY[league]) && competition.getYear() == seasonYear) {
                try {
                    setTeams(competition);
                } catch (IOException ignored) {
                    competition.setTeams(new ArrayList<>());
                    // ignore
                }
                setBundesligaFixtures(competition, seasonYear, BL_STORAGE_FOLDER_PATH[league]);
                return competition;
            }
        }
        throw new RuntimeException("Bundesliga not found!");
    }

    private List<Competition> getCompetitionsOfYear(int year) throws IOException {
        URL myURL = new URL("http://api.football-data.org/v1/competitions/?season=" + year);
        HttpURLConnection myURLConnection = getConnection(myURL);
        ObjectMapper mapper = new ObjectMapper(); // just need one
        return mapper.readValue(myURLConnection.getInputStream(), new TypeReference<List<Competition>>() {
        });
    }

    private void setTeams(Competition competition) throws IOException {
        URL myURL = new URL("http://api.football-data.org/v1/competitions/" + competition.getId() + "/teams");
        HttpURLConnection myURLConnection = getConnection(myURL);
        ObjectMapper mapper = new ObjectMapper();
        TeamList teamList = mapper.readValue(myURLConnection.getInputStream(), TeamList.class);
        List<Team> competitionTeams = new ArrayList<>();
        for (Team team : teamList.getTeams()) {
            Team t = teamsHelper.getTeam(teams, team.getName());
            if (t != null) {
                competitionTeams.add(t);
            }
        }
        competition.setTeams(competitionTeams);
    }

    private void setBundesligaFixtures(Competition competition, int year, String fixturesPath) throws IOException {
        List<Fixture> fixtures = new ArrayList<>();
        InputStreamReader inStream = getInputStream(year, fixturesPath);
        BufferedReader buff = new BufferedReader(inStream);
        buff.readLine(); // ignore header
        String content = buff.readLine();
        while (content != null) {
            fixtures.add(parseFixture(content, competition));
            content = buff.readLine();
        }
        competition.setFixtures(fixtures);
    }

    private InputStreamReader getInputStream(int year, String fixturesPath) throws IOException {
        return new FileReader(fixturesPath + getYearCode(year) + ".csv");
//        URL url12 = new URL("http://www.football-data.co.uk/mmz4281/" + getYearCode(year) + "/D1.csv");
//        URLConnection urlConn = url12.openConnection();
//        return new InputStreamReader(urlConn.getInputStream());
    }

    private String getYearCode(int year) {
        return String.format("%02d", year % 100) + String.format("%02d", (year + 1) % 100);
    }

    private Fixture parseFixture(String content, Competition competition) throws IOException {
        Fixture fixture = new Fixture();
        String[] tokens = content.split(",");
        fixture.setMatchDate(parseDate(tokens[1]));
        fixture.setHomeTeam(getTeam(competition, tokens[2]));
        fixture.setAwayTeam(getTeam(competition, tokens[3]));
        fixture.setFullTimeHomeGoals(Integer.parseInt(tokens[4]));
        fixture.setFullTimeAwayGoals(Integer.parseInt(tokens[5]));
        fixture.setHalfTimeHomeGoals(Integer.parseInt(tokens[7]));
        fixture.setHalfTimeAwayGoals(Integer.parseInt(tokens[8]));
        fixture.setHomeShots(Integer.parseInt(tokens[10]));
        fixture.setAwayShots(Integer.parseInt(tokens[11]));
        fixture.setHomeShotsOnTarget(Integer.parseInt(tokens[12]));
        fixture.setAwayShotsOnTarget(Integer.parseInt(tokens[13]));
        fixture.setHomeFouls(Integer.parseInt(tokens[14]));
        fixture.setAwayFouls(Integer.parseInt(tokens[15]));
        fixture.setHomeCorners(Integer.parseInt(tokens[16]));
        fixture.setAwayCorners(Integer.parseInt(tokens[17]));
        fixture.setHomeYellows(Integer.parseInt(tokens[18]));
        fixture.setAwayYellows(Integer.parseInt(tokens[19]));
        fixture.setHomeReds(Integer.parseInt(tokens[20]));
        fixture.setAwayReds(Integer.parseInt(tokens[21]));
        return fixture;
    }

    private Team getTeam(Competition competition, String teamName) throws IOException {
        try {
            return teamsHelper.getTeamByApiName(competition.getTeams(), teamName);
        } catch (IllegalArgumentException e) {
            return teamsHelper.getTeamByApiName(getAllTeams(), teamName);
        }
    }

    private LocalDate parseDate(String token) {
        return LocalDate.parse(token, DateTimeFormatter.ofPattern("dd/MM/yy"));
    }

    private HttpURLConnection getConnection(URL myURL) throws IOException {
        HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();
        myURLConnection.setRequestMethod("GET");
        myURLConnection.setRequestProperty("Content-Type", "application/json");
        myURLConnection.setRequestProperty("X-Auth-Token", "db7107c4a3e94426b14758a633a3d14b");
        return myURLConnection;
    }

    public Collection<Team> getAllTeams() throws IOException {
        if (teams == null) {
            teams = TeamLoader.loadTeams();
        }
        return teams;
    }

}
