package sk.upjs.kopr.file_copy.client;

import java.util.concurrent.atomic.AtomicInteger;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ProgressFilesService extends Service<Void>{
	
	private AtomicInteger currentFileNumber;
	private final long totalFileNumber;
	
	

	public ProgressFilesService(AtomicInteger currentFileNumber, long totalFileNumber) {
		this.currentFileNumber = currentFileNumber;
		this.totalFileNumber = totalFileNumber;
	}
	
	public void setFileNumberProgress(int progress) {
		this.currentFileNumber.set(progress);
	}
	
	
	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				while(currentFileNumber.get()<totalFileNumber) {
					updateProgress(currentFileNumber.get(), totalFileNumber);
				}
				return null;
			}
			
		};
	}

}
