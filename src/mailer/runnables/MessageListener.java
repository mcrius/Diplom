/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailer.runnables;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import mailer.dto.MessageDTO;

/**
 *
 * @author bzzzt
 */
public class MessageListener implements Runnable{

    private Folder folder;
    private TableView table;
    private ProgressIndicator pi;
    private Label label;

    public MessageListener(Folder folder, TableView table, ProgressIndicator pi, Label label) {
        this.folder = folder;
        this.table = table;
        this.pi = pi;
        this.label = label;
    }
    
    
    
    
    @Override
    public void run() {
        final List<MessageDTO> newM = new ArrayList<>();
        List<MessageDTO> old = new ArrayList<>();
        int lastCount = 0;
        try {
            lastCount = folder.getMessageCount();
            while(true){
                int newMessageCount = folder.getMessageCount();
                if (newMessageCount > lastCount) {
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            label.setText("Updating Inbox.");
                            label.setVisible(true);
                            pi.setVisible(true);
                        }
                    });
                    Message[] messages = folder.getMessages(folder.getMessageCount() - (newMessageCount - lastCount), folder.getMessageCount());
                    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File("cache.blurp")))) {
                        old = (List<MessageDTO>) in.readObject();
                    }
                    for (int i = 0; i < messages.length; i++) {
                        newM.add(new MessageDTO(messages[i]));
                    }
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            table.getItems().addAll(newM);
                        }
                    });
                    old.addAll(newM);
                    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File("cache.blurp")))) {
                        out.writeObject(old);
                        out.flush();
                    }
                    Platform.runLater(new Runnable(){

                        @Override
                        public void run() {
                            label.setText("Done.");
                            label.setVisible(false);
                            pi.setVisible(false);
                        }
                        
                    });
                    lastCount = newMessageCount;
                }
                Thread.sleep(10 * 1000);
            }
        } catch (MessagingException | ClassNotFoundException | InterruptedException | IOException ex) {
            Logger.getLogger(MessageListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
