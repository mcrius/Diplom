/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest;

import java.util.ArrayList;
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
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JOptionPane;
import mailtest.MainScene.MainSceneController;
import mailtest.jpa.controllers.AttachmentNameJpaController;
import mailtest.jpa.controllers.MailMessageJpaController;
import mailtest.jpa.controllers.SettingController;
import mailtest.jpa.entities.AttachmentName;
import mailtest.jpa.entities.MailMessage;
import mailtest.jpa.entities.Setting;
import mailtest.jpa.entities.SettingsProperty;
import mailtest.utils.Utils;

/**
 *
 * @author vasil.georgiev
 */
public class MailTest extends Application {

    private static Message[] messages;
    private double step = 0d;
    private static Session session;
    private static EntityManagerFactory emf;

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

    public static EntityManagerFactory getEmf() {
        return emf;
    }

    public static void setEmf(EntityManagerFactory emf) {
        MailTest.emf = emf;
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

    private List<MailMessage> old;

    @Override
    public void init() {
        notifyPreloader(new Preloader.ProgressNotification(-1d));
        emf = Persistence.createEntityManagerFactory("MailerPU");
        double progress = 0d;
        Properties props = null;
        Platform.setImplicitExit(true);

        SettingController sg = new SettingController(emf);
        List<Setting> list = sg.findSettingEntities();
        if (list != null && !list.isEmpty()) {

        Setting settings = sg.findSettingEntities().get(0);
            List<SettingsProperty> properties = settings.getProperties();
            props = Utils.getConnectionProperties(properties);

            MailMessageJpaController mmc = new MailMessageJpaController(emf);
            old = mmc.findMailMessageEntities();
            MainSceneController.setMessages(old);

            

            session = Session.getDefaultInstance(props, null);
            try {
                store = session.getStore(props.getProperty("mail.store.protocol"));
            } catch (NoSuchProviderException ex) {
                Logger.getLogger(MailTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                store.connect(props.getProperty("mail.store.host"), settings.getUsername(), settings.getPassword());
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
                } else {
                    if (old != null && !old.isEmpty() && (old.get(old.size() - 1).getId() <= inbox.getMessages()[inbox.getMessageCount() - 1].getMessageNumber())) {
                        notifyPreloader(new Preloader.ProgressNotification(0d));
                        int lastSavedId = old.get(old.size() - 1).getMessageId();
                        int newestId = inbox.getMessages()[inbox.getMessageCount() - 1].getMessageNumber();
                        if ((newestId - lastSavedId) != 0) {
                            step = 1 / (newestId - lastSavedId);
                        } else {
                            step = -2d;
                        }
                        System.out.println("Step is : " + step);
                        messages = inbox.getMessages(inbox.getMessageCount() - (newestId - lastSavedId), inbox.getMessageCount());
                        FetchProfile fp = new FetchProfile();
                        fp.add(FetchProfile.Item.ENVELOPE);
                        inbox.fetch(messages, fp);
                    } else {
                        notifyPreloader(new Preloader.ProgressNotification(0d));
                        notifyPreloader(new Preloader.ErrorNotification("Downloading recent messages.", "", null));
                        step = 0.03d;
                        int count = inbox.getMessageCount() - 30;
                        if (count < 0) {
                            messages = inbox.getMessages();
                            step = 1 / inbox.getMessageCount();
                        } else {
                            messages = inbox.getMessages(inbox.getMessageCount() - 100, inbox.getMessageCount());
                        }
                        FetchProfile fp = new FetchProfile();
                        fp.add(FetchProfile.Item.ENVELOPE);
                        inbox.fetch(messages, fp);
                    }
                }

                if (MainSceneController.getMessages() != null && messages != null) {
                    for (Message message : messages) {
                        if (step != -2d) {
                            MailMessage mailMessage = new MailMessage(message);
                            List<AttachmentName> atts = Utils.createAttachmentNames(message, mailMessage);
                            mmc.create(mailMessage);
                            MainSceneController.getMessages().add(mailMessage);
                            AttachmentNameJpaController ac = new AttachmentNameJpaController(MailTest.getEmf());
                            for (AttachmentName f : atts) {
                                ac.create(f);
                            }
                        }
                        progress += step;
                        notifyPreloader(new Preloader.ProgressNotification(progress));
                    }
                }
            } catch (MessagingException e) {
                Logger.getLogger(MailTest.class.getName()).log(Level.SEVERE, null, e);
                JOptionPane.showMessageDialog(null, "You currently have no internet connection.\nYou will be able to read only locally saved messages.");
            } catch (Exception e) {
                Logger.getLogger(MailTest.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    //TODO Create folder tree here
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainScene/MainScene.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("MailChecker");
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
