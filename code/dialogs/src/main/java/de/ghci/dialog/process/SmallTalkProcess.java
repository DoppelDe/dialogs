package de.ghci.dialog.process;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.MagicBooleans;

import java.io.File;

/**
 * @author Dominik
 */
public class SmallTalkProcess {

    private static Chat chatSession;

    public static String generateReply(String input) {
        load();
        String response = chatSession.multisentenceRespond(input);
        if (response.contains("&lt;")) {
            response = response.replace("&lt;", "<");
        }
        if(response.contains("&gt;")) {
            response = response.replace("&gt;", ">");
        }
        return response;
    }

    public static void load() {
        if(chatSession == null) {
            MagicBooleans.trace_mode = false;
            String resourcesPath = getResourcesPath();
            Bot bot = new Bot("schiri", resourcesPath);
//            bot.writeAIMLIFFiles();

            chatSession = new Chat(bot);
        }
    }

    private static String getResourcesPath() {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        path = path.substring(0, path.length() - 2);
        return path + File.separator + "src" + File.separator + "main" + File.separator + "resources";
    }
}
