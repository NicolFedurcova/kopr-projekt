package sk.upjs.kopr.file_copy.client;

import java.util.concurrent.atomic.AtomicInteger;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ProgressFilesService extends Service<Void>{
	
	private AtomicInteger currentFileNumber;
	private final long totalFileNumber;
	
		
	public ProgressFilesService(int currentFileNumber, long totalFileNumber) {
		this.currentFileNumber = new AtomicInteger(currentFileNumber);
		this.totalFileNumber = totalFileNumber;
	}
	
	
	public void incrementFileNumberProgress() {
		this.currentFileNumber.incrementAndGet();
	}

	
	
	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				System.out.println("NUMBER:::::::: curr: " + currentFileNumber.get() + " total " + totalFileNumber);
				updateProgress(currentFileNumber.get(), totalFileNumber);
				
				while(currentFileNumber.get()<totalFileNumber) {
					//System.out.println("UPDATED NUMBER:::::::: curr: " + currentFileNumber.get() + " total " + totalFileNumber);
					
					updateProgress(currentFileNumber.get(), totalFileNumber);
				}
				updateProgress(currentFileNumber.get(), totalFileNumber);
				return null;
			}
			
		};
	}

}
