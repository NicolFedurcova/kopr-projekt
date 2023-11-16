package sk.upjs.kopr.file_copy.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import sk.upjs.kopr.file_copy.MainAppController;
import sk.upjs.kopr.file_copy.server.FileSendTask;
import sk.upjs.kopr.file_copy.server.Searcher;

public class FileSaveTask implements Runnable{
	private Socket dataSocket;
	private CountDownLatch gate;
	private ObjectInputStream ois;
	private Label label;
	private Alert alert;
	
	
	public FileSaveTask(Socket dataSocket, CountDownLatch gate, Alert alert, Label label) {
		this.dataSocket = dataSocket;
		this.gate = gate;
		this.label = label;
		this.alert = alert;
		
	}

	@Override
	public void run() {
		try {
			
			ois = new ObjectInputStream(dataSocket.getInputStream());
			while(true) {
				if(Thread.currentThread().isInterrupted()) {
					return;
				}
				
				String filePath = ois.readUTF();
				if(filePath.equals(Searcher.POISON_PILL.getName())) {
					break;
				}
				
				File fileBeingReceived = new File(filePath);
				System.out.println(" file being received: " + filePath + " by thread: " + Thread.currentThread().getName());
				File path = fileBeingReceived.getParentFile();
				path.mkdirs();
				
				long size = ois.readLong();
	            long offset = ois.readLong();		
				
				receiveData(ois, fileBeingReceived, size, offset);
				
				ClientService.pfs.incrementFileNumberProgress();
								
			}
						
		} catch (SocketException e1){
			
			javafx.application.Platform.runLater(() -> {
			      alert.setContentText("Server bol zastavený! Spustite kopírovanie znova");
			      alert.show();
			      label.setText("Server bol zastavený, spustite server a skúste znova");
			      return;
		    });
			
		}catch (IOException e) {
			System.err.println("IO EXCEPTION");
			e.printStackTrace();
		} finally {
			gate.countDown();
			try {
				if (ois != null) ois.close();
				if (dataSocket != null && dataSocket.isConnected()) dataSocket.close();
			} catch (IOException e3) {
				e3.printStackTrace();
			}
		}
		
		
	}

	private void receiveData(ObjectInputStream ois, File fileBeingReceived, long size, long offset) throws SocketException {
		try (RandomAccessFile raf = new RandomAccessFile(fileBeingReceived, "rw")) {
			byte[] data = new byte[FileSendTask.BLOCK_SIZE];
			raf.seek(offset);
			
			int readLength = 0;
			while (offset < size) {
				if(Thread.currentThread().isInterrupted()) {
					if (raf!=null) raf.close();
					return;
				}
				
				if(size-offset < FileSendTask.BLOCK_SIZE) {
					data = new byte[(int)(size-offset)];
					readLength = ois.read(data); 
				} else {
					readLength = ois.read(data); 
				}
				
				ClientService.pss.incrementFileSizeProgress(readLength);
				raf.write(data,0,readLength);
				offset +=readLength;
				
				data = new byte[FileSendTask.BLOCK_SIZE];
								
			}
			raf.close();
            
		} catch (SocketException e1){
			throw e1;
		} catch (FileNotFoundException e3) {
			e3.printStackTrace();
		} catch (IOException e4) {
			e4.printStackTrace();
		} 
		
	}
	


}
