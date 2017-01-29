package de.nomagic.printerController.createConfigFx;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application
{

	@Override
	public void start(Stage primaryStage)
	{
		System.out.println("Starting Configuration Creator in version : " + getCommitID());
        // Create GUI
        primaryStage.setTitle("Pacemaker Configuration creator");
        MainWindowController ctrl = new MainWindowController(primaryStage);
        ctrl.createAndShowWindow();
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
