package sk.upjs.kopr.file_copy.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import sk.upjs.kopr.file_copy.MainAppController;
import sk.upjs.kopr.file_copy.client.ClientService;

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
				oos.writeUTF(filePath); 
				oos.writeLong(size); 
				oos.writeLong(offset); 
				
				oos.flush();
				sendData(oos);
				fileBeingSent = filesToSend.take();
				
			}
			oos.writeUTF(Searcher.POISON_PILL.getName()); 
			
		} catch (SocketException e1) {
			//System.err.println("Uz ma nepocuva klient");
			//e1.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println("INTERRUPTED EXCEPTION");
			e.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		
		}finally {
			try {
				if (oos != null) oos.close();
				if (dataSocket != null && dataSocket.isConnected()) dataSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sendData(ObjectOutputStream oos) throws IOException {
		try (RandomAccessFile raf = new RandomAccessFile(fileBeingSent, "r")) {
			byte[] data = new byte[BLOCK_SIZE];
			raf.seek(offset);
			
			while (offset < size) {
				
				if (size - offset < BLOCK_SIZE) {
					data = new byte[(int) (size - offset)];
				} else {
					data = new byte[BLOCK_SIZE];
				}
				long prev = offset;
				offset += raf.read(data); 
				
				oos.write(data);
				oos.flush();
			}
			   
		} catch (SocketException e5) {
			throw e5;
		}
	}
	
	private String getTargetDestination(File file) {
		return 	MainAppController.TARGET_DESTINATION+file.getAbsolutePath().substring(Server.FOLDER_TO_SHARE.getAbsolutePath().length());
		
	}
	
	
}
