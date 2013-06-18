/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest.sendScene;

import com.sun.javafx.collections.ObservableListWrapper;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
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
    @FXML
    private TabPane tabPane;
    
    public static List<MessageDTO> messages = new ArrayList<>();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
                init();
    }

    public void init() {
        webView.cacheProperty().setValue(true);
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
        webView.getEngine().getHistory().setMaxSize(1);
        fromCol.prefWidthProperty().bind(table.widthProperty().multiply(0.30f));
        subjCol.prefWidthProperty().bind(table.widthProperty().multiply(0.50f));
        dateCol.prefWidthProperty().bind(table.widthProperty().multiply(0.20f));
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
                MessageDTO dto = (MessageDTO) t1;
                if (dto.getAttachmentNames().length != 0) {
                    System.out.println(Arrays.toString(dto.getAttachmentNames()));
                    tabPane.getTabs().get(1).setDisable(false);
                }
                webView.getEngine().loadContent(dto.getBody());
                
            }
        });
        table.setItems(new ObservableListWrapper(messages));
        
        
    }
    
    public static List<MessageDTO> getMessages() {
        return messages;
    }

    public static void setMessages(List<MessageDTO> messages) {
        SendController.messages = messages;
    }


    
}
