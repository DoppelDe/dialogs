package de.ghci.dialog;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.ghci.dialog.gui.MainFrame;
import de.ghci.dialog.model.Information;
import de.ghci.dialog.model.soccer.SoccerInformation;
import de.ghci.dialog.process.DialogProcess;
import de.ghci.dialog.process.QuestionProcess;
import de.ghci.dialog.process.opinion.*;
import de.ghci.dialog.process.SmallTalkProcess;
import de.ghci.dialog.process.soccer.*;
import org.lambda3.graphene.core.Graphene;

import java.io.*;
import java.util.Locale;

/**
 * @author Dominik
 */
public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
//        Injector injector = Guice.createInjector(new ProductModule());
        Injector injector = Guice.createInjector(new SoccerModule());
        Locale.setDefault(Locale.ENGLISH);

        DialogProcess process = injector.getInstance(DialogProcess.class);
        new Graphene();
        SmallTalkProcess.load();

        System.out.println("Starting dialog...");
        MainFrame mainFrame = new MainFrame(process);
        mainFrame.setVisible(true);
        process.addObserver(mainFrame);
        process.startDialog();
    }
}