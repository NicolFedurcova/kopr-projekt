package sk.upjs.kopr.file_copy.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import sk.upjs.kopr.file_copy.FileInfo;
import sk.upjs.kopr.file_copy.FileRequest;
import sk.upjs.kopr.file_copy.client.ClientTesting;

public class FileSendTask implements Runnable {

	private static final int BLOCK_SIZE = 16384; // 16 kB
	private static BlockingQueue<File> filesToSend;
	private static ConcurrentHashMap<String, Long> fileInfosFromClient;
	private final Socket dataSocket;
	private File fileBeingSent;
	private Long offset;
	private Long size;
	
	public FileSendTask(BlockingQueue<File> filesToSend, Socket dataSocket,
			ConcurrentHashMap<String, Long> fileInfosFromClient) throws FileNotFoundException {
		this.filesToSend = filesToSend;
		this.fileInfosFromClient = fileInfosFromClient;
		this.dataSocket = dataSocket;
	}

	@Override
	public void run() {
		try {
			fileBeingSent = filesToSend.take();//takujem prvy
			ObjectOutputStream oos = new ObjectOutputStream(dataSocket.getOutputStream());
			
			while(fileBeingSent != Searcher.POISON_PILL) {
				
				String filePath = getTargetDestination(fileBeingSent);
				size = fileBeingSent.length();
				
				if(fileInfosFromClient==null || !fileInfosFromClient.containsKey(filePath)) {
					offset=(long) 0;
				} else {
					offset = fileInfosFromClient.get(filePath);
					/*
					if(offset == fileBeingSent.length()) {
						fileBeingSent = filesToSend.take();
						continue;
					}*/ //sa este rozhodnem ci toto hej
					
				}
				oos.writeUTF(filePath); //HADZEM ZE TENTO FILE PRIDE
				oos.writeLong(size); //S TAKOU VELKOSTOU 
				oos.writeLong(offset); //S TAKYM OFFSETOM 
				
				oos.flush();
				sendData(oos);
				fileBeingSent = filesToSend.take();
				
			}
			oos.writeUTF("POISON_PILL");
			System.out.println("socket on port: " + dataSocket.getPort() + "was killed by poison! watch out!");
			if (oos != null) oos.close();
			if (dataSocket != null && dataSocket.isConnected()) dataSocket.close();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			//WATCH OUT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			System.out.println("INTERRUPTED EXCEPTION");
			e.printStackTrace();
		}
	}
	
	private void sendData(ObjectOutputStream oos) throws IOException {
		try (RandomAccessFile raf = new RandomAccessFile(fileBeingSent, "r")) {
			byte[] data = new byte[BLOCK_SIZE];
			raf.seek(offset);
			//U GURSKEHO TU ESTE VYNIMKA
			while (offset < size) {
				//U GURSKEHO TU ESTE VYNIMKA

				if (size - offset < BLOCK_SIZE) {
					data = new byte[(int) (size - offset)];
				} else {
					data = new byte[BLOCK_SIZE];
				}
				
				
				offset += raf.read(data);
				oos.write(data, 0, 0);
			}
			oos.flush();
			raf.close();
			//   
		}
	}
	
	///POZOR TU VYSTUPUJE CLIENT TESTING!!!
	private String getTargetDestination(File file) {
		System.out.println("VYSLEDOK get target destination: " + ClientTesting.TARGET_DESTINATION+file.getAbsolutePath().substring(Server.FOLDER_TO_SHARE.getAbsolutePath().length()));
		return 	ClientTesting.TARGET_DESTINATION+file.getAbsolutePath().substring(Server.FOLDER_TO_SHARE.getAbsolutePath().length());
		
	}
	
	
}
