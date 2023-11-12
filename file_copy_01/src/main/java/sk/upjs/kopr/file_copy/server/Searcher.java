package sk.upjs.kopr.file_copy.server;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import sk.upjs.kopr.file_copy.client.ClientTesting;

public class Searcher implements Callable<Long[]>{
	
	public static final File POISON_PILL = new File("poison.pill");
	private File rootDir;
	private BlockingQueue<File> filesToSend; 
	private long sizeOfFiles;
	private long numberOfFiles;
	private ConcurrentHashMap<String, Long> clientFileInfos;
	
	
	
	public Searcher(File rootDir, BlockingQueue<File> filesToSend) {		
		this.rootDir = rootDir;
		this.filesToSend = filesToSend;
		this.clientFileInfos = null;
	}
	
	public Searcher(File rootDir, ConcurrentHashMap<String, Long> fileInfos) {		
		this.rootDir = rootDir;
		this.filesToSend = null;
		this.clientFileInfos = fileInfos;
	}
	
	
	@Override
	public Long[] call() {
		Long [] result = search(rootDir);
		
		if(filesToSend!=null) {
			for(int i = 0; i<ClientTesting.CONNECTIONS; i++) {
				filesToSend.offer(POISON_PILL); //tolko poison pillov kolko sendtaskov kolko vlakien
			}
		}
		
		return result;
	}
	
	private Long[] search(File directory) {
		
		File[] files = directory.listFiles();
	
		for (int i = 0; i<files.length; i++ ) {
			if(files[i].isFile()) {
				if(filesToSend!=null) {
					filesToSend.offer(files[i]);
					//System.out.println("SERAHCER naplnil files to send");
				}
				if (clientFileInfos!=null) {
					clientFileInfos.put(files[i].getAbsolutePath(), files[i].length());//SERVERU POSIELAM MAPU S ABSOLUTNYMI CESTAMI U KLIENTA!!
					//System.out.println("SERAHCER naplnil client file infos");
				}
				sizeOfFiles+=files[i].length();
				numberOfFiles++;
			}
			if(files[i].isDirectory()) {
				search(files[i]);
			}
		}
		
					
		Long[] result = {sizeOfFiles, numberOfFiles};
		return result;
	}

	
	
	
	
}
