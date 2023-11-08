package sk.upjs.kopr.file_copy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


public class MainApp extends Application  {
	
	@Override
	public void start(Stage stage) throws Exception {
		System.out.println("class: "+getClass());
		System.out.println("TOTO TU JE PROBLEM:" + getClass().getResource("MainApp.fxml")+"");
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainApp.fxml"));
		
		MainAppController controller = new MainAppController();
		fxmlLoader.setController(controller);
		
		
		Parent parent = fxmlLoader.load();
		Scene scene = new Scene(parent);
		
		String css = this.getClass().getResource("file_copy.css").toExternalForm();
		scene.getStylesheets().add(css);
		String cesta = this.getClass().getResource("logo.png")+"";
		Image icon = new Image(cesta);
		stage.getIcons().add(icon);
		
		
		stage.setScene(scene);
		stage.setTitle("Kopirovac priecinku");
		stage.show();
				
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
