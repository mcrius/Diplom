/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest.MainScene;

import com.sun.javafx.collections.ObservableListWrapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.swing.JOptionPane;
import mailtest.AboutScene.AboutSceneController;
import mailtest.MailTest;
import mailtest.SearchScene.SearchSceneController;
import mailtest.SendScene.SendSceneController;
import mailtest.SettingsScene.SettingsController;
import mailtest.jpa.controllers.MailMessageJpaController;
import mailtest.jpa.controllers.SettingController;
import mailtest.jpa.controllers.exceptions.NonexistentEntityException;
import mailtest.jpa.entities.AttachmentName;
import mailtest.jpa.entities.MailMessage;
import mailtest.jpa.entities.Setting;
import mailtest.runnables.MessageListener;
import mailtest.runnables.OpenFolderRun;
import mailtest.utils.Utils;

/**
 * FXML Controller class
 *
 * @author vasil.georgiev
 */
public class MainSceneController implements Initializable {

    @FXML
    private TableView table;
    @FXML
    private WebView webView;
    @FXML
    private TableColumn fromCol;
    @FXML
    private TableColumn subjCol;
    @FXML
    private TableColumn dateCol;
    @FXML
    private TabPane tabPane;
    @FXML
    private TreeView<String> tree;
    @FXML
    private ListView<String> fileList;
    @FXML
    private ProgressBar saveProgress;
    @FXML
    private Label progressLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator indicator;
    public static List<MailMessage> messages = new ArrayList<>();
    public static Thread t;
    private static Message replyMessage;
    private static MailMessage replyDto;
    private static boolean isReplyMode = false;

    public static boolean isIsReplyMode() {
        return isReplyMode;
    }

    public static void setIsReplyMode(boolean isReplyMode) {
        MainSceneController.isReplyMode = isReplyMode;
    }

    public static Message getReplyMessage() {
        return replyMessage;
    }

    public static MailMessage getReplyDto() {
        return replyDto;
    }

    public static void setReplyMessage(Message replyMessage) {
        MainSceneController.replyMessage = replyMessage;
    }

    public static void setReplyDto(MailMessage replyDto) {
        MainSceneController.replyDto = replyDto;
    }

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        init();
    }

    public void init() {
        indicator.setVisible(true);
        SettingController sc = new SettingController(MailTest.getEmf());
        List<Setting> settings = sc.findSettingEntities(1, 0);
        if (settings != null && !settings.isEmpty()) {
            
            tree.setRoot(new TreeItem<>(settings.get(0).getUsername()));
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    initTreeView();
                    indicator.setVisible(false);
                }
            });
        }

        webView.cacheProperty().setValue(false);
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
        webView.getEngine().getHistory().setMaxSize(1);
        fromCol.prefWidthProperty().bind(table.widthProperty().multiply(0.30f));
        subjCol.prefWidthProperty().bind(table.widthProperty().multiply(0.50f));
        dateCol.prefWidthProperty().bind(table.widthProperty().multiply(0.17f));
        fromCol.setCellValueFactory(new PropertyValueFactory<MailMessage, String>("from"));
        subjCol.setCellValueFactory(new PropertyValueFactory<MailMessage, String>("subject"));
        dateCol.setCellValueFactory(new PropertyValueFactory<MailMessage, String>("receivedDate"));
        dateCol.setSortType(TableColumn.SortType.DESCENDING);
        dateCol.setSortable(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
                tabPane.getTabs().get(1).setDisable(true);
                tabPane.getSelectionModel().select(tabPane.getTabs().get(0));
                MailMessage mm = (MailMessage) t1;
                if (mm != null && mm.getAttachmentNames() != null) {
                    if (!mm.getAttachmentNames().isEmpty()) {
                        tabPane.getTabs().get(1).setDisable(false);
                        List<String> s = new ArrayList<>();
                        for (AttachmentName att : mm.getAttachmentNames()) {
                            s.add(att.getAttachmentName());
                        }
                        fileList.getItems().setAll(s);
                    }
                    webView.getEngine().loadContent(mm.getBody());
                }
            }
        });
        table.setItems(new ObservableListWrapper(messages));
        if (MailTest.getInbox() != null) {
            MessageListener ml = new MessageListener(MailTest.getInbox(), table, indicator, statusLabel);
            t = new Thread(ml);
            t.start();
        }
    }

    public static List<MailMessage> getMessages() {
        return messages;
    }

    public static void setMessages(List<MailMessage> messages) {
        MainSceneController.messages = messages;
    }

    public void saveSelectedAttachment(ActionEvent e) {
        FileChooser fc = new FileChooser();
        String selected = fileList.getSelectionModel().getSelectedItem();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(selected.substring(selected.lastIndexOf(".")) + " file", selected.substring(selected.lastIndexOf("."))));
        File file = fc.showSaveDialog(null);
        try {
            file = new File(file.getCanonicalPath() + selected.substring(selected.lastIndexOf(".")));
        } catch (IOException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
        file.renameTo(new File(file.getAbsolutePath() + selected.substring(selected.lastIndexOf(".") - 1)));
        try {
            InputStream[] attachmentStreams;
            try (FileOutputStream fos = new FileOutputStream(file)) {
                MailMessage dto = (MailMessage) table.getSelectionModel().getSelectedItem();
                String[] name = new String[1];
                name[0] = fileList.getSelectionModel().getSelectedItem();
                attachmentStreams = Utils.getAttachmentStreams(name, dto.getMessageId());
                if (attachmentStreams.length > 0) {
                    progressLabel.setText("Saving " + name[0]);
                    byte[] buffer = new byte[4096];
                    saveProgress.setProgress(0);
                    progressLabel.setVisible(true);
                    saveProgress.setVisible(true);
                    double step = attachmentStreams[0].available() / 4096;
                    while (attachmentStreams[0].read(buffer) != -1) {
                        fos.write(buffer);
                        buffer = new byte[4096];
                        saveProgress.setProgress(saveProgress.getProgress() + step);
                    }
                    attachmentStreams[0].close();
                } else {
                    JOptionPane.showMessageDialog(null, "Could not download file. Something went wrong,");
                }
            }
            progressLabel.setVisible(false);
            saveProgress.setVisible(false);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void saveAllAttachments(ActionEvent e) {
        progressLabel.setText("Downloading all attachments.");
        progressLabel.setVisible(true);
        saveProgress.setProgress(0d);
        saveProgress.setVisible(true);
//        try {
//            Thread.sleep(10);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
//        }
        DirectoryChooser dc = new DirectoryChooser();
        File pointer = dc.showDialog(null);
        try {
            String path = pointer.getCanonicalPath();
            MailMessage dto = (MailMessage) table.getSelectionModel().getSelectedItem();
            InputStream[] streams = Utils.getAttachmentStreams(dto.getAttachmentNames(), dto.getMessageId());
            double step = 100 / streams.length;
            for (int i = 0; i < streams.length; i++) {
                saveProgress.setProgress(saveProgress.getProgress() + step);
                File file = new File(path + "\\" + dto.getAttachmentNames().get(i).getAttachmentName());
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] buffer = new byte[2048];
                    while (streams[i].read(buffer) != -1) {
                        fos.write(buffer);
                        buffer = new byte[2048];
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
        progressLabel.setText("Done");
        progressLabel.setVisible(false);
        saveProgress.setVisible(false);
    }

    public void initTreeView() {
        try {
            tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            tree.getRoot().getChildren().add(new TreeItem<>("Inbox"));
            Folder[] folders = MailTest.getFolders();
            if (folders != null) {
                for (int i = 0; i < folders.length; i++) {
                    if (!folders[i].getName().equalsIgnoreCase("inbox")) {
                        tree.getRoot().getChildren().add(new TreeItem<>(folders[i].getName()));
                    }
                    if (folders[i].list().length > 0) {
                        for (Folder list : folders[i].list()) {
                            tree.getRoot().getChildren().get(i).getChildren().add(new TreeItem<>(list.getName()));
                        }
                    }
                }
            }
        } catch (MessagingException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
        tree.getRoot().setExpanded(true);
    }

    public void openFolderAction(ActionEvent e) {
        openFolder();
    }

    public void removeFolderAction(ActionEvent e) {
        Folder folder = null;
        if (tree.getSelectionModel().getSelectedItem().getParent().equals(tree.getRoot())) {
            try {
                folder = MailTest.getStore().getFolder(tree.getSelectionModel().getSelectedItem().getValue());
            } catch (MessagingException ex) {
                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                folder = MailTest.getStore().getFolder(tree.getSelectionModel().getSelectedItem().getParent().getValue() + "/" + tree.getSelectionModel().getSelectedItem().getValue());
            } catch (MessagingException ex) {
                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this folder?", "Delete?", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                if (folder.isOpen()) {
                    folder.close(true);
                }
                folder.delete(true);
                ObservableList<TreeItem<String>> children = tree.getRoot().getChildren();
                for (int i = 0; i < children.size(); i++) {
                    if (children.get(i).getValue().equals(folder.getName())) {
                        children.remove(i);
                    }
                }
            } catch (MessagingException ex) {
                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void addFolderAction(ActionEvent e) {
        Store store = MailTest.getStore();
        String result = JOptionPane.showInputDialog("Enter new folder name");
        try {
            Folder folder = store.getDefaultFolder().getFolder(result);
            folder.create(Folder.HOLDS_MESSAGES);
            tree.getRoot().getChildren().add(new TreeItem<>(result));
        } catch (MessagingException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void showSettingsScene(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(SettingsController.class.getResource("SettingsScene.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.getIcons().add(new Image(MailTest.class.getResourceAsStream("icon.png")));
            stage.setTitle("Account Settings");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void showSendScene(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(SendSceneController.class.getResource("SendScene.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.getIcons().add(new Image(MailTest.class.getResourceAsStream("icon.png")));
            stage.setTitle("New message");
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void showSearch(ActionEvent e){
        try {
            Parent root = FXMLLoader.load(SearchSceneController.class.getResource("SearchScene.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.getIcons().add(new Image(MailTest.class.getResourceAsStream("icon.png")));
            stage.setTitle("Search messages");
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void exportAction(ActionEvent e) {
//        FileChooser fc = new FileChooser();
//        fc.setTitle("Export settings.");
//        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("db file", ".db"));
//        File file = fc.showSaveDialog(null);
//        if (file != null) {
//            try {
//                file = new File(file.getCanonicalPath() + ".blurp");
//            } catch (IOException ex) {
//                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            try {
//                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
//                    out.writeObject(SettingsDTO.readDtoFromFile());
//                    out.flush();
//                }
//            } catch (IOException | ClassNotFoundException ex) {
//                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
    }

    public void importAction(ActionEvent e) {
//        FileChooser fc = new FileChooser();
//        fc.setTitle("Import settings file");
//        File file = fc.showOpenDialog(null);
//        if (file != null) {
//            try {
//                file = new File(file.getCanonicalPath() + ".blurp");
//            } catch (IOException ex) {
//                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            try {
//                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
//                    SettingsDTO.saveDtoToFile((SettingsDTO) in.readObject());
//                }
//            } catch (FileNotFoundException ex) {
//                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (IOException | ClassNotFoundException ex) {
//                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            JOptionPane.showMessageDialog(null, "Settings have been imported. Please start program again.");
//            t.interrupt();
//            Platform.exit();
//        }
    }

    public void onTreeSelect(MouseEvent e) {
        if (e.getClickCount() == 2) {
            openFolder();
        }
    }

    private void openFolder() {
        statusLabel.setText("Opening folder " + tree.getSelectionModel().getSelectedItem().getValue());
        indicator.setVisible(true);
        if (tree.getSelectionModel().getSelectedItem().getValue().equalsIgnoreCase("inbox")) {
            table.setItems(new ObservableListWrapper(messages));
            statusLabel.setText("");
            indicator.setVisible(false);
        } else {
            try {
                Folder folder = null;
                if (!tree.getSelectionModel().getSelectedItem().equals(tree.getRoot()) && tree.getSelectionModel().getSelectedItem().getParent().equals(tree.getRoot())) {
                    folder = MailTest.getStore().getFolder(tree.getSelectionModel().getSelectedItem().getValue());
                } else {
                    folder = MailTest.getStore().getFolder(tree.getSelectionModel().getSelectedItem().getParent().getValue() + "/" + tree.getSelectionModel().getSelectedItem().getValue());
                }

                t = new Thread(new OpenFolderRun(folder, table, indicator, statusLabel));
                t.start();
            } catch (MessagingException ex) {
                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void archiveMessage(ActionEvent e) {
        MailMessage dto = (MailMessage) table.getSelectionModel().getSelectedItem();
        try {
            Message m = MailTest.getInbox().getMessage(dto.getMessageId());
            Message[] msgs = new Message[1];
            msgs[0] = m;
            Folder folder = MailTest.getStore().getFolder("Archived");
            if (!folder.isOpen()) {
                folder.open(Folder.READ_WRITE);
            }
            MailTest.getInbox().copyMessages(msgs, folder);
            m.setFlag(Flags.Flag.DELETED, true);
            MailTest.getInbox().expunge();
        } catch (MessagingException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
        table.getItems().remove(table.getSelectionModel().getSelectedItem());
        messages.remove(dto);
        
        MailMessageJpaController mmc = new MailMessageJpaController(MailTest.getEmf());
        try {
            mmc.destroy(dto.getId());
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void deleteMessage(ActionEvent e) {
        if (table.getSelectionModel().getSelectedItem() != null) {
            int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this message?", "Delete?", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                try {
                    MailMessage dto = (MailMessage) table.getSelectionModel().getSelectedItem();
                    Message m = MailTest.getInbox().getMessage(dto.getId());
                    Message[] msgs = new Message[1];
                    msgs[0] = m;
                    m.setFlag(Flags.Flag.DELETED, true);
                    m.getFolder().expunge();
                    table.getItems().remove(table.getSelectionModel().getSelectedItem());
                    messages.remove(dto);

                    MailMessageJpaController mmc = new MailMessageJpaController(MailTest.getEmf());
                    try {
                        mmc.destroy(dto.getId());
                    } catch (NonexistentEntityException ex) {
                        Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (MessagingException ex) {
                    Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void forwardAction(ActionEvent e) {
        SettingController sc = new SettingController(MailTest.getEmf());
        Setting setting = new Setting();
        int settingCount = sc.getSettingCount();
        if (settingCount == 1) {
            setting = sc.findSettingEntities().get(0);
            try {
                MailMessage dto = (MailMessage) table.getSelectionModel().getSelectedItem();
                Message message = MailTest.getInbox().getMessage(dto.getMessageId());
                Message fwd = message.reply(true);
                fwd.setSubject("Fwd: " + message.getSubject());
                String recipient = JOptionPane.showInputDialog("Enter recipient");
                InternetAddress to = new InternetAddress(recipient);
                fwd.setRecipient(Message.RecipientType.TO, to);
                fwd.setFrom(new InternetAddress(setting.getUsername()));
                fwd.setContent((Multipart) message.getContent());
                Transport tr = MailTest.getSession().getTransport(Utils.getConnectionProperties(setting.getProperties()).getProperty("mail.transport.protocol"));
                Address[] adrss = new Address[1];
                adrss[0] = to;
                tr.connect(setting.getUsername(), setting.getPassword());
                tr.sendMessage(fwd, adrss);
                tr.close();
            } catch (MessagingException ex) {
                JOptionPane.showMessageDialog(null, "E-mail address is not valid", "Error!", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            JOptionPane.showMessageDialog(null, "No settings found", "Error!", JOptionPane.ERROR_MESSAGE);
        }

    }

    public void clearCacheAction(ActionEvent e) {
        File cache = new File("cache.blurp");
        cache.delete();
        JOptionPane.showMessageDialog(null, "Cache cleared. The new settings will be in effect after restart.");
    }

    public void replyAction(ActionEvent e) {
        replyDto = (MailMessage) table.getSelectionModel().getSelectedItem();
        if (replyDto != null) {
            try {
                replyMessage = MailTest.getInbox().getMessage(replyDto.getMessageId()).reply(true);
                isReplyMode = true;
                Parent root = FXMLLoader.load(SendSceneController.class.getResource("SendScene.fxml"));
                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.getIcons().add(new Image(MailTest.class.getResourceAsStream("icon.png")));
                stage.setTitle("Reply to ...");
                stage.setScene(scene);
                stage.show();
            } catch (    MessagingException | IOException ex) {
                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
    public void showAboutScene(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(AboutSceneController.class.getResource("AboutScene.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.getIcons().add(new Image(MailTest.class.getResourceAsStream("icon.png")));
            stage.setTitle("About");
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
