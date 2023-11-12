package sk.upjs.kopr.file_copy;

import org.hamcrest.core.IsInstanceOf;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import sk.upjs.kopr.file_copy.client.ClientTesting;
import sk.upjs.kopr.file_copy.server.Server;

public class MainAppController {
	
	@FXML
    private Button copyButton;

    @FXML
    private Text destinationFolderNameText;

    @FXML
    private TextField portCountTextField;

    @FXML
    private ProgressBar progressBarFiles;

    @FXML
    private ProgressBar progressBarSize;

    @FXML
    private Button resumeButton;

    @FXML
    private Text sourceFolderNameText;

    @FXML
    private Button stopButton;
    
    private int connections;
    private DialogPane dialog;
    String css = this.getClass().getResource("file_copy.css").toExternalForm();

    @FXML
    void initialize() {
    	sourceFolderNameText.setText(Server.FOLDER_TO_SHARE.getAbsolutePath());
    	destinationFolderNameText.setText(ClientTesting.TARGET_DESTINATION.getAbsolutePath());
    	resumeButton.setDisable(true);
    	stopButton.setDisable(true);    	
    }
    
    @FXML
	void copy(ActionEvent event) {
    	Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("CHYBA!");
		dialog = alert.getDialogPane();
		dialog.getStylesheets().add(css);
		dialog.getStyleClass().add("dialog");
		
		try {
			connections = Integer.parseInt(portCountTextField.getText());
			if(connections <=0) {
				alert.setContentText("Počet spojení musí byť vyplnený, kladný a musí byť celočíselnou hodnotou");
				alert.show();
				return;
			}
		} catch (NumberFormatException e) {
				alert.setContentText("Počet spojení musí byť vyplnený a musí byť celočíselnou hodnotou");
				alert.show();
				return;
		}
		
		ClientTesting client = new ClientTesting(connections);
		client.start();
		progressBarFiles.progressProperty().bind(client.pfs.progressProperty());
		progressBarSize.progressProperty().bind(client.pss.progressProperty());
		copyButton.setDisable(true);
		stopButton.setDisable(false);
		
    }
    
    @FXML
    void stop(ActionEvent event) {
    	resumeButton.setDisable(false);
    	stopButton.setDisable(true);

    }
    
    @FXML
    void resume(ActionEvent event) {
    	resumeButton.setDisable(true);
    	stopButton.setDisable(false);

    }

    

    
    
	    
}
