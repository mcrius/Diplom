/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest.runnables;

import com.sun.javafx.collections.ObservableListWrapper;
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
import mailtest.MainScene.MainSceneController;
import mailtest.jpa.entities.MailMessage;

/**
 *
 * @author bzzzt
 */
public class OpenFolderRun implements Runnable{

    private Folder folder;
    private TableView table;
    private ProgressIndicator pi;
    private Label label;

    public OpenFolderRun(Folder folder, TableView table, ProgressIndicator pi, Label label) {
        this.folder = folder;
        this.table = table;
        this.pi = pi;
        this.label = label;
    }
    
    
    @Override
    public void run() {
        final List<MailMessage> list = new ArrayList<>();
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                pi.setVisible(true);
                label.setText("Opening folder " + folder.getName());
            }
        });
        try {
            folder.open(Folder.READ_WRITE);
            Message[] all = null;
            if ((folder.getMessageCount()-100) < 0) {
                all = folder.getMessages();
            }else{
                all = folder.getMessages(folder.getMessageCount()-100, folder.getMessageCount());
            }
            for (Message all1 : all) {
                list.add(new MailMessage(all1));
            }
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    table.setItems(new ObservableListWrapper(list));
                }
            });
        } catch (MessagingException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                label.setText("Done.");
                pi.setVisible(false);
            }
        });
    }
    
}
