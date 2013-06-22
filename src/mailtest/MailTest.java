/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.swing.JOptionPane;
import mailtest.dto.MessageDTO;
import mailtest.MainScene.MainSceneController;

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
    private static Folder inbox;
    
    private static Store store;
    private static Folder[] folders;

    public static Folder[] getFolders() {
        return folders;
    }

    public static Store getStore() {
        return store;
    }
    
    
    
    private Folder[] findFolders() {
        try {
            Folder[] list = store.getDefaultFolder().list();
            for (int i = 0; i < list.length; i++) {
                list[i].list();
            }
            return list;
        } catch (MessagingException ex) {
            Logger.getLogger(MailTest.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    

    public static Folder getInbox() {
        return inbox;
    }
    private List<MessageDTO> old;

    @Override
    public void init() {
        boolean created = false;
        ObjectInputStream objectInputStream = null;
        try {
            File messageFile = new File("test.test");
            if (!messageFile.exists()) {
                try {
                    created = messageFile.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(MailTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (!created) {
                objectInputStream = new ObjectInputStream(new FileInputStream(messageFile));
            }
            if (messageFile.length() > 20) {
                old = (List<MessageDTO>) objectInputStream.readObject();
                MainSceneController.setMessages(old);
                System.out.println("Set messages");
            }
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
            store = s.getStore("imaps");
            try {
                store.connect("imap.gmail.com", "rius.ns@gmail.com", MailTest.PASS);
                folders = findFolders();
                inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_WRITE);
                System.out.println(inbox.hasNewMessages());
                if (inbox.hasNewMessages()) {
                    messages = inbox.getMessages(inbox.getMessageCount() - inbox.getUnreadMessageCount(), inbox.getMessageCount());
//                messages = inbox.getMessages(inbox.getMessageCount() - 25, inbox.getMessageCount());
                    FetchProfile fp = new FetchProfile();
                    fp.add(FetchProfile.Item.ENVELOPE);
                    inbox.fetch(messages, fp);
                }
                if (old.get(old.size() -1).getId() < inbox.getMessages()[inbox.getMessageCount() -1].getMessageNumber()) {
                    int lastSavedId = old.get(old.size() -1 ).getId();
                    int newestId = inbox.getMessages()[inbox.getMessageCount() -1 ].getMessageNumber();
                    messages = inbox.getMessages(inbox.getMessageCount() - (newestId - lastSavedId), inbox.getMessageCount());
                    FetchProfile fp = new FetchProfile();
                    fp.add(FetchProfile.Item.ENVELOPE);
                    inbox.fetch(messages, fp);
                }
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(messageFile));
                if (MainSceneController.getMessages() != null && messages != null) {
                    for (int i = 0; i < messages.length; i++) {
                        MainSceneController.getMessages().add(new MessageDTO(messages[i]));
                    }
                }
                out.writeObject(MainSceneController.getMessages());
                out.flush();
                System.out.println("Messages written");
            } catch (MessagingException e) {
                JOptionPane.showMessageDialog(null, "You currently have no internet connection.\nYou will be able to read only locally saved messages.");
            }
        } catch (IOException | ClassNotFoundException | MessagingException ex) {
            Logger.getLogger(MailTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MailTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainScene/MainScene.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("MailChecker");
//        stage.getIcons().add(new Image("icon.ico"));
        stage.show();

    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}