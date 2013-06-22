/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest;

import com.sun.javafx.collections.ObservableListWrapper;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import mailtest.dto.SettingsDTO;

/**
 *
 * @author vasil.georgiev
 */
public class SettingsController implements Initializable {

    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private CheckBox manual;
    @FXML
    public TextField userField;
    @FXML
    public TextField incHostname;
    @FXML
    public TextField outHostname;
    @FXML
    public TextField incPort;
    @FXML
    public TextField outPort;
    @FXML
    private ComboBox<String> incSSL;
    @FXML
    private ComboBox<String> outSSL;
    @FXML
    public PasswordField passField;
    @FXML
    public GridPane grid;
    @FXML
    private Button cancel;
    @FXML
    private Button save;

//    @FXML
//    public void handleButtonAction(ActionEvent event) throws NoSuchProviderException {
//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                try {
//                    Properties props = System.getProperties();
//                    props.setProperty("mail.smtp.auth", "true");
//                    props.setProperty("mail.smtp.starttls.enable", "true");
//                    props.setProperty("mail.smtp.host", "smtp.gmail.com");
//                    props.setProperty("mail.smtp.port", "587");
//                    props.setProperty("mail.store.protocol", "imaps");
//                    props.setProperty("mail.imap.host", "imap.gmail.com");
//                    props.setProperty("mail.imap.port", "993");
//                    props.setProperty("mail.imap.timeout", "2000");
//                    Session s = Session.getDefaultInstance(props, null);
//                    Store store = s.getStore("imaps");
//                    store.connect("imap.gmail.com", userField.getText(), passField.getText());
//                    Folder[] folders = store.getDefaultFolder().list("*");
//                    for (int i = 0; i < folders.length; i++) {
//                        System.out.println(">>" + folders[i].getFullName());
//                    }
//                    final Folder f = store.getFolder("INBOX");
//                    final int messagesCount = f.getMessageCount();
//
//                    int newMessageCount = f.getUnreadMessageCount();
//
//                } catch (MessagingException ex) {
//                    Logger.getLogger(SettingsController.class.getName()).log(Level.WARNING, "Messaging Exc", ex);
//                }
//            }
//        });
//        t.start();
//
//    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        choiceBox.setItems(new ObservableListWrapper<>(Arrays.asList("IMAP", "POP3")));
        choiceBox.getSelectionModel().select(0);
        incSSL.setItems(new ObservableListWrapper<>(Arrays.asList("None", "STARTTLS", "SSL/TLS")));
        incSSL.getSelectionModel().select(0);
        outSSL.setItems(new ObservableListWrapper<>(Arrays.asList("None", "STARTTLS", "SSL/TLS")));
        outSSL.getSelectionModel().select(0);
        grid.setVisible(false);
//        if (!SettingsDTO.checkFile()) {
//            SettingsDTO.createFile();
//        }else{
//            JOptionPane.showMessageDialog(null, "You have already entered your e-mail account.\nDoing this again will replace the existing one.");
//        }
    }

    public void manualClick(ActionEvent e) {
        grid.setVisible(!grid.isVisible());
//            if (grid.isVisible()) {
//                grid.setPrefHeight(168d);
//            }else{
//                grid.setPrefHeight(0d);
//            }
    }

    public void saveAction(ActionEvent e) {
        try {
            SettingsDTO dto = SettingsDTO.readDtoFromFile();
            dto.setProperties(dto.makeProperties(incHostname.getText(), outHostname.getText(), incPort.getText(), outPort.getText(), incSSL.getValue(), outSSL.getValue(), choiceBox.getSelectionModel().getSelectedItem(), isGmail()));
            dto.setUsername(userField.getText());
            dto.setPassword(passField.getText());
            SettingsDTO.saveDtoToFile(dto);
            closeWindow();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(SettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        JOptionPane.showMessageDialog(null, "Settings were saved but you need to restart the application so they can take effect.");
    }
    
    public void closeAction(ActionEvent e){
        closeWindow();
    }
    
    private void closeWindow(){
        Stage thisStage = (Stage) cancel.getScene().getWindow();
        thisStage.close();
    }
    
    private boolean isGmail() {
        if (userField.getText().contains("gmail.com")) {
            return true;
        } else {
            return false;
        }
    }
    
}
