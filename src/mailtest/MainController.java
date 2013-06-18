/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest;

import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;

/**
 *
 * @author vasil.georgiev
 */
public class MainController implements Initializable {
    
        @FXML
        public Label notification;
        @FXML
        public TextField userField;
        
        @FXML
        public PasswordField passField;
        @FXML
        public ProgressIndicator pi;
        @FXML
        public AnchorPane ap;
    
        @FXML
        public void handleButtonAction(ActionEvent event) throws NoSuchProviderException{
        pi.setOpacity(1d);
        notification.setText("");
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                
        try {
            Properties props = System.getProperties();
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.starttls.enable", "true");
            props.setProperty("mail.smtp.host", "smtp.gmail.com");
            props.setProperty("mail.smtp.port", "587");
            props.setProperty("mail.store.protocol", "imaps");
            props.setProperty("mail.imap.host", "imap.gmail.com");
            props.setProperty("mail.imap.port", "993");
            props.setProperty("mail.imap.timeout", "2000");
            Session s = Session.getDefaultInstance(props, null);
            Store store = s.getStore("imaps");
            store.connect("imap.gmail.com", userField.getText(), passField.getText());
            Folder[] folders = store.getDefaultFolder().list("*");
            for (int i = 0; i < folders.length; i++) {
                System.out.println(">>" + folders[i].getFullName());
            }
            final Folder f = store.getFolder("INBOX");
            final int messagesCount = f.getMessageCount();
            f.addMessageCountListener(new MessageCountListener() {

                @Override
                public void messagesAdded(MessageCountEvent mce) {
                    Message[] messages = mce.getMessages();
                        if (messages.length > 0) {
                            notification.setText("You have " + messages.length + " new messages.");
                        }
                }

                @Override
                public void messagesRemoved(MessageCountEvent mce) {
                    System.out.println(messagesCount -  mce.getMessages().length + " deleted.");
                }
            });
            int newMessageCount = f.getUnreadMessageCount();
            if (newMessageCount == 0 ) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        notification.setText("Sorry no new messages.");
                        pi.setOpacity(0d);
                    }
                });
            }else{
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        notification.setText("You have a message.");
                        pi.setOpacity(0d);
                    }
                });
            }
            
        } catch (MessagingException ex) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    notification.setText("Invalid username or password.");
                    ShakeTransition shakeTransition = new ShakeTransition(ap);
                    shakeTransition.play();
                    pi.setOpacity(0d);
                }
            });
            Logger.getLogger(MainController.class.getName()).log(Level.WARNING, "Messaging Exc", ex);
        }
            }
        });
        t.start();

    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
        public void dismiss(ActionEvent e){
        notification.setText("");
    }
    
    
}
