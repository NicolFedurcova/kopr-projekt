package sk.upjs.kopr.file_copy.client;

import java.util.concurrent.atomic.AtomicLong;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class ProgressSizeService extends Service<Void>{
	
	private AtomicLong currentFileSize;
	private final long totalFileSize;
	
	

	public ProgressSizeService(long currentFileSize, long totalFileSize) {
		this.currentFileSize = new AtomicLong(currentFileSize);
		this.totalFileSize = totalFileSize;
	}
	
	public void setFileSizeProgress(long progress) {
		this.currentFileSize.set(progress);
	}



	@Override
	protected Task<Void> createTask() {
	
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				while(currentFileSize.get()<totalFileSize) {
					updateProgress(currentFileSize.get(), totalFileSize);
				}
				return null;
			}
			
		};
				
	}

}
