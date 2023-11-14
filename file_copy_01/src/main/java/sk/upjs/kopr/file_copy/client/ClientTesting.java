package sk.upjs.kopr.file_copy.client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.HandshakeCompletedEvent;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import sk.upjs.kopr.file_copy.MainAppController;
import sk.upjs.kopr.file_copy.server.FileSendTask;
import sk.upjs.kopr.file_copy.server.Searcher;
import sk.upjs.kopr.file_copy.server.Server;

public class ClientTesting extends Service<Boolean> {

	public static final File TARGET_DESTINATION = new File("C:\\Users\\nicol\\Desktop\\cisla_copy");
	public static  int CONNECTIONS;
	private int totalNumberOfFiles;
	private long totalSizeOfFiles;
	private CountDownLatch gate;
	public static ProgressFilesService pfs;
	public static ProgressSizeService pss;
	private ExecutorService executor;

	public ClientTesting(int connections, CountDownLatch gate, ProgressFilesService pfs, ProgressSizeService pss){
		this.CONNECTIONS = connections;
		this.gate = gate;
		this.pfs = pfs;
		this.pss =pss;
		
	}
	
	public void stop() {
		try {
			if (executor!=null) {
				executor.shutdownNow();
				System.out.println("SA VYPOL EXECUTOR");
				
				gate.await(); ////////////////////////////////////TUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU
				System.out.println("SA PRESLO CEZ GATE");
				
				cancel();
				System.out.println("SA SPRAVIL CANCEL");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		/*
		try {
			if (executor!=null) {
				executor.shutdownNow();
				System.out.println("SA VYPOL EXECUTOR");
				
				gate.await();
				System.out.println("SA PRESLO CEZ GATE");
				
				cancel();
				System.out.println("SA SPRAVIL CANCEL");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} */
	}

	@Override
	protected Task<Boolean> createTask(){
		return new Task<Boolean>(){

			@Override
			protected Boolean call() {
				
				try {
					executor = Executors.newCachedThreadPool();
					for (int i = 0; i < CONNECTIONS; i++) {
						Socket dataSocket = new Socket("localhost", Server.SERVER_PORT);
			        	FileSaveTask fileSaveTask = new FileSaveTask(dataSocket,i, gate );
						executor.execute(fileSaveTask);
			        }
			        executor.shutdown();
					
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
			/*
			@Override
			public void cancelled() {
				try {
					if (executor!=null) {
						executor.shutdownNow();
						System.out.println("SA VYPOL EXECUTOR");
						gate.await();
						System.out.println("SA PRESLO CEZ GATE");
						cancel();
						System.out.println("SA SPRAVIL CANCEL");
					}
				} catch (InterruptedException e) {
					System.out.println("INTERRUPTEEEEEEEEEEED/n!/n!/n!");
					e.printStackTrace();
				}
			}*/
		};
	}
}
