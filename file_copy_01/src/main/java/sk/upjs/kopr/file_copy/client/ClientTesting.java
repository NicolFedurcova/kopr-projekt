package sk.upjs.kopr.file_copy.client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import sk.upjs.kopr.file_copy.server.Searcher;
import sk.upjs.kopr.file_copy.server.Server;

public class ClientTesting {
	
	public static ConcurrentHashMap<String, Long> filesIHave;
	public static BlockingQueue<File> unnecessaryFilesToSend;
	public static final File TARGET_DESTINATION = new File("C:\\Users\\nicol\\Desktop\\test_copy");
	public static int connections;
	
	public static void main(String[] args) {
		try (Socket socket = new Socket ("localhost", Server.SERVER_PORT)) {
			connections = 3;
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			unnecessaryFilesToSend = new LinkedBlockingQueue<>();
			filesIHave= new ConcurrentHashMap<>();
			getFileInfo();
			
			if (filesIHave.isEmpty()) {
				oos.writeUTF("I WANT IT ALL");
				oos.writeInt(connections);
				System.out.println("client sends: I WANT IT ALL, number of connections: " + connections);
			} else {
				oos.writeUTF("I ALREADY HAVE SOMETHING");
				oos.writeObject(filesIHave);
				oos.writeInt(connections);
				System.out.println("client sends: I ALREADY HAVE SOMETHING: " + filesIHave.toString() + "\n with number of connections: " + connections);
			}
			oos.flush();
			
			String message = ois.readUTF();
			if(message.equals("OK, HERE IS INFO ABOUT WHAT I HAVE")) {
				System.out.println("client gets: " + message);
				int numberOfFiles = ois.readInt();
				System.out.println("clint gets number of files: " + numberOfFiles);
				long totalSizeOfFiles = ois.readLong();
				System.out.println("client gets size of files: " + totalSizeOfFiles + " B");
				String message2 = ois.readUTF();
				System.out.println("client gets message: " + message2);
				
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void getFileInfo(){
		Searcher searcher = new Searcher(TARGET_DESTINATION, filesIHave);
		searcher.call();
		
	}
}

