package sk.upjs.kopr.file_copy.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import sk.upjs.kopr.file_copy.FileInfo;
import sk.upjs.kopr.file_copy.FileRequest;
import sk.upjs.kopr.file_copy.client.ClientTesting;

public class FileSendTask implements Runnable {

	public static final int BLOCK_SIZE = 16384; // 16 kB
	private static BlockingQueue<File> filesToSend;
	private static ConcurrentHashMap<String, Long> fileInfosFromClient;
	private final Socket dataSocket;
	private File fileBeingSent;
	private Long offset;
	private Long size;
	private ObjectOutputStream oos;
	
	public FileSendTask(BlockingQueue<File> filesToSend, Socket dataSocket,
			ConcurrentHashMap<String, Long> fileInfosFromClient) throws FileNotFoundException {
		this.filesToSend = filesToSend;
		this.fileInfosFromClient = fileInfosFromClient;
		this.dataSocket = dataSocket;
		//System.out.println("------VYTVORIL SA SEND TASK------ " + dataSocket.getPort());
		
	}

	@Override
	public void run() {
		try {
			fileBeingSent = filesToSend.take();//takujem prvy
			oos = new ObjectOutputStream(dataSocket.getOutputStream());
			
			while(fileBeingSent != Searcher.POISON_PILL) {
				
				String filePath = getTargetDestination(fileBeingSent);
				size = fileBeingSent.length();
				
				if(fileInfosFromClient==null || !fileInfosFromClient.containsKey(filePath)) {
					offset=(long) 0;
				} else {
					offset = fileInfosFromClient.get(filePath);
					
					if(offset == fileBeingSent.length()) {
						fileBeingSent = filesToSend.take();
						continue;
					}
					
				}
				oos.writeUTF(filePath); //HADZEM ZE TENTO FILE PRIDE
				//System.out.println( dataSocket.getPort() + "  IDE SA POSIELAT SUBOR: " + filePath + " vlaknom " + Thread.currentThread().getName());
				oos.writeLong(size); //S TAKOU VELKOSTOU 
				//System.out.println(dataSocket.getPort() + "  S TAKOU VELKOSTOU: " + size);
				oos.writeLong(offset); //S TAKYM OFFSETOM 
				//System.out.println(dataSocket.getPort() +"  S TAKYM OFFSETOM: " + offset);
				
				oos.flush();
				sendData(oos);
				fileBeingSent = filesToSend.take();
				
			}
			oos.writeUTF(Searcher.POISON_PILL.getName()); //TOTO ASI ENCHCEM TU MAT
			//System.out.println(Searcher.POISON_PILL.getName());
			//System.out.println("socket on port: " + dataSocket.getPort() + " was killed by poison! watch out!");
			/*
			if (oos != null) oos.close();
			if (dataSocket != null && dataSocket.isConnected()) dataSocket.close();
			*/
			
		} catch (SocketException e1) {
			System.err.println("******** "+ Thread.currentThread().getName()+"\n client is lost");
			/////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////
			//IDK CI TOTO MOZEM ALE SI INTERRUPTUJEM VLAKNO I GUESS?
			//Thread.currentThread().interrupt();
			//e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (InterruptedException e) {
			//WATCH OUT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			System.out.println("INTERRUPTED EXCEPTION");
			e.printStackTrace();
		}finally {
			try {
				if (oos != null) oos.close();
				if (dataSocket != null && dataSocket.isConnected()) dataSocket.close();
				System.out.println("******** "+ Thread.currentThread().getName()+" zatvaram si oos a ois vo finally");
			} catch (IOException e) {
				e.printStackTrace();
			}
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
				long prev = offset;
				offset += raf.read(data); //pozor k sa tu nastaví -1!!!!!!!!!!!!!
				
				//System.out.println(dataSocket.getPort() +" TOTO EJ POLE BAJTOV KTORE SA POSIELA: " + Arrays.toString(data)  + " vlaknom " + Thread.currentThread().getName());
				oos.write(data);
				oos.flush();
			}
			   
		}
	}
	
	///POZOR TU VYSTUPUJE CLIENT TESTING!!!
	private String getTargetDestination(File file) {
		//System.out.println("VYSLEDOK get target destination: " + ClientTesting.TARGET_DESTINATION+file.getAbsolutePath().substring(Server.FOLDER_TO_SHARE.getAbsolutePath().length()));
		return 	ClientTesting.TARGET_DESTINATION+file.getAbsolutePath().substring(Server.FOLDER_TO_SHARE.getAbsolutePath().length());
		
	}
	
	
}
