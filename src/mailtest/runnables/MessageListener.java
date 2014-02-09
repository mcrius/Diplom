/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest.runnables;

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
import mailtest.MailTest;
import mailtest.jpa.controllers.MailMessageJpaController;
import mailtest.jpa.entities.MailMessage;

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
        try {
            final List<MailMessage> newM = new ArrayList<>();
            int lastCount = 0;
            MailMessageJpaController mmc = new MailMessageJpaController(MailTest.getEmf());
            lastCount = folder.getMessageCount();
            while(true){
                try {
                    int newMessageCount = folder.getMessageCount();
                    if (newMessageCount > lastCount) {
                        try {
                            Platform.runLater(new Runnable() {
                                
                                @Override
                                public void run() {
                                    label.setText("Updating Inbox.");
                                    label.setVisible(true);
                                    pi.setVisible(true);
                                }
                            });
                            Message[] messages = folder.getMessages(folder.getMessageCount() - (newMessageCount - lastCount), folder.getMessageCount());
                            for (int i = 0; i < messages.length; i++) {
                                MailMessage mm = new MailMessage(messages[i]);
                                mmc.create(mm);
                                newM.add(mm);
                            }
                            Platform.runLater(new Runnable() {
                                
                                @Override
                                public void run() {
                                    table.getItems().addAll(newM);
                                }
                            });
                            Platform.runLater(new Runnable(){
                                
                                @Override
                                public void run() {
                                    label.setText("Done.");
                                    label.setVisible(false);
                                    pi.setVisible(false);
                                }
                                
                            });
                            lastCount = newMessageCount;
                        } catch (MessagingException ex) {
                            Logger.getLogger(MessageListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MessageListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (MessagingException ex) {
                    Logger.getLogger(MessageListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (MessagingException ex) {
            Logger.getLogger(MessageListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
