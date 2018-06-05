package de.ghci.dialog.io.soccer;

import de.ghci.dialog.model.soccer.Team;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dominik
 */
public class TeamLoader {

    private static final String FILE_PATH = "C:\\Users\\Dominik\\Documents\\studium_master\\masterarbeit\\workspace\\dialogs\\src\\main\\resources\\teams.store";
    private static List<Team> teams;

    public static void storeTeams(List<Team> teams) throws IOException {
        FileOutputStream fout = new FileOutputStream(FILE_PATH);
        ObjectOutputStream out = new ObjectOutputStream(fout);
        out.writeObject(teams);
    }

    public static List<Team> loadTeams() throws IOException {
        if(teams != null && !teams.isEmpty()) {
            return teams;
        }
        try(FileInputStream fin = new FileInputStream(FILE_PATH)) {
            ObjectInputStream ois = new ObjectInputStream(fin);
            teams = (List<Team>) ois.readObject();
            return teams;
        } catch (FileNotFoundException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }
}
