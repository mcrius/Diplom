/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailer.SendScene;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JOptionPane;
import mailer.MailTest;
import mailer.MainScene.MainSceneController;
import mailer.dto.SettingsDTO;

/**
 * FXML Controller class
 *
 * @author bzzzt
 */
public class SendSceneController implements Initializable {

    @FXML
    private HTMLEditor editor;
    @FXML
    private Button sendButton;
    @FXML
    private Button saveButton;
    @FXML
    private TextField toField;
    @FXML
    private TextField subjectField;
    private Address[] addressList;
    private List<File> files = new ArrayList<>();
    private boolean isReplyMode;

    public boolean isIsReplyMode() {
        return isReplyMode;
    }

    public void setIsReplyMode(boolean isReplyMode) {
        this.isReplyMode = isReplyMode;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isReplyMode = MainSceneController.isIsReplyMode();
        if (isReplyMode) {
            subjectField.setText("Re: " + MainSceneController.getReplyDto().getSubject());
            toField.setText(MainSceneController.getReplyDto().getFrom());
            editor.setHtmlText("<p> ------ Original Message ------</p></br>" + MainSceneController.getReplyDto().getBody());
        }
    }

    private boolean isEmailAddress(String address) {
        try {
            Address a = new InternetAddress(address);
            return true;
        } catch (AddressException ex) {
            Logger.getLogger(SendSceneController.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public void sendAction(ActionEvent e) {
        MimeMessage message = null;

        if (isReplyMode) {
            message = (MimeMessage) MainSceneController.getReplyMessage();
        }
        try {
            Session session = MailTest.getSession();
            Transport transport = session.getTransport(SettingsDTO.readDtoFromFile().getConnectionProperties().getProperty("mail.transport.protocol"));
            if (!isReplyMode) {
                message = new MimeMessage(session);
            }
            String to = toField.getText();
            String subject = subjectField.getText().trim();
            if (!to.trim().isEmpty()) {
                if (to.contains(",")) {
                    String[] split = to.split(",");
                    addressList = new Address[split.length];
                    for (int i = 0; i < split.length; i++) {
                        if (isEmailAddress(split[i])) {
                            addressList[i] = new InternetAddress(split[i]);
                        }
                    }
                } else {
                    addressList = new Address[1];
                    addressList[0] = new InternetAddress(to);
                }
                for (int i = 0; i < addressList.length; i++) {
                    message.addRecipient(Message.RecipientType.TO, addressList[i]);
                }
                message.setSubject(subject);
                message.setFrom(new InternetAddress(SettingsDTO.readDtoFromFile().getUsername()));
                Multipart content = new MimeMultipart();
                BodyPart text = new MimeBodyPart();
                text.setContent(editor.getHtmlText(), "text/html");
                content.addBodyPart(text);
                if (!files.isEmpty()) {
                    for (File file : files) {
                        BodyPart part = new MimeBodyPart();
                        part.setFileName(file.getName());
                        part.setDisposition(Part.ATTACHMENT);
                        part.setDataHandler(new DataHandler(new FileDataSource(file)));
                        content.addBodyPart(part);
                    }
                }
                message.setContent(content);
                if (!transport.isConnected()) {
                    transport.connect(SettingsDTO.readDtoFromFile().getUsername(), SettingsDTO.readDtoFromFile().getPassword());
                }
                transport.sendMessage(message, addressList);
                transport.close();
            } else {
                JOptionPane.showMessageDialog(null, "Please enter at least one recipient.", "Error!", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException | ClassNotFoundException | NoSuchProviderException ex) {
            Logger.getLogger(SendSceneController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AddressException ex) {
            Logger.getLogger(SendSceneController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(SendSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
        MainSceneController.setIsReplyMode(false);
        MainSceneController.setReplyDto(null);
        MainSceneController.setReplyMessage(null);
        closeWindow();
    }

    public void attachAction(ActionEvent e) {
        FileChooser fc = new FileChooser();
        files = fc.showOpenMultipleDialog(null);
    }

    public void saveAction(ActionEvent e) {
        MimeMessage message = null;
        if (isReplyMode) {
            message = (MimeMessage) MainSceneController.getReplyMessage();
        }
        try {
            Session session = MailTest.getSession();
            Transport transport = session.getTransport(SettingsDTO.readDtoFromFile().getConnectionProperties().getProperty("mail.transport.protocol"));
            if (!isReplyMode) {
                message = new MimeMessage(session);
            }
            String to = toField.getText();
            String subject = subjectField.getText().trim();
            if (!to.trim().isEmpty()) {
                if (to.contains(",")) {
                    String[] split = to.split(",");
                    addressList = new Address[split.length];
                    for (int i = 0; i < split.length; i++) {
                        if (isEmailAddress(split[i])) {
                            addressList[i] = new InternetAddress(split[i]);
                        }
                    }
                } else {
                    addressList = new Address[1];
                    addressList[0] = new InternetAddress(to);
                }
                for (int i = 0; i < addressList.length; i++) {
                    message.addRecipient(Message.RecipientType.TO, addressList[i]);
                }
                message.setSubject(subject);
                message.setFrom(new InternetAddress(SettingsDTO.readDtoFromFile().getUsername()));
                Multipart content = new MimeMultipart();
                BodyPart text = new MimeBodyPart();
                text.setContent(editor.getHtmlText(), "text/html");
                content.addBodyPart(text);
                if (!files.isEmpty()) {
                    for (File file : files) {
                        BodyPart part = new MimeBodyPart();
                        part.setFileName(file.getName());
                        part.setDisposition(Part.ATTACHMENT);
                        part.setDataHandler(new DataHandler(new FileDataSource(file)));
                        content.addBodyPart(part);
                    }
                }
                message.setContent(content);
                Folder draft = null;
                if (SettingsDTO.readDtoFromFile().getUsername().contains("gmail.com")) {
                    draft = MailTest.getInbox().getStore().getFolder("[Gmail]/Drafts");
                } else {
                    draft = MailTest.getInbox().getStore().getFolder("Drafts");
                }
                message.setFlag(Flags.Flag.DRAFT, true);
                Message[] helper = new Message[1];
                helper[0] = message;
                if (!draft.isOpen()) {
                    draft.open(Folder.READ_WRITE);
                }
                draft.appendMessages(helper);
                draft.expunge();
            } else {
                JOptionPane.showMessageDialog(null, "Please enter at least one recipient.", "Error!", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | ClassNotFoundException | NoSuchProviderException ex) {
            Logger.getLogger(SendSceneController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AddressException ex) {
            Logger.getLogger(SendSceneController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(SendSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }
        MainSceneController.setIsReplyMode(false);
        MainSceneController.setReplyDto(null);
        MainSceneController.setReplyMessage(null);
        closeWindow();
    }

    private void closeWindow() {
        Stage thisStage = (Stage) sendButton.getScene().getWindow();
        thisStage.close();
    }
}
