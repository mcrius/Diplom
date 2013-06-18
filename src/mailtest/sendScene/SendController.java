/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest.sendScene;

import com.sun.javafx.collections.ObservableListWrapper;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import mailtest.MailTest;
import mailtest.dto.MessageDTO;

/**
 * FXML Controller class
 *
 * @author vasil.georgiev
 */
public class SendController implements Initializable {

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
    private boolean hasHTML;
    @FXML
    private TabPane tabPane;
    private boolean textIsHtml = false;
    
    public static List<MessageDTO> messages = new ArrayList<>();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
//        
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
                init();
//            }
//        });
    }

    public void init() {
        webView.cacheProperty().setValue(true);
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
        webView.getEngine().getHistory().setMaxSize(1);
//        fromCol.setCellValueFactory(new Callback<CellDataFeatures<Message, String>, ObservableValue<String>>() {
//            @Override
//            public ObservableValue<String> call(CellDataFeatures<Message, String> p) {
//                try {
//                    return new ReadOnlyObjectWrapper<>(p.getValue().getFrom()[0].toString());
//                } catch (MessagingException ex) {
//                    Logger.getLogger(SendController.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                return new ReadOnlyObjectWrapper<>("");
//            }
//        });
        fromCol.prefWidthProperty().bind(table.widthProperty().multiply(0.30f));
        subjCol.prefWidthProperty().bind(table.widthProperty().multiply(0.50f));
        dateCol.prefWidthProperty().bind(table.widthProperty().multiply(0.20f));
//        subjCol.setCellValueFactory(new PropertyValueFactory<Message, String>("subject"));
//        dateCol.setCellValueFactory(new PropertyValueFactory<Message, Date>("receivedDate"));
        fromCol.setCellValueFactory(new PropertyValueFactory<MessageDTO, String>("from"));
        subjCol.setCellValueFactory(new PropertyValueFactory<MessageDTO, String>("subject"));
        dateCol.setCellValueFactory(new PropertyValueFactory<MessageDTO, String>("receivedDate"));
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
                tabPane.getTabs().get(1).setDisable(true);
//                final Message m = (Message) t1;
                MessageDTO dto = (MessageDTO) t1;
                webView.getEngine().loadContent(dto.getBody());
//                System.out.println(m.getMessageNumber());
//                try {
//                    String body = getText(m);
//                    if (body != null) {
//                        webView.getEngine().loadContent(body);
//                    }
//                } catch (        MessagingException | IOException ex) {
//                    Logger.getLogger(SendController.class.getName()).log(Level.SEVERE, null, ex);
//                }

            }
        });
//        table.setItems(new ObservableListWrapper(Arrays.asList(MailTest.getMessages())));
        table.setItems(new ObservableListWrapper(messages));
    }
    
    /**
     * Return the primary text content of the message.
     */
    private String getText(Part p) throws MessagingException, IOException {
        if (p != null) {


            if (p.isMimeType("text/*")) {
                String s = (String) p.getContent();
                textIsHtml = p.isMimeType("text/html");
                return s;
            }

            if (p.isMimeType("multipart/alternative")) {
                // prefer html text over plain text
                Multipart mp = (Multipart) p.getContent();
                String text = null;
                for (int i = 0; i < mp.getCount(); i++) {
                    Part bp = mp.getBodyPart(i);
                    if (bp.isMimeType("text/plain")) {
                        if (text == null) {
                            text = getText(bp);
                        }
                        continue;
                    } else if (bp.isMimeType("text/html")) {
                        String s = getText(bp);
                        if (s != null) {
                            return s;
                        }
                    } else {
                        return getText(bp);
                    }
                }
                return text;
            } else if (p.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) p.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    String s = getText(mp.getBodyPart(i));
                    if (s != null) {
                        return s;
                    }
                }
            }
        }
            return null;
        
    }

    //                try {
    //                    if (m.getContent() instanceof String) {
    //                        webView.getEngine().loadContent((String) m.getContent());
    //                    } else {
    //                        Multipart mes = (Multipart) m.getContent();
    //                        System.out.println(">>>" + mes.getCount());
    //                        for (int i = 0; i < mes.getCount(); i++) {
    //                            if (mes.getBodyPart(i).isMimeType("TEXT/HTML")) {
    //                                webView.getEngine().loadContent((String) mes.getBodyPart(i).getContent());
    //                                hasHTML = true;
    //                                break;
    //                            }
    //                        }
    //                        if (!hasHTML) {
    //                            for (int i = 0; i < mes.getCount(); i++) {
    //                                if (mes.getBodyPart(i).isMimeType("TEXT/PLAIN")) {
    //                                    webView.getEngine().loadContent((String) mes.getBodyPart(i).getContent());
    //                                    break;
    //                                } else {
    //                                    if (mes.getBodyPart(i).getContentType().contains("multipart")) {
    //                                        Multipart inner = (Multipart) mes.getBodyPart(i).getContent();
    //                                        System.out.println("inner >>" + inner.getCount());
    //                                        webView.getEngine().loadContent((String) inner.getBodyPart(i + 1).getContent());
    //                                        break;
    //                                    }
    //                                }
    //                            }
    //                        }
    //                        for (int i = 0; i < mes.getCount(); i++) {
    //                            System.out.println(mes.getBodyPart(i).getDisposition());
    //                            if (mes.getBodyPart(i).getContentType().contains("IMAGE")) {
    //                                tabPane.getTabs().get(1).setDisable(false);
    //                            }
    //                        }
    //                        hasHTML = false;
    //                    }
    //
    //                } catch (IOException | MessagingException ex) {
    //                }
    //                }
    public static List<MessageDTO> getMessages() {
        return messages;
    }

    public static void setMessages(List<MessageDTO> messages) {
        SendController.messages = messages;
    }


    
}
