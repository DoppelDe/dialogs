package de.ghci.dialog.gui;

import de.ghci.dialog.model.dialog.Dialog;
import de.ghci.dialog.model.dialog.Speaker;
import de.ghci.dialog.process.DialogProcess;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Dominik
 */
public class MainFrame extends JFrame implements Observer {

    private static final String TEXT_SUBMIT = "text_submit";
    private static final String LABEL_HUMAN = "You";
    private static final String LABEL_BOT = "Schiri";

    private DialogProcess dialogProcess;
    private JPanel southPanel;
    private JButton sendButton;
    private JTextArea inputField;
    private JTextArea textHistory;

    public MainFrame(DialogProcess dialogProcess) {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.dialogProcess = dialogProcess;
        setUpGui();
    }

    private void setUpGui() {
        setTitle("Schiri");
        southPanel = new JPanel();
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        inputField = new JTextArea(3, 24);
        inputField.setLineWrap(true);
        inputField.setEnabled(false);
        initInputFieldAction();

        textHistory = new JTextArea();
        textHistory.setEditable(false);
        textHistory.setLineWrap(true);

        JScrollPane comp = new JScrollPane(textHistory);
        comp.setBorder(new EmptyBorder(10, 10, 10, 10));

        getContentPane().add(comp, BorderLayout.CENTER);
        getContentPane().add(southPanel, BorderLayout.SOUTH);
        southPanel.add(new JScrollPane(inputField));
//        southPanel.add(inputField);
        southPanel.add(sendButton);



        setSize(600, 400);
        setLocationByPlatform(true);
    }

    private void initInputFieldAction() {
        InputMap input = inputField.getInputMap();
        input.put(KeyStroke.getKeyStroke("ENTER"), TEXT_SUBMIT);
        ActionMap actions = inputField.getActionMap();
        actions.put(TEXT_SUBMIT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String text = inputField.getText();
        textHistory.append("\n"+ LABEL_HUMAN +": " + text);
        scrollDown();
        inputField.setText("");
        disableInput();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    dialogProcess.newHumanUtterance(text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof DialogProcess) {
            Dialog dialog = ((DialogProcess) o).getDialog();
            textHistory.setText(getTextFromDialog(dialog));
            enableInput();

            scrollDown();
        }
    }

    private void disableInput() {
        inputField.setEnabled(false);
        sendButton.setEnabled(false);
    }

    protected void enableInput() {
        inputField.setEnabled(true);
        sendButton.setEnabled(true);
        inputField.requestFocus();
    }

    private void scrollDown() {
        textHistory.setCaretPosition(textHistory.getDocument().getLength());
    }

    private String getTextFromDialog(Dialog dialog) {
        return dialog.getUtterances().stream()
                .map(u -> u.getSpeaker() == Speaker.HUMAN
                        ? LABEL_HUMAN + ": " + u.getVisibleText()
                        : LABEL_BOT + ": " + u.getVisibleText())
                .reduce((s1, s2) -> s1 + "\n" + s2).get();
    }
}
