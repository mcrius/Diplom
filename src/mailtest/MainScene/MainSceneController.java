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
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.swing.JOptionPane;
import mailtest.MailTest;
import mailtest.SettingsController;
import mailtest.dto.MessageDTO;
import mailtest.dto.SettingsDTO;
import mailtest.runnables.OpenFolderRun;

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
    public static List<MessageDTO> messages = new ArrayList<>();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        init();
    }

    public void init() {
        indicator.setVisible(true);
        if (SettingsDTO.checkFile()) {
            try {
                tree.setRoot(new TreeItem<>(SettingsDTO.readDtoFromFile().getUsername()));
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            tree.setVisible(false);
        }
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                
                initTreeView();
                indicator.setVisible(false);
            }
        });
        webView.cacheProperty().setValue(false);
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
        webView.getEngine().getHistory().setMaxSize(1);
        fromCol.prefWidthProperty().bind(table.widthProperty().multiply(0.30f));
        subjCol.prefWidthProperty().bind(table.widthProperty().multiply(0.50f));
        dateCol.prefWidthProperty().bind(table.widthProperty().multiply(0.17f));
        fromCol.setCellValueFactory(new PropertyValueFactory<MessageDTO, String>("from"));
        subjCol.setCellValueFactory(new PropertyValueFactory<MessageDTO, String>("subject"));
        dateCol.setCellValueFactory(new PropertyValueFactory<MessageDTO, String>("receivedDate"));
        dateCol.setSortType(TableColumn.SortType.DESCENDING);
        dateCol.setSortable(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
                tabPane.getTabs().get(1).setDisable(true);
                tabPane.getSelectionModel().select(tabPane.getTabs().get(0));
                MessageDTO dto = (MessageDTO) t1;
                if (dto != null) {
                    if (dto.getAttachmentNames() != null && dto.getAttachmentNames().length != 0) {
                        tabPane.getTabs().get(1).setDisable(false);
                        fileList.getItems().setAll(dto.getAttachmentNames());
                    }
                    webView.getEngine().loadContent(dto.getBody());
                }
            }
        });
        table.setItems(new ObservableListWrapper(messages));
    }

    public static List<MessageDTO> getMessages() {
        return messages;
    }

    public static void setMessages(List<MessageDTO> messages) {
        MainSceneController.messages = messages;
    }

    public void saveSelectedAttachment(ActionEvent e) {
        FileChooser fc = new FileChooser();
        String selected = fileList.getSelectionModel().getSelectedItem();
        System.out.println(selected.lastIndexOf("."));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(selected.substring(selected.lastIndexOf(".")) + " file", selected.substring(selected.lastIndexOf("."))));
//        fc.setInitialDirectory(new File(FileSystems.getDefault().getRootDirectories().iterator().next().toString() + selected.substring(selected.lastIndexOf("."))));
        File file = fc.showSaveDialog(null);
        try {
            file = new File(file.getCanonicalPath() + selected.substring(selected.lastIndexOf(".")));
        } catch (IOException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
        file.renameTo(new File(file.getAbsolutePath() + selected.substring(selected.lastIndexOf(".") - 1)));
        System.out.println(file.getName());
//        file.
        try {
            InputStream[] attachmentStreams;
            try (FileOutputStream fos = new FileOutputStream(file)) {
                MessageDTO dto = (MessageDTO) table.getSelectionModel().getSelectedItem();
                String[] name = new String[1];
                name[0] = fileList.getSelectionModel().getSelectedItem();
                attachmentStreams = dto.getAttachmentStreams(name);
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
                }else{
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
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
        DirectoryChooser dc = new DirectoryChooser();
        File pointer = dc.showDialog(null);
        try {
            String path = pointer.getCanonicalPath();
            MessageDTO dto = (MessageDTO) table.getSelectionModel().getSelectedItem();
            InputStream[] streams = dto.getAttachmentStreams(dto.getAttachmentNames());
            double step = 100 / streams.length;
            for (int i = 0; i < streams.length; i++) {
                saveProgress.setProgress(saveProgress.getProgress() + step);
                File file = new File(path + "\\" + dto.getAttachmentNames()[i]);
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
                        for (int j = 0; j < folders[i].list().length; j++) {
                            tree.getRoot().getChildren().get(i).getChildren().add(new TreeItem<>(folders[i].list()[j].getName()));
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
        statusLabel.setText("Opening folder " + tree.getSelectionModel().getSelectedItem().getValue());
        indicator.setVisible(true);
        if (tree.getSelectionModel().getSelectedItem().getValue().equalsIgnoreCase("inbox")) {
            table.setItems(new ObservableListWrapper(messages));
            statusLabel.setText("");
            indicator.setVisible(false);
        } else {
            try {
                Folder folder = null;
                if (tree.getSelectionModel().getSelectedItem().getParent().equals(tree.getRoot())) {
                    folder = MailTest.getStore().getFolder(tree.getSelectionModel().getSelectedItem().getValue());
                }else{
//                    folder = MailTest.getStore().getDefaultFolder().getName();
                    folder = MailTest.getStore().getFolder(tree.getSelectionModel().getSelectedItem().getParent().getValue() + "/" + tree.getSelectionModel().getSelectedItem().getValue());
                }
                
                Thread t = new Thread(new OpenFolderRun(folder, table, indicator, statusLabel));
                t.start();
            } catch (MessagingException ex) {
                Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    public void showSettingsScene(ActionEvent e){
        try {
            Parent root = FXMLLoader.load(SettingsController.class.getResource("SettingsScene.fxml"));
            Stage stage  = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
