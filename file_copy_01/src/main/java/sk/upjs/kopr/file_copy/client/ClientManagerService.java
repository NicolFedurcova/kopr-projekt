package sk.upjs.kopr.file_copy.client;

import java.util.concurrent.ExecutorService;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ClientManagerService extends Service<Void>{
	
	public static final String SAVE_ROUTE = "C:\\Users\\nicol\\Desktop\\mti-copy";
	private int numberOfConnections;
	
	private long fileSize;
	private int fileNumber;
	
	private ExecutorService executor;
	

	@Override
	protected Task<Void> createTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
