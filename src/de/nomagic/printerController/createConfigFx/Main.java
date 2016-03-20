package de.nomagic.printerController.createConfigFx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application
{
    private BorderPane MainWindowScene;

	@Override
	public void start(Stage primaryStage)
	{
		System.out.println("Starting Configuration Creator in version : " + getCommitID());

        // Create GUI
        primaryStage.setTitle("Pacemaker Configuration creator");
        MainWindowController ctrl = new MainWindowController(primaryStage);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("MainWindow.fxml"));
        loader.setController(ctrl);
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
        primaryStage.setScene(scene);
        primaryStage.show();
	}

    private String getCommitID()
    {
        try
        {
            final InputStream s = getClass().getResourceAsStream("/commit-id");
            final BufferedReader in = new BufferedReader(new InputStreamReader(s));
            final String commitId = in.readLine();
            final String changes = in.readLine();
            if(null != changes)
            {
                if(0 < changes.length())
                {
                    return commitId + "-(" + changes + ")";
                }
                else
                {
                    return commitId;
                }
            }
            else
            {
                return commitId;
            }
        }
        catch( Exception e )
        {
            return "could not read version: " + e.toString();
        }
    }

	public static void main(String[] args)
	{
		launch(args);
	}
}
