package de.nomagic.printerController.createConfigFx;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import de.nomagic.printerController.pacemaker.ClientConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainWindowController implements Initializable, ChangeListener<TreeItem<String>>
{
	private final Stage parentStage;
    private BorderPane MainWindowScene;
	private TreeItem<String> root = new TreeItem<String>("Clients");
	private ClientConnection clientChannel = null;
	private final String ConfigurationTab = "Configuration";
	private boolean isOnline = false;
    @FXML
    private TreeView<String> clientTreeView;
    @FXML
    private CheckBox onlineCheckBox;
    @FXML
    private ScrollPane SettingsScrollPane;

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
	public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldValue,TreeItem<String> newValue)
    {
        TreeItem<String> selectedItem = newValue;
        if(true == selectedItem.isLeaf())
        {
        	String selectedString = selectedItem.getValue();
        	System.out.println("Selected Text : " + selectedString);
        	if(ConfigurationTab == selectedString)
        	{
        		ConfigurationController cfgControl = new ConfigurationController();
        		Pane cfgPane = cfgControl.getPane();
        		SettingsScrollPane.setContent(cfgPane);
        	}
        }
    }

	@Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
		root.setExpanded(true);
		clientTreeView.setRoot(root);
		clientTreeView.setShowRoot(false);
		clientTreeView.getSelectionModel().selectedItemProperty().addListener(this);
    }

	public void addClient(ClientConnection aClient)
	{
		if(null != aClient)
		{
			clientChannel = aClient;
			TreeItem<String> clientItem = new TreeItem<String>(clientChannel.getConnectionName());
			ObservableList<TreeItem<String>> capabilities = clientItem.getChildren();
			TreeItem<String> ClientConfiguration = new TreeItem<String>(ConfigurationTab);
			capabilities.add(ClientConfiguration);
			root.getChildren().add(clientItem);
			onlineCheckBox.setDisable(false);
		}
	}

    @FXML
    private void handleOnlineCheckBoxClick(ActionEvent event)
    {
    	if(false == isOnline)
    	{
			if(false == clientChannel.connect())
			{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Failed to connect");
				alert.setHeaderText(null);
				alert.setContentText("Could not open the connection to the client! ");
				alert.showAndWait();
				onlineCheckBox.setSelected(false);
			}
			else
			{
				// connection is now online
				onlineCheckBox.setSelected(true);
				isOnline = true;
			}
    	}
    	else
    	{
    		if(null != clientChannel)
    		{
    			clientChannel.disconnect();
    		}
    		onlineCheckBox.setSelected(false);
    		isOnline = false;
    	}
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
