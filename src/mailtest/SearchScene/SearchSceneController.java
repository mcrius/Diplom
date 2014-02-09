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
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

/**
 * FXML Controller class
 *
 * @author bzzzt
 */
public class SearchSceneController implements Initializable {

    public static final String[] FIELDS = new String[] {"from", "to", "cc", "subject", "body"};
    List<MailMessage> results;
    @FXML private TableView<MailMessage> table;
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
    @FXML private ProgressIndicator progress;
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
        //TODO GENERATE QUERY
        StandardAnalyzer a = new StandardAnalyzer(Version.LUCENE_45);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_45, FIELDS, a);
//        try {
//            Query q = parser.parse(query);
//        } catch (ParseException ex) {
//            Logger.getLogger(SearchSceneController.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
        
    }
    
}
