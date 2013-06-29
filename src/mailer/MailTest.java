/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailer;

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
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.swing.JOptionPane;
import mailer.dto.MessageDTO;
import mailer.MainScene.MainSceneController;
import mailer.dto.SettingsDTO;

/**
 *
 * @author vasil.georgiev
 */
public class MailTest extends Application {

    private static Message[] messages;
    private double step = 0d;
    private static Session session;

    public static Session getSession() {
        return session;
    }
    
    

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
        double progress = 0d;
        Properties props = null;
        Platform.setImplicitExit(true);
        boolean created = false;
        ObjectInputStream objectInputStream = null;
        if (SettingsDTO.checkFile()) {
            SettingsDTO dto = null;
            try {
                dto = SettingsDTO.readDtoFromFile();
                props = dto.getConnectionProperties();
            } catch (    IOException | ClassNotFoundException ex) {
                Logger.getLogger(MailTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        try {
            File messageFile = new File("cache.blurp");
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
            
            session = Session.getDefaultInstance(props, null);
            store = session.getStore(props.getProperty("mail.store.protocol"));
            try {
                store.connect(props.getProperty("mail.store.host"), dto.getUsername(), dto.getPassword());
                folders = findFolders();
                inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_WRITE);
                System.out.println(inbox.hasNewMessages());
                if (inbox.hasNewMessages()) {
                    notifyPreloader(new Preloader.ProgressNotification(0d));
                    step = 1 / inbox.getUnreadMessageCount();
                    System.out.println("Step is : " + step);
                    messages = inbox.getMessages(inbox.getMessageCount() - inbox.getUnreadMessageCount(), inbox.getMessageCount());
                    FetchProfile fp = new FetchProfile();
                    fp.add(FetchProfile.Item.ENVELOPE);
                    inbox.fetch(messages, fp);
                } else{
                    if (old != null && (old.get(old.size() -1).getId() <= inbox.getMessages()[inbox.getMessageCount() -1].getMessageNumber())) {
                        notifyPreloader(new Preloader.ProgressNotification(0d));
                        int lastSavedId = old.get(old.size() -1 ).getId();
                        int newestId = inbox.getMessages()[inbox.getMessageCount() -1 ].getMessageNumber();
                        if ((newestId - lastSavedId) != 0) {
                            step = 1 / (newestId - lastSavedId);
                        }else{
                            step = -2d;
                        }
                        System.out.println("Step is : " + step);
                        messages = inbox.getMessages(inbox.getMessageCount() - (newestId - lastSavedId), inbox.getMessageCount());
                        FetchProfile fp = new FetchProfile();
                        fp.add(FetchProfile.Item.ENVELOPE);
                        inbox.fetch(messages, fp);
                    }else{
                        notifyPreloader(new Preloader.ProgressNotification(0d));
                        notifyPreloader(new Preloader.ErrorNotification("Downloading recent messages.", "", null));
                        step = 0.01d;
                        int count = inbox.getMessageCount() - 100;
                        if (count < 0) {
                            messages = inbox.getMessages();
                            step = 1 / inbox.getMessageCount();
                        }else{
                            messages = inbox.getMessages(inbox.getMessageCount() - 100, inbox.getMessageCount());
                        }
                        FetchProfile fp = new FetchProfile();
                        fp.add(FetchProfile.Item.ENVELOPE);
                        inbox.fetch(messages, fp);
                    }
//                    }
            }
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(messageFile));
                if (MainSceneController.getMessages() != null && messages != null) {
                    for (int i = 0; i < messages.length; i++) {
                        if (step != -2d) {
                            MainSceneController.getMessages().add(new MessageDTO(messages[i]));
                        }
                        progress+=step;
                        notifyPreloader(new Preloader.ProgressNotification(progress));
                    }
                }
                out.writeObject(MainSceneController.getMessages());
                out.flush();
                System.out.println("Messages written");
            } catch (MessagingException e) {
                Logger.getLogger(MailTest.class.getName()).log(Level.SEVERE, null, e);
                JOptionPane.showMessageDialog(null, "You currently have no internet connection.\nYou will be able to read only locally saved messages.");
            }
        } catch (IOException | ClassNotFoundException | MessagingException ex) {
            Logger.getLogger(MailTest.class.getName()).log(Level.SEVERE, null, ex);
            if (ex instanceof EOFException) {
                File file = new File("cache.blurp");
                file.delete();
            }
        } finally {
            try {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MailTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        }else{
            SettingsDTO.createFile();
        }
        
        //TODO Create folder tree here
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainScene/MainScene.fxml"));
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Mailer");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        stage.centerOnScreen();
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent t) {
                MainSceneController.t.interrupt();
            }
        });

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