package sk.upjs.kopr.file_copy.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


public class Server {

	public static final int SERVER_PORT = 5000;
	public static final File FOLDER_TO_SHARE = new File("C:\\Users\\nicol\\Desktop\\cisla");
	private static BlockingQueue<File> filesToSend;
	private static ConcurrentHashMap<String, Long> fileInfosFromClient; 
	private static int connections; 
	private static Long totalFolderSize;
	private static int numberOfFiles;
	
	
	public static void main(String[] args) throws FileNotFoundException {
		
		if (! FOLDER_TO_SHARE.exists() || ! FOLDER_TO_SHARE.isDirectory()) {
			throw new FileNotFoundException("No such folder: " + FOLDER_TO_SHARE);
		}
		try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
			System.out.println("Server is running on port " + SERVER_PORT + " ...");
			System.out.println("Sharing folder " + FOLDER_TO_SHARE);
			ExecutorService executor = Executors.newCachedThreadPool();
			while(true) {
				
				Socket communicationSocket = serverSocket.accept();
				ObjectInputStream ois = new ObjectInputStream(communicationSocket.getInputStream());
				ObjectOutputStream oos = new ObjectOutputStream(communicationSocket.getOutputStream());
				fileInfosFromClient = null;
				filesToSend = new LinkedBlockingQueue<File>();		
				handshake(oos, ois);
				System.out.println("FILES TO SEND: ");
				System.out.println(Arrays.toString(filesToSend.toArray()));
				
			
				
		        for (int i = 0; i < connections; i++) {		
					Socket dataSocket = serverSocket.accept();
		        	FileSendTask fileSendTask = new FileSendTask(filesToSend, dataSocket, fileInfosFromClient);
		        	executor.execute(fileSendTask);
		        }
		        communicationSocket.close();
					
						
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} 
		
	}
	private static void getFolderContent(int connections) {
		Searcher searcher = new Searcher(FOLDER_TO_SHARE, filesToSend, connections);
		Long [] result = searcher.call();
		totalFolderSize = result[0];
		numberOfFiles = result[1].intValue();
		}
	
	private static void handshake(ObjectOutputStream oos, ObjectInputStream ois) {
		try {
			String message = ois.readUTF();
			System.out.println("server got message: " + message);
			if(message.equals("I ALREADY HAVE SOMETHING")) {
				try {
					fileInfosFromClient = (ConcurrentHashMap<String, Long>) ois.readObject();
					System.out.println("server got fileinfos from clinet: " + fileInfosFromClient.toString());
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			} 
			else if(!message.equals("I WANT IT ALL")) {
				oos.writeUTF("unknown message");
				return;
			}
			connections = ois.readInt();
			System.out.println("server gets: NUMBER OF CONNECTIONS: " + connections );
			
			getFolderContent(connections);	
			
			oos.writeUTF("OK, HERE IS INFO ABOUT WHAT I HAVE");
			System.out.println("server sends: OK, HERE IS INFO ABOUT WHAT I HAVE");
			oos.writeInt(numberOfFiles);
			System.out.println("number of files:" + numberOfFiles);
			oos.writeLong(totalFolderSize);
			System.out.println("total folder size:" + totalFolderSize + " B");
			oos.writeUTF("END OF INFO, GONNA SEND FILES NOW");
			System.out.println("END OF INFO, GONNA SEND FILES NOW");
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}


