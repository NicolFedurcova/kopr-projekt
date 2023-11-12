package sk.upjs.kopr.file_copy.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class generatorcek {
	
	public static void main(String[] args) {
		createTestFile("patky", 5);
	}
	
	public static File createTestFile(String name, int base) {
		File file = new File("C:\\Users\\nicol\\Desktop\\" +name+".txt");
		FileWriter fw;
		try {
			fw = new FileWriter(file.getAbsoluteFile());
			try (BufferedWriter bw = new BufferedWriter(fw)) {
				for (int i=0; i<262144000; i++) {
					bw.write(base);
				}
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
		return file;
	}

}
