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
package de.nomagic.test.pacemaker;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 */
public class StatusWindow extends JFrame implements Hardware
{
    private static final long serialVersionUID = 1L;
    private final JPanel StatusPanel = new JPanel();
    private final JTextArea curStatus = new JTextArea("not available");

    public StatusWindow()
    {
        this.setTitle("Pacemaker Status");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        StatusPanel.add(curStatus);
        this.add(StatusPanel);
        this.pack();
        this.setVisible(true);
    }

    @Override
    public int getNumberSteppers()
    {
        return 5;
    }

    @Override
    public int getNumberHeaters()
    {
        return 3;
    }

    @Override
    public int getNumberPwm()
    {
        return 1;
    }

    @Override
    public int getNumberTempSensor()
    {
        return 4;
    }

    @Override
    public int getNumberInput()
    {
        return 10;
    }

    @Override
    public int getNumberOutput()
    {
        return 13;
    }

    @Override
    public int getHardwareType()
    {
        return 0;
    }

    @Override
    public int getHardwareRevision()
    {
        return 0;
    }

    @Override
    public String getFirmwareNameString()
    {
        return "Simulated Pacemaker";
    }

    @Override
    public int[] getListOfSupportedProtocolExtensions()
    {
        final int[] res = {0, 1, 2};
        return res;
    }

    @Override
    public int getFirmwareType()
    {
        return 0;
    }

    @Override
    public String getSerialNumberString()
    {
        return "0815-4711-007";
    }

    @Override
    public String getBoardNameString()
    {
        return "Pacemaker Simulator";
    }

    @Override
    public String getGivenNameString()
    {
        return "SimOne";
    }

    @Override
    public byte getProtocolVersionMajor()
    {
        return 1;
    }

    @Override
    public byte getProtocolVersionMinor()
    {
        return 0;
    }

    @Override
    public int getFirmwareRevisionMajor()
    {
        return 7;
    }

    @Override
    public int getFirmwareRevisionMinor()
    {
        return 3;
    }

    @Override
    public int getNumberBuzzer()
    {
        return 2;
    }

}
