package de.nomagic.printerController.createConfigFx;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import de.nomagic.printerController.pacemaker.ClientConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainWindowController implements Initializable
{
	private final Stage parentStage;
    private BorderPane MainWindowScene;
	private TreeItem<String> root = new TreeItem<String>("Clients");
	private ClientConnection clientChannel = null;
    @FXML
    private TreeView<String> clientTreeView;
    @FXML
    private CheckBox onlineCheckBox;

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

	@Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
		root.setExpanded(true);
		clientTreeView.setRoot(root);
		clientTreeView.setShowRoot(false);
    }

	public void addClient(ClientConnection aClient)
	{
		if(null != aClient)
		{
			clientChannel = aClient;
			TreeItem<String> clientItem = new TreeItem<String>(clientChannel.getName());
			root.getChildren().add(clientItem);
			// connection is now online
			onlineCheckBox.setSelected(true);
			onlineCheckBox.setDisable(false);
		}
	}

    @FXML
    private void handleOnlineCheckBoxClick(ActionEvent event)
    {
    	// TODO
    }

    @FXML
    private void handleAddClientButton(ActionEvent event)
    {
    	System.out.println("Clicked add client");
    	AddClientWindowController ctrl = new AddClientWindowController(parentStage, this);
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
