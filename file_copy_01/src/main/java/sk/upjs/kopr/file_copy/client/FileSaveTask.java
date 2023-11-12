package sk.upjs.kopr.file_copy.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import sk.upjs.kopr.file_copy.server.FileSendTask;
import sk.upjs.kopr.file_copy.server.Searcher;

public class FileSaveTask implements Runnable{
	private Socket dataSocket;
	private AtomicInteger progressNumberOfFiles;
	private AtomicLong progressSize;
	private int order;
	
	public FileSaveTask(Socket dataSocket, AtomicInteger progressNumberOfFiles, AtomicLong progressSize,int order) {
		this.dataSocket = dataSocket;
		this.progressNumberOfFiles = progressNumberOfFiles;
		this.progressSize = progressSize;
		this.order = order;
		
	}

	@Override
	public void run() {
		try {
			ObjectInputStream ois = new ObjectInputStream(dataSocket.getInputStream());
			while(true) { ///kym nepride poison pill
				
				String filePath = ois.readUTF();
				System.out.println(order + " this file is coming: " + filePath + " by thread: " + Thread.currentThread().getName());
				System.out.println(Searcher.POISON_PILL.getName());
				if(filePath.equals(Searcher.POISON_PILL.getName())) {
					
					break;
				}
				
				File fileBeingReceived = new File(filePath);
				//System.out.println(order + " file being received: " + filePath + " by thread: " + Thread.currentThread().getName());
				File path = fileBeingReceived.getParentFile();
				//System.out.println(order + " mkdir path: " + path);
				path.mkdirs();
				
				long size = ois.readLong(); //celkova velkost filu ktory prichadza
	            //System.out.println(order + " file is coming with size: " + size);
				long offset = ois.readLong();	//uvodny offset			
				//System.out.println(order + " file is coming with offset: " + offset);
				
				receiveData(ois, fileBeingReceived, size, offset);
				//System.out.println(order + "SKONCIL SOM");
				progressNumberOfFiles.incrementAndGet();
				
			}
			
			//System.out.println("socket: " + order + " was killed by poison! watch out!");
			if (ois != null) ois.close();
			if (dataSocket != null && dataSocket.isConnected()) dataSocket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	private void receiveData(ObjectInputStream ois, File fileBeingReceived, long size, long offset) {
		try (RandomAccessFile raf = new RandomAccessFile(fileBeingReceived, "rw")) {
			byte[] data = new byte[FileSendTask.BLOCK_SIZE];
			raf.seek(offset);
			//System.out.println(order  +" sa posuva na offset: " + offset + " z celkoveho size: " + size);
			
			
			int readLength = 0;
			while (offset < size) {
				//raf.seek(offset);
				if(Thread.currentThread().isInterrupted()) {
					break;
				}
				if(size-offset < FileSendTask.BLOCK_SIZE) {
					data = new byte[(int)(size-offset)];
					//readLength = ois.read(data, 0, (int)(size-offset)); 
					readLength = ois.read(data); 
				} else {
					//readLength = ois.read(data, 0, FileSendTask.BLOCK_SIZE);
					readLength = ois.read(data); 
				}
				//System.out.println(order +" TOTO EJ POLE BAJTOV KTORE SA PRIJIMA: " + Arrays.toString(data));
				progressSize.addAndGet(readLength);
				//raf.seek(offset);
				raf.write(data,0,readLength);
				//raf.write(data);
				//System.out.println(order + " OFFSET SA ZMENIL Z " + offset + " na " + (offset+readLength) );
				offset +=readLength;
				
				data = new byte[FileSendTask.BLOCK_SIZE];
								
			}
			
			
			raf.close();
            
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	


}
