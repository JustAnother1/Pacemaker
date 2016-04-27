package de.nomagic.printerController.createConfigFx;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

import de.nomagic.printerController.pacemaker.ClientConnection;
import de.nomagic.printerController.pacemaker.TcpClientConnection;
import de.nomagic.printerController.pacemaker.UartClientConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import purejavacomm.CommPortIdentifier;

public class AddClientWindowController implements Initializable
{
	private final Stage parentStage;
	private Stage secondStage;
	private ObservableList<String> serialPorts;

	@FXML
	private ComboBox<String> serial_portComboBox;
	@FXML
	private TextField tcp_hostTextField;
	@FXML
	private TextField tcp_portTextField;
	@FXML
	private Button cancel_button;
	@FXML
	private Button add_button;
	@FXML
	private TabPane connectionTypeTabPane;

	private final MainWindowController mainWindowController;

	public AddClientWindowController(Stage parentStage, MainWindowController mainWindowController)
	{
		this.parentStage = parentStage;
		this.mainWindowController = mainWindowController;
	}

	@Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
		// detect available serial ports
        final Properties systemProperties = System.getProperties();
        systemProperties.setProperty("jna.nosys", "true");
        ObservableList<String> list = serial_portComboBox.getItems();
        Enumeration e = CommPortIdentifier.getPortIdentifiers();
        while(true == e.hasMoreElements())
        {
        	CommPortIdentifier id = (CommPortIdentifier)e.nextElement();
        	String port = id.getName();
        	list.add(port);
        	System.out.println(port);
        }
    }

	@FXML
	private void handleAddConnection(ActionEvent event)
	{
		System.out.println("add client pressed");
		SingleSelectionModel<Tab> selectionModel = connectionTypeTabPane.getSelectionModel();
		Tab selectedTab = selectionModel.getSelectedItem();
		String TabName = selectedTab.getText();
		ClientConnection connect = null;
		if("Serial".equals(TabName))
		{
			connect = new UartClientConnection(serial_portComboBox.getValue() + ":115200:8:None:1:false:false:false:false");
		}
		else if("TCP".equals(TabName))
		{
			connect = TcpClientConnection.establishConnectionTo(tcp_hostTextField.getText() + ":" + tcp_portTextField.getText());
		}
		// create connection from Data
		if(null != connect)
		{
			// Give connection to main Window
			mainWindowController.addClient(connect);
			// close this window
			secondStage.close();
		}
	}

	@FXML
	private void handleCancelAction(ActionEvent event)
	{
		System.out.println("cancel pressed");
	    secondStage.close();
	}

	public void createAndShowWindow()
	{
    	BorderPane AddClientWindowScene;

        secondStage = new Stage();
        secondStage.initStyle(StageStyle.DECORATED);
        secondStage.initModality(Modality.NONE);
        secondStage.initOwner(parentStage);
        parentStage.toFront();
        // secondStage.show();

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
