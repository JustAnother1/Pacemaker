package de.nomagic.printerController.createConfigFx;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AddClientWindowController 
{
	private final Stage parentStage;

	public AddClientWindowController(Stage parentStage) 
	{
		this.parentStage = parentStage;
	}
	
	public void createAndShowWindow()
	{
    	BorderPane AddClientWindowScene;
    	
        Stage secondStage = new Stage();
        secondStage.initStyle(StageStyle.DECORATED);
        secondStage.initModality(Modality.NONE);
        secondStage.initOwner(parentStage);
        parentStage.toFront();
        secondStage.show();
        
        // Create GUI
        secondStage.setTitle("Add client connection");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("AddClientWindow.fxml"));
        loader.setController(this);
        try
        {
            AddClientWindowScene = (BorderPane) loader.load();
            // Show the scene containing the root layout.
            Scene scene = new Scene(AddClientWindowScene);
            secondStage.setScene(scene);
            secondStage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
	}

}
