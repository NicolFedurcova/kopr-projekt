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

import sk.upjs.kopr.file_copy.MainAppController;
import sk.upjs.kopr.file_copy.server.FileSendTask;
import sk.upjs.kopr.file_copy.server.Searcher;

public class FileSaveTask implements Runnable{
	private Socket dataSocket;
	private int order;
	private CountDownLatch gate;
	private ObjectInputStream ois;
	
	public FileSaveTask(Socket dataSocket,int order, CountDownLatch gate) {
		this.dataSocket = dataSocket;
		this.order = order;
		this.gate = gate;
		
	}

	@Override
	public void run() {
		try {
			
			ois = new ObjectInputStream(dataSocket.getInputStream());
			while(true) { ///kym nepride poison pill
				/////////////////////////////////////////////////////////////////////////////////////
				//IDK CI TOT OTU TAK MOZE BYT
				if(Thread.currentThread().isInterrupted()) {
					/*
					System.out.println(Thread.currentThread().getName() +"SA GATE ZNIZIL file save task run");
					gate.countDown();
					if (ois != null) ois.close();
					if (dataSocket != null && dataSocket.isConnected()) dataSocket.close();
					*/
					System.out.println(Thread.currentThread().getName() + "  SOM ZISTIL PRERUSENIE V RUNe");
					return;
				}
				////////////////////////////////////////////////////////////////////////////////////
				String filePath = ois.readUTF();
				if(filePath.equals(Searcher.POISON_PILL.getName())) {
					System.out.println(Thread.currentThread().getName() + "  TU JE POISON PILL");
					break;
					
				}
				File fileBeingReceived = new File(filePath);
				System.out.println(order + " file being received: " + filePath + " by thread: " + Thread.currentThread().getName());
				File path = fileBeingReceived.getParentFile();
				path.mkdirs();
				
				long size = ois.readLong(); //celkova velkost filu ktory prichadza
	            long offset = ois.readLong();	//uvodny offset			
				
				receiveData(ois, fileBeingReceived, size, offset);
				System.out.println(Thread.currentThread() + "   SKONCIL SOM");
				ClientTesting.pfs.incrementFileNumberProgress();
								
			}
						
		} catch (SocketException e1){
			System.err.println("SERVER BOL ZASTAVENY");
			//MainAppController.customErrorAlert("Server bol zastavený...", "Server bol zastavený počas sťahovania, skúste znova neskôr...");
		}catch (IOException e) {
			System.err.println("IO EXCEPTION");
			//e.printStackTrace();
		} finally {
			gate.countDown();
			System.out.println(Thread.currentThread().getName() + "  SA GATE ZNIZIL VO FINALLY");
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
			//System.out.println(order  +" sa posuva na offset: " + offset + " z celkoveho size: " + size);
			
			
			int readLength = 0;
			while (offset < size) {
				//raf.seek(offset);
				/////////////////////////////////////////////////////////////////////////////////////
				//IDK CI TOT OTU TAK MOZE BYT
				if(Thread.currentThread().isInterrupted()) {
					/*
					System.out.println(Thread.currentThread().getName() + "SA GATE ZNIZIL fise save task run ---> receive data ");
					gate.countDown();
					if (raf!=null) raf.close();
					if (ois != null) ois.close();
					if (dataSocket != null && dataSocket.isConnected()) dataSocket.close();
					*/
					System.out.println(Thread.currentThread().getName() + "  SOM ZISTIL PRERUSENIE V Rceive data ");
					if (raf!=null) raf.close();
					return;
				}
				////////////////////////////////////////////////////////////////////////////////////
				if(size-offset < FileSendTask.BLOCK_SIZE) {
					data = new byte[(int)(size-offset)];
					//readLength = ois.read(data, 0, (int)(size-offset)); 
					readLength = ois.read(data); 
				} else {
					//readLength = ois.read(data, 0, FileSendTask.BLOCK_SIZE);
					readLength = ois.read(data); 
				}
				//System.out.println(order +" TOTO EJ POLE BAJTOV KTORE SA PRIJIMA: " + Arrays.toString(data));
				//progressSize.addAndGet(readLength);
				ClientTesting.pss.incrementFileSizeProgress(readLength);
				//raf.seek(offset);
				raf.write(data,0,readLength);
				//raf.write(data);
				//System.out.println(order + " OFFSET SA ZMENIL Z " + offset + " na " + (offset+readLength) );
				offset +=readLength;
				
				data = new byte[FileSendTask.BLOCK_SIZE];
								
			}
			raf.close();
            
		} catch (SocketException e1){
			//System.err.println("SERVER BOL ZASTAVENÝ");
			throw e1;
		} catch (FileNotFoundException e3) {
			e3.printStackTrace();
		} catch (IOException e4) {
			e4.printStackTrace();
		} 
		
	}
	


}
