package de.nomagic.printerController.createConfigFx;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

public class ConfigurationController
{

	public ConfigurationController()
	{
		// TODO Auto-generated constructor stub
	}

	public Pane getPane()
	{
		Pane res = null;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("Configuration.fxml"));
        loader.setController(this);
        try
        {
            res = (Pane) loader.load();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
		return res;
	}

}
