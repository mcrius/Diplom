/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailtest.SearchScene;

import com.sun.javafx.collections.ObservableListWrapper;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import mailtest.jpa.entities.MailMessage;
import mailtest.runnables.SearchInMailThread;
import mailtest.runnables.SearchThread;

/**
 * FXML Controller class
 *
 * @author bzzzt
 */
public class SearchSceneController implements Initializable {

    public static final String[] FIELDS = new String[] {"from", "to", "cc", "subject", "body"};
    List<MailMessage> results;
    @FXML private static TableView<MailMessage> table;
    @FXML private Button searchButton;
    @FXML private TextField toField;
    @FXML private TextField fromField;
    @FXML private TextField ccField;
    @FXML private TextField subjectField;
    @FXML private TextField bodyField;
    @FXML private RadioButton cacheRadio;
    @FXML private RadioButton entireRadio;
    @FXML private TableColumn fromColumn;
    @FXML private TableColumn subjectColumn;
    @FXML private TableColumn receivedColumn;
    @FXML private static ProgressIndicator progress;
    @FXML private WebView webView;

    
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        results = new ArrayList<>();
        webView.cacheProperty().setValue(false);
        webView.setContextMenuEnabled(false);
        webView.getEngine().setJavaScriptEnabled(false);
        webView.getEngine().getHistory().setMaxSize(1);
        fromColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.30f));
        subjectColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.50f));
        receivedColumn.prefWidthProperty().bind(table.widthProperty().multiply(0.17f));
        fromColumn.setCellValueFactory(new PropertyValueFactory<MailMessage, String>("from"));
        subjectColumn.setCellValueFactory(new PropertyValueFactory<MailMessage, String>("subject"));
        receivedColumn.setCellValueFactory(new PropertyValueFactory<MailMessage, String>("receivedDate"));
        receivedColumn.setSortType(TableColumn.SortType.DESCENDING);
        receivedColumn.setSortable(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
                MailMessage mm = (MailMessage) t1;
                if (mm != null) {
                    webView.getEngine().loadContent(mm.getBody());
                }
            }
        });
        table.setItems(new ObservableListWrapper(results));
    }

    private void closeWindow() {
        Stage thisStage = (Stage) searchButton.getScene().getWindow();
        thisStage.close();
    }
    
    @FXML
    public void handleSearch(ActionEvent e){
        boolean searchInCache = cacheRadio.isSelected();
        boolean searchInAcc = entireRadio.isSelected();
        if (searchInCache) {
            SearchThread searchThread = new SearchThread(fromField.getText(), toField.getText(),
                    ccField.getText(), subjectField.getText(), bodyField.getText());
            Thread t = new Thread(searchThread);
            t.start();
        }else{
            if (searchInAcc) {
                SearchInMailThread simt = new SearchInMailThread(fromField.getText(), toField.getText(),
                    ccField.getText(), subjectField.getText(), bodyField.getText());
                Thread t = new Thread(simt);
                t.start();
            }
        }
    }

    public static TableView<MailMessage> getTable() {
        return table;
    }

    public static ProgressIndicator getProgress() {
        return progress;
    }
    
    
    
}
