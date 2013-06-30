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
package de.nomagic.printerController.printerConfigurationCreationWizard;

import java.awt.Component;

import javax.swing.JPanel;

import de.nomagic.Translator.Translator;
import de.nomagic.WizardDialog.DataStore;
import de.nomagic.WizardDialog.OneNextWizardSlide;

/** Ask user for parameters of UART Interface.
 *
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class InterfaceTypeUartSlide extends OneNextWizardSlide
{
    private JPanel slide = new JPanel();

    public InterfaceTypeUartSlide(Translator t)
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getName()
    {
        return "UartSlide";
    }

    @Override
    public Component getComponent()
    {
        // TODO Auto-generated method stub
        return slide;
    }

    @Override
    public DataStore actionOnShow(DataStore ds)
    {
        // TODO Auto-generated method stub
        return ds;
    }

    @Override
    public DataStore actionOnClose(DataStore ds)
    {
        // TODO Auto-generated method stub
        return ds;
    }

}
