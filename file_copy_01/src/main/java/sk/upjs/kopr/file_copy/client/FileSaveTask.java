package sk.upjs.kopr.file_copy.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import sk.upjs.kopr.file_copy.server.Searcher;

public class FileSaveTask implements Runnable{
	private Socket dataSocket;
	private AtomicInteger progressNumberOfFiles;
	private AtomicLong progressSize;
	
	public FileSaveTask(Socket dataSocket, AtomicInteger progressNumberOfFiles, AtomicLong progressSize) {
		this.dataSocket = dataSocket;
		this.progressNumberOfFiles = progressNumberOfFiles;
		this.progressSize = progressSize;
		
	}

	@Override
	public void run() {
		try {
			ObjectInputStream ois = new ObjectInputStream(dataSocket.getInputStream());
			while(true) {
				String filePath = ois.readUTF();
				if(filePath.equals(Searcher.POISON_PILL.getName())) {
					break;
				}
				
				File fileBeingReceived = new File(filePath);
				File path = fileBeingReceived.getParentFile();
				path.mkdirs();
				long size = ois.readLong();
	            long offset = ois.readLong();				
				receiveData(ois, fileBeingReceived, size, offset);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	private void receiveData(ObjectInputStream ois, File fileBeingReceived, long size, long offset) {
		try (RandomAccessFile raf = new RandomAccessFile(fileBeingReceived, "rw")) {
			byte[] data = new byte[(int) size];
			raf.seek(offset);
			while (offset < size) {
				if(Thread.currentThread().isInterrupted()) {
					break;
				}
				if(size-offset < data.length) {
					//TU TREBA REAL THINKING VLEA HODIN NECHAVAM NA ZAJTRA
				}
				
			}
            
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	


}
