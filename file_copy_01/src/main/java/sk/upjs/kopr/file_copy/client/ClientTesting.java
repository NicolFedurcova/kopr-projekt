package sk.upjs.kopr.file_copy.client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.HandshakeCompletedEvent;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import sk.upjs.kopr.file_copy.server.FileSendTask;
import sk.upjs.kopr.file_copy.server.Searcher;
import sk.upjs.kopr.file_copy.server.Server;

public class ClientTesting extends Service<Boolean> {

	public static ConcurrentHashMap<String, Long> filesIHave;
	public static final File TARGET_DESTINATION = new File("C:\\Users\\nicol\\Desktop\\test_copy");
	public static int connections;
	private static int numberOfFiles;
	private static long totalSizeOfFiles;
	private AtomicInteger progressNumberOfFiles;
	private AtomicLong progressSize;
	

	public static void main(String[] args) {
		try (Socket socket = new Socket("localhost", Server.SERVER_PORT)) {
			connections = 3;
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

			filesIHave = new ConcurrentHashMap<>();
			getFileInfo();
			handshake(oos, ois, numberOfFiles, totalSizeOfFiles);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void getFileInfo() {
		Searcher searcher = new Searcher(TARGET_DESTINATION, filesIHave);
		searcher.call();

	}

	@Override
	protected Task<Boolean> createTask() {
		return new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				try (Socket communicationSocket = new Socket("localhost", Server.SERVER_PORT)) {

					connections = 3;
					ObjectOutputStream oos = new ObjectOutputStream(communicationSocket.getOutputStream());
					ObjectInputStream ois = new ObjectInputStream(communicationSocket.getInputStream());

					filesIHave = new ConcurrentHashMap<>();
					getFileInfo();
					handshake(oos, ois, numberOfFiles, totalSizeOfFiles);
					
					ExecutorService executor = Executors.newCachedThreadPool();
					//List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();
			        for (int i = 0; i < connections; i++) {
						Socket dataSocket = new Socket("localhost", Server.SERVER_PORT);
			        	FileSaveTask fileSaveTask = new FileSaveTask(dataSocket, progressNumberOfFiles,progressSize );
						executor.execute(fileSaveTask);
			        }
					communicationSocket.close();
					

				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}

		};
	}

	private static void handshake(ObjectOutputStream oos, ObjectInputStream ois, int numberOfFiles, long totalSizeOfFiles) {
		try {
			if (filesIHave.isEmpty()) {
				oos.writeUTF("I WANT IT ALL");
				oos.writeInt(connections);
				System.out.println("client sends: I WANT IT ALL, number of connections: " + connections);
			} else {
				oos.writeUTF("I ALREADY HAVE SOMETHING");
				oos.writeObject(filesIHave);
				oos.writeInt(connections);
				System.out.println("client sends: I ALREADY HAVE SOMETHING: " + filesIHave.toString()
						+ "\n with number of connections: " + connections);
			}
			oos.flush();

			String message = ois.readUTF();
			if (message.equals("OK, HERE IS INFO ABOUT WHAT I HAVE")) {
				System.out.println("client gets: " + message);
				numberOfFiles = ois.readInt();
				System.out.println("clint gets number of files: " + numberOfFiles);
				totalSizeOfFiles = ois.readLong();
				System.out.println("client gets size of files: " + totalSizeOfFiles + " B");
				String message2 = ois.readUTF();
				System.out.println("client gets message: " + message2);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
