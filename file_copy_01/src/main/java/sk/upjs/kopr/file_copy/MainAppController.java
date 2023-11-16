package sk.upjs.kopr.file_copy;


import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import sk.upjs.kopr.file_copy.client.ClientService;
import sk.upjs.kopr.file_copy.client.FileSaveTask;
import sk.upjs.kopr.file_copy.client.ProgressFilesService;
import sk.upjs.kopr.file_copy.client.ProgressSizeService;
import sk.upjs.kopr.file_copy.server.Searcher;
import sk.upjs.kopr.file_copy.server.Server;

public class MainAppController{
	
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
    
    @FXML
    private Text sizeLabel;
    
    @FXML
    private Text numberOfFilesLabel;
    
    
    @FXML
    private Label infoLabel;
    
    
    public static int CONNECTIONS;
    private DialogPane dialog;
    private Alert alert;
    private String css = this.getClass().getResource("file_copy.css").toExternalForm();
    public ConcurrentHashMap<String, Long> filesOfClient;
    private  int progressNumberOfFiles;
	private  long progressSize;
	public int totalNumberOfFiles;
	public long totalSizeOfFiles;
	public static final File TARGET_DESTINATION = new File("C:\\Users\\nicol\\Desktop\\cisla_copy");
	public static ProgressFilesService pfs;
	public static ProgressSizeService pss;
	private ClientService client;
	
    
    @FXML
    void initialize() {
    	sourceFolderNameText.setText(Server.FOLDER_TO_SHARE.getAbsolutePath());
    	destinationFolderNameText.setText(TARGET_DESTINATION.getAbsolutePath());
    	resumeButton.setDisable(true);
    	stopButton.setDisable(true);  
    	infoLabel.setText("");
    	alert = new Alert(AlertType.ERROR);
		alert.setTitle("CHYBA!");
		dialog = alert.getDialogPane();
		dialog.getStylesheets().add(css);
		dialog.getStyleClass().add("dialog");
		
    }
    
    @FXML
	void copy(ActionEvent event) {
		try {
			CONNECTIONS = Integer.parseInt(portCountTextField.getText());
			if(CONNECTIONS <=0) {
				alert.setContentText("Počet spojení musí byť vyplnený, kladný a musí byť celočíselnou hodnotou");
				alert.show();
				return;
			}
		} catch (NumberFormatException e) {
				alert.setContentText("Počet spojení musí byť vyplnený a musí byť celočíselnou hodnotou");
				alert.show();
				return;
		}
		try (Socket communicationSocket = new Socket("localhost", Server.SERVER_PORT)) {
			
			ObjectOutputStream oos = new ObjectOutputStream(communicationSocket.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(communicationSocket.getInputStream());
			
			
			filesOfClient = new ConcurrentHashMap<>();
			Long[] sizeLengthInfo = getFileInfo();
			CountDownLatch handshakeDone = new CountDownLatch(1);
			handshake(oos, ois,sizeLengthInfo, handshakeDone);
						
			pfs= new ProgressFilesService(progressNumberOfFiles, totalNumberOfFiles);
			pss = new ProgressSizeService(progressSize, totalSizeOfFiles);
			
			pss.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

				@Override
				public void handle(WorkerStateEvent event) {
					copyButton.setVisible(false);
					stopButton.setVisible(false);
					resumeButton.setVisible(false);
					infoLabel.setText("Sťahovanie bolo úspešne dokončené");
					alert.setTitle("Kopírovanie dokončené");
					alert.setAlertType(AlertType.INFORMATION);
					alert.setContentText("kopírovanie bolo úspešne dokončené");
					alert.show();
					return;
				}
			});
			
			pfs.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					return;
				}
			});
			
			progressBarFiles.progressProperty().bind(pfs.progressProperty());
			progressBarSize.progressProperty().bind(pss.progressProperty());
			pfs.start();
			pss.start();
			
			try {
				handshakeDone.await();
				communicationSocket.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			CountDownLatch gate = new CountDownLatch(CONNECTIONS);
			client = new ClientService(gate, pfs, pss, this.alert, this.infoLabel);
			client.start();
			
			
		} catch(ConnectException e1) {
			infoLabel.setText("Server bol zastavený, spustite server a skúste znova");
			stopButton.setDisable(true);
			alert.setContentText("Server nie je dostupný, spustite server a skúste znova");	
			alert.show();
			copyButton.setDisable(false);
			copyButton.setVisible(true);
			portCountTextField.setDisable(false);			
			return;
			
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
									
		copyButton.setVisible(false);
		stopButton.setDisable(false);
		infoLabel.setText("Prebieha kopírovanie...");
		portCountTextField.setDisable(true);		
    }

    @FXML
    void stop(ActionEvent event) {
    	infoLabel.setText("Kopírovanie sa zastavilo...");
    	client.cancel();
    	resumeButton.setDisable(false);
    	stopButton.setDisable(true);
    	portCountTextField.setDisable(true);
    }
    
    @FXML
    void resume(ActionEvent event) {
    	infoLabel.setText("Kopírovanie sa obnovilo...");
    	portCountTextField.setDisable(true);
    	resumeButton.setDisable(true);
    	stopButton.setDisable(false);
    	copy(event);
    }
    
    
    
    private Long[] getFileInfo() {
		Searcher searcher = new Searcher(TARGET_DESTINATION, filesOfClient);
		return searcher.call();
	}
	
    
    private void handshake(ObjectOutputStream oos, ObjectInputStream ois, Long[] sizeLengthInfo, CountDownLatch handshakeDone) {
		try {
			
			if (filesOfClient.isEmpty()) {
				oos.writeUTF("I WANT IT ALL");
				oos.writeInt(CONNECTIONS);
				System.out.println("client sends: I WANT IT ALL, number of connections: " + CONNECTIONS);
				progressNumberOfFiles = 0;
				progressSize = 0;
							
			} else {
				oos.writeUTF("I ALREADY HAVE SOMETHING");
				oos.writeObject(filesOfClient);
				oos.writeInt(CONNECTIONS);
				System.out.println("client sends: I ALREADY HAVE SOMETHING: " + filesOfClient.toString()
						+ "\n with number of connections: " + CONNECTIONS);
				progressNumberOfFiles = sizeLengthInfo[1].intValue();
				progressSize = sizeLengthInfo[0];
			}
			oos.flush();

			String message = ois.readUTF();
			if (message.equals("OK, HERE IS INFO ABOUT WHAT I HAVE")) {
				System.out.println("client gets: " + message);
				totalNumberOfFiles = ois.readInt();
				System.out.println("clint gets number of files: " + totalNumberOfFiles);
				totalSizeOfFiles = ois.readLong();
				System.out.println("client gets size of files: " + totalSizeOfFiles + " B");
				String message2 = ois.readUTF();
				System.out.println("client gets message: " + message2);
			}
			
			handshakeDone.countDown();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ois != null & oos!=null)
				try {
					ois.close();
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	    
}
