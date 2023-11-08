package sk.upjs.kopr.file_copy;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class MainAppController {
	
	 	@FXML
	    private Button copyButton;

	    @FXML
	    private Text folderNameText;

	    @FXML
	    private TextField portCountTextField;

	    @FXML
	    private ProgressBar progressBarFiles;
	    
	    @FXML
	    private ProgressBar progressBarSize;

	    @FXML
	    private Button resumeButton;

	    @FXML
	    private Button stopButton;
	    
	    @FXML
	    void initialize() {
	    	
	    }
	    
}
