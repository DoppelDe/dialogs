package de.ghci.dialog.io;

import de.ghci.dialog.model.switchboard.SwUtterance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author Dominik
 */
public class SwitchboardDataParser {

    private static final String DATA_FILE = "C:\\Users\\Dominik\\Documents\\studium_master\\masterarbeit\\workspace\\" +
            "dialogs\\src\\main\\resources\\switchboard_data.csv";
    public static final String CSV_SPLITTER_REGEX = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";


    public static List<SwUtterance> getUtterances() throws IOException {
        List<SwUtterance> utterances = new ArrayList<>();

        FileReader in = new FileReader(DATA_FILE);
        BufferedReader br = new BufferedReader(in);
        String line;
        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(CSV_SPLITTER_REGEX, -1);
            String dialogAct = tokens[4];
            String text = sanitise(tokens[8]);
            utterances.add(new SwUtterance(dialogAct, text));
        }
        in.close();

        System.out.println("Switchboard data loaded");
        return utterances;
    }

    private static String sanitise(String text) {
        String result = "";
        int brackets = 0;
        int brackets2 = 0;
        boolean open = false;
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if(c != '"') {
                if(c == '<' || c == '{') {
                    brackets++;
                } else if (c == '>' || c == '}') {
                    brackets--;
                } else if(brackets == 0 && c != '/' && c != '-') {
                    if(c == '[') {
                        brackets2++;
                        open = true;
                    } else if(c == ']') {
                        brackets2--;
                        open = true;
                    } else if(c == '+') {
                        open = false;
                    } else if(brackets2 == 0 || open) {
                        result += c;
                    }
                }
            }
        }
        return result.trim().replaceAll("\\s+", " ");
    }
}
