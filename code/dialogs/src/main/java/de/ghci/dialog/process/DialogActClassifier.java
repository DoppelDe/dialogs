package de.ghci.dialog.process;

import de.ghci.dialog.model.dialog.DialogAct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Dominik
 */
public class DialogActClassifier {

    private static final String PYTHON_COMMAND = "python C:\\Users\\Dominik\\Documents\\studium_master\\" +
            "masterarbeit\\libraries\\switchboard\\classification\\Code\\classify.py \"%s\"";

    public static DialogAct classify(String utterance) {
        DialogAct returnDa;
        try {
            String daString = getDAString(utterance);
            if(daString.startsWith("s")) {
                returnDa = DialogAct.STATEMENT;
            } else if (daString.startsWith("q"))  {
                returnDa = DialogAct.QUESTION;
            } else {
                returnDa = DialogAct.REST;
            }
        } catch (IOException e) {
            System.err.println("DA failed: " + e.getMessage());
            throw new IllegalStateException(e);
        }
        System.out.println("DA classify: " + utterance);
        System.out.println("DA: " + returnDa);
        return returnDa;
    }


    private static String getDAString(String utterance) throws IOException {
        Process p = Runtime.getRuntime().exec(String.format(PYTHON_COMMAND, utterance));

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        String s = stdInput.readLine();

        if(s == null) {
            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
        }

        return s;
    }

}
