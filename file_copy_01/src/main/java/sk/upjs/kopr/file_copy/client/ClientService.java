package sk.upjs.kopr.file_copy.client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import sk.upjs.kopr.file_copy.MainAppController;
import sk.upjs.kopr.file_copy.server.Server;

public class ClientService extends Service<Boolean> {

	
	private CountDownLatch gate;
	public static ProgressFilesService pfs;
	public static ProgressSizeService pss;
	private ExecutorService executor;
	private Label label;
	private Alert alert;

	public ClientService(CountDownLatch gate, ProgressFilesService pfs, ProgressSizeService pss, Alert aert, Label label){
		this.gate = gate;
		this.label = label;
		this.alert = alert;
		this.pfs = pfs;
		this.pss = pss;
	}


	@Override
	protected Task<Boolean> createTask(){
		return new Task<Boolean>(){

			@Override
			protected Boolean call() {

				try {
					executor = Executors.newCachedThreadPool();
					for (int i = 0; i < MainAppController.CONNECTIONS; i++) {
						Socket dataSocket = new Socket("localhost", Server.SERVER_PORT);
			        	FileSaveTask fileSaveTask = new FileSaveTask(dataSocket,gate, alert, label);
						executor.execute(fileSaveTask);
			        }
					executor.shutdown();
					
				
					try {
						gate.await();
					} catch (InterruptedException e) {
						executor.shutdownNow();
					}
			       
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
		};
	}
}
