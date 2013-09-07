/**
 *
 */
package de.nomagic.printerController.printerConfigurationCreationWizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import de.nomagic.Translator.Translator;
import de.nomagic.WizardDialog.DataStore;
import de.nomagic.WizardDialog.WizardSlide;
import de.nomagic.printerController.Cfg;

/** Let the user select which Interface type to use to talk to the client.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class InterfaceTypeSelectionSlide implements WizardSlide, ActionListener
{
    private JPanel slide = new JPanel();
    private String SelectedInterface = null;
    private HashMap<String, WizardSlide> interfaces = new HashMap<String, WizardSlide>();

    public InterfaceTypeSelectionSlide(Translator t)
    {
        slide.setLayout(new BoxLayout(slide, BoxLayout.PAGE_AXIS));
        JTextArea pleaseChoose = new JTextArea();
        pleaseChoose.setText(t.t("Interface_Selection_ChooseOne"));
        pleaseChoose.setEditable(false);
        pleaseChoose.setAlignmentX(Component.LEFT_ALIGNMENT);
        slide.add(pleaseChoose);
    }

    @Override
    public String getName()
    {
        return "Interface Selection";
    }

    @Override
    public Component getComponent()
    {
        Iterator<String> it = interfaces.keySet().iterator();
        boolean first = true;
        ButtonGroup group = new ButtonGroup();
        while(it.hasNext())
        {
            String name = it.next();
            JRadioButton aChoice = new JRadioButton(name);
            aChoice.setActionCommand(name);
            aChoice.addActionListener(this);
            if(true == first)
            {
                aChoice.setSelected(true);
                SelectedInterface = name;
                first = false;
            }
            group.add(aChoice);
            aChoice.setAlignmentX(Component.LEFT_ALIGNMENT);
            slide.add(aChoice);
        }
        return slide;
    }

    @Override
    public WizardSlide getNextSlide()
    {
        return interfaces.get(SelectedInterface);
    }

    @Override
    public boolean hasNextSlide()
    {
        return true;
    }

    @Override
    public DataStore actionOnShow(DataStore ds)
    {
        return ds;
    }

    @Override
    public DataStore actionOnClose(DataStore ds)
    {
        Object obj = ds.getObject(WizardMain.DS_CONFIGURATION_NAME);
        if(null == obj)
        {
            Cfg c = new Cfg();
            ds.putObject(WizardMain.DS_CONFIGURATION_NAME, c);
        }
        return ds;
    }

    public void addInterfaceTypeSlide(String InterfaceName, WizardSlide interfaceSlide)
    {
        interfaces.put(InterfaceName, interfaceSlide);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        SelectedInterface = e.getActionCommand();
    }

}
