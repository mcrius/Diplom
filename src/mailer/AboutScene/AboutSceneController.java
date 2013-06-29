/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mailer.AboutScene;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author bzzzt
 */
public class AboutSceneController implements Initializable {

    @FXML
    private Button close;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public void close(){
        Stage stage = (Stage) close.getScene().getWindow();
        stage.close();
    }
}
