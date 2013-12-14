/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses/>
 *
 */
package de.nomagic.printerController.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.printerController.Cfg;
import de.nomagic.printerController.CloseApplication;
import de.nomagic.printerController.ControllerMain;
import de.nomagic.printerController.Tool;
import de.nomagic.printerController.core.CoreStateMachine;
import de.nomagic.printerController.core.Executor;

/** Main Window of GUI.
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class MainWindow extends JFrame implements ActionListener
{
    public static final String ACTION_CFG_LOAD = "cfg_load";
    public static final String ACTION_CFG_SAVE = "cfg_save";
    public static final String ACTION_CLIENT_CONNECT = "client_connect";
    public static final String ACTION_CLIENT_DISCONNECT = "client_disconnect";
    public static final String ACTION_CLIENT_ADD = "client_add";
    public static final String ACTION_VIEW_STANDARD = "view_standard";
    public static final String ACTION_VIEW_EXPERT = "view_expert";
    public static final String ACTION_VIEW_DEVELOPER = "view_developer";
    public static final String ACTION_PRINT = "print";
    public static final int VIEW_MODE_STANDARD = 0;
    public static final int VIEW_MODE_EXPERT = 1;
    public static final int VIEW_MODE_DEVELOPER = 2;
    public static final String CFG_SETTING_VIEW_MODE = "view Mode";
    public static final String CFG_VIEW_MODE_STANDARD = "standard";
    public static final String CFG_VIEW_MODE_EXPERT = "expert";
    public static final String CFG_VIEW_MODE_DEVELOPER = "developer";

    private static final long serialVersionUID = 1L;

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final PrinterStatusPanel printerStatusPanel;
    private final MachineControlPanel machineControlPanel;
    private final JMenuBar menuBar = new  JMenuBar();
    private JMenuItem addClient;
    private Cfg cfg;
    private CoreStateMachine core;

    public MainWindow(final Cfg cfg, final CoreStateMachine core, final CloseApplication Closer)
    {
        this.cfg = cfg;
        this.core = core;
        Executor exe = null;
        if(null != core)
        {
             exe = core.getExecutor();
        }
        // set up the window
        this.setTitle("Pacemaker - printerController");
        this.setResizable(true);
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e)
            {
                try
                {
                    printerStatusPanel.close();
                    machineControlPanel.close();
                    Closer.close();
                    if(null != cfg)
                    {
                        FileOutputStream fout;
                        fout = new FileOutputStream(ControllerMain.DEFAULT_CONFIGURATION_FILE_NAME);
                        cfg.saveTo(fout);
                    }
                }
                catch(Exception e1)
                {
                    log.error(Tool.fromExceptionToString(e1));
                }
                System.exit(0);
            }

        });
        // add all sub Panes
        // Printer Status Panel (cur extruder, cur Temperature, cur Position of print head,....)
        printerStatusPanel = new PrinterStatusPanel(exe);
        // Machine Control Panel
        machineControlPanel = new MachineControlPanel(core, cfg, printerStatusPanel, this, this);

        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                                    machineControlPanel.getPanel(),
                                                    printerStatusPanel.getPanel());
        this.add(splitPane);
        // End of Panels

        // Menues
        createMenueBar();
        this.setJMenuBar(menuBar);

        // Set view Mode
        setViewMode();

        this.pack();
        this.setVisible(true);
    }

    private void createMenueBar()
    {
        final JMenu cfgMenu = new JMenu("Config");
        cfgMenu.setMnemonic(KeyEvent.VK_C);
        JMenuItem menuItem = new JMenuItem("Load Configuration", KeyEvent.VK_L);
        menuItem.addActionListener(this);
        menuItem.setActionCommand(ACTION_CFG_LOAD);
        cfgMenu.add(menuItem);
        menuItem = new JMenuItem("Save Configuration", KeyEvent.VK_S);
        menuItem.addActionListener(this);
        menuItem.setActionCommand(ACTION_CFG_SAVE);
        cfgMenu.add(menuItem);
        menuBar.add(cfgMenu);

        final JMenu clientMenu = new JMenu("Client");
        clientMenu.setMnemonic(KeyEvent.VK_L);
        menuItem = new JMenuItem("Connect", KeyEvent.VK_C);
        menuItem.addActionListener(this);
        menuItem.setActionCommand(ACTION_CLIENT_CONNECT);
        clientMenu.add(menuItem);
        menuItem = new JMenuItem("Disconnect", KeyEvent.VK_D);
        menuItem.addActionListener(this);
        menuItem.setActionCommand(ACTION_CLIENT_DISCONNECT);
        clientMenu.add(menuItem);
        addClient = new JMenuItem("add new Client", KeyEvent.VK_A);
        addClient.addActionListener(this);
        addClient.setActionCommand(ACTION_CLIENT_ADD);
        clientMenu.add(addClient);
        menuBar.add(clientMenu);

        final JMenu viewMenu = new JMenu("View");
        cfgMenu.setMnemonic(KeyEvent.VK_V);
        final ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem("Standard");
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_S);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        rbMenuItem.setActionCommand(ACTION_VIEW_STANDARD);
        viewMenu.add(rbMenuItem);
        rbMenuItem = new JRadioButtonMenuItem("Expert");
        rbMenuItem.setMnemonic(KeyEvent.VK_E);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        rbMenuItem.setActionCommand(ACTION_VIEW_EXPERT);
        viewMenu.add(rbMenuItem);
        rbMenuItem = new JRadioButtonMenuItem("Developer");
        rbMenuItem.setMnemonic(KeyEvent.VK_D);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        rbMenuItem.setActionCommand(ACTION_VIEW_DEVELOPER);
        viewMenu.add(rbMenuItem);

        menuBar.add(viewMenu);
    }

    private void setViewMode()
    {
        int mode = VIEW_MODE_STANDARD;
        if(null == cfg)
        {
            // Standard mode
        }
        else
        {
            final String cfgMode = cfg.getGeneralSetting(CFG_SETTING_VIEW_MODE, CFG_VIEW_MODE_STANDARD);
            if(CFG_VIEW_MODE_STANDARD.equals(cfgMode))
            {
                mode = VIEW_MODE_STANDARD;
            }
            else if(CFG_VIEW_MODE_EXPERT.equals(cfgMode))
            {
                mode = VIEW_MODE_EXPERT;
            }
            else if(CFG_VIEW_MODE_DEVELOPER.equals(cfgMode))
            {
                mode = VIEW_MODE_DEVELOPER;
            }
            else
            {
                log.warn("Invalid Mode {} configured !", cfgMode);
            }
        }
        setViewMode(mode);
    }

    private void setViewMode(int mode)
    {
        printerStatusPanel.setViewMode(mode);
        machineControlPanel.setViewMode(mode);
        switch(mode)
        {
        case VIEW_MODE_EXPERT:
            addClient.setVisible(true);
            break;
        case VIEW_MODE_DEVELOPER:
            addClient.setVisible(true);
            break;
        case VIEW_MODE_STANDARD:
        default:
            addClient.setVisible(false);
            break;
        }
    }

    private void handleActionLoadConfiguration()
    {
        log.trace("User wants to load a configuration from a file,..");
        final JFileChooser fc = new JFileChooser();
        if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            final File file = fc.getSelectedFile();
            final Cfg c = new Cfg();
            try
            {
                if(true == c.readFrom(new FileInputStream(file)))
                {
                    log.trace("using the newly read configuration !");
                    core.close();
                    cfg = c;
                    core = new CoreStateMachine(cfg);
                    final ClientPanel cp = machineControlPanel.getClientPanel();
                    printerStatusPanel.updateExecutor(core.getExecutor());
                    machineControlPanel.updateCore(core);
                    machineControlPanel.updateCfg(cfg);
                    cp.updateConnectionDefinition("");
                    setViewMode();
                }
            }
            catch (FileNotFoundException e1)
            {
                e1.printStackTrace();
            }
        }
    }

    private void handleActionSaveConfiguration()
    {
        log.trace("User wants to save the configuration to a file,..");
        final JFileChooser fc = new JFileChooser();
        if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            final File file = fc.getSelectedFile();
            try
            {
                final FileOutputStream fout = new FileOutputStream(file);
                cfg.saveTo(fout);
            }
            catch(FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        final String command = e.getActionCommand();
        log.trace(command);
        if(ACTION_CFG_LOAD.equals(command))
        {
            handleActionLoadConfiguration();
        }
        else if(ACTION_CFG_SAVE.equals(command))
        {
            handleActionSaveConfiguration();
        }
        else if(ACTION_CLIENT_CONNECT.equals(command))
        {
            machineControlPanel.handleActionOpenClient();
        }
        else if(ACTION_CLIENT_DISCONNECT.equals(command))
        {
            machineControlPanel.handleActionCloseClient();
        }
        else if(ACTION_CLIENT_ADD.equals(command))
        {
            @SuppressWarnings("unused")
            final ConnectionDescriptionDialog d = new ConnectionDescriptionDialog(this,
                                                                                  machineControlPanel.getClientPanel(),
                                                                                  this);
        }
        else if(ACTION_VIEW_STANDARD.equals(command))
        {
            setViewMode(VIEW_MODE_STANDARD);
        }
        else if(ACTION_VIEW_EXPERT.equals(command))
        {
            setViewMode(VIEW_MODE_EXPERT);
        }
        else if(ACTION_VIEW_DEVELOPER.equals(command))
        {
            setViewMode(VIEW_MODE_DEVELOPER);
        }
        else if(ACTION_PRINT.equals(command))
        {
            machineControlPanel.handleActionPrint();
        }
        else
        {
            log.warn("Action handler missing for Event : {}", command);
        }
    }

}
