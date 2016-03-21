package de.nomagic.printerController.createConfigFx;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainWindowController
{
	private final Stage parentStage;
    private BorderPane MainWindowScene;
	
	public MainWindowController(Stage primaryStage)
	{
		parentStage = primaryStage;
	}
	
	public void createAndShowWindow()
	{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("MainWindow.fxml"));
        loader.setController(this);
        try
        {
            MainWindowScene = (BorderPane) loader.load();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        // Show the scene containing the root layout.
        Scene scene = new Scene(MainWindowScene);
        parentStage.setScene(scene);
        parentStage.show();
	}
	
    @FXML 
    private void handleAddClientButton(ActionEvent event) 
    {
    	System.out.println("Clicked add client");
    	AddClientWindowController ctrl = new AddClientWindowController(parentStage);
    	ctrl.createAndShowWindow();
    }
    
    @FXML 
    private void handleLoadConfigButton(ActionEvent event) 
    {
    	System.out.println("Clicked load Config");
    }
    
    @FXML 
    private void handleSaveConfigButton(ActionEvent event) 
    {
    	System.out.println("Clicked save config");
    }
}
