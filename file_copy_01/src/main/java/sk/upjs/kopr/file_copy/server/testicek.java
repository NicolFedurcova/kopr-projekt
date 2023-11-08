package sk.upjs.kopr.file_copy.server;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class testicek {
	private static final int BLOCK_SIZE = 1; //1 byte = 8 bit
	//private static String dataicka = "1234"; //1 cilo = 1 bajt = 4 bajty = 32 bitov
	private static File daticka = new File("C:\\Users\\nicol\\Desktop\\test\\priecinok1\\subor1.txt");
	private static RandomAccessFile raf;
	private static byte[] data = new byte[BLOCK_SIZE];
	private static long offset = 0;

	
	public static void main(String[] args) throws IOException {
		raf = new RandomAccessFile(daticka, "r");
		System.out.println("DATA NA ZACIATKU:" + Arrays.toString(data));
		System.out.println("VELKOST DATCIEK:" + daticka.length());
		raf.seek(offset);
		
		for(int i=0; i<daticka.length(); i+=BLOCK_SIZE) {//0, 10, 20, 30, 40, 50, ... , 90
			int size = (int) Math.min(BLOCK_SIZE, daticka.length() - i); //10, 91-90
			raf.read(data, 0, size);
			System.out.println(Integer.toBinaryString(data[0]));
			System.out.println("DATA PO " + i+ "tej iteracii: " +Arrays.toString(data) + "pocet prvkov: " + data.length);
		}
		
		/*
		while (offset < daticka.length()) {
			
			if (daticka.length() - offset < BLOCK_SIZE) {
				data = new byte[(int) (daticka.length() - offset)];
			} else {
				data = new byte[BLOCK_SIZE];
			}
			}
		*/
			
			
			offset += raf.read(data);
			System.out.println();
			System.out.println(Integer.toBinaryString(data[0]));
			System.out.println("DATA PO tej iteracii: " +Arrays.toString(data) + "pocet prvkov: " + data.length);
			
			
		
				
	}
		
		

}
