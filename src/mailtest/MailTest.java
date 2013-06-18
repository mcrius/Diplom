/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest;

import java.util.Properties;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import mailtest.dto.MessageDTO;
import mailtest.sendScene.SendController;

/**
 *
 * @author vasil.georgiev
 */
public class MailTest extends Application {
    
    public static final String PASS = "441355423485884424";
    private static Message[] messages;

    public static Message[] getMessages() {
        return messages;
    }
    
    

    @Override
    public void init() throws Exception {
        notifyPreloader(new Preloader.ProgressNotification(-1d));
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
        store.connect("imap.gmail.com", "rius.ns@gmail.com", MailTest.PASS);
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        messages = inbox.getMessages(inbox.getMessageCount()-10, inbox.getMessageCount());
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        inbox.fetch(messages, fp);
        for (int i = 0; i < messages.length; i++) {
            SendController.getMessages().add(new MessageDTO(messages[i]));
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sendScene/send.fxml"));

        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.setTitle("MailChecker");
        stage.show();

    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application. main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}