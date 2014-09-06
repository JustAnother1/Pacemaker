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

import java.awt.Component;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import de.nomagic.printerController.core.ActionResponse;
import de.nomagic.printerController.core.Action_enum;
import de.nomagic.printerController.core.Event;
import de.nomagic.printerController.core.EventSource;
import de.nomagic.printerController.core.Executor;
import de.nomagic.printerController.core.TemperatureObserver;
import de.nomagic.printerController.core.TimeoutHandler;
import de.nomagic.printerController.Heater_enum;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class TemperaturePanel implements TemperatureObserver, EventSource
{
    public static final int MAXIMUM_ITEM_COUNT = 1000;
    public static final int MAXIMUM_TIME_BETWEEN_TEMPERATURE_MEASUREMENT_MS = 1000;

    private final JPanel myPanel;
    private final String chartTitle = "Temperature Chart";
    private final String xAxisLabel = "Time";
    private final String yAxisLabel = "Temperature";
    private XYSeriesCollection dataset = new XYSeriesCollection();
    private long startTime;
    private XYSeries[] data = new XYSeries[Heater_enum.size];
    private JFreeChart chart;
    private TimeoutHandler timeOut;
    private int[] timeOutId = new int[Heater_enum.size];
    private Executor exe;


    public TemperaturePanel(Executor exe)
    {
        updateExecutor(exe);
        chart = ChartFactory.createXYLineChart(chartTitle,
                                               xAxisLabel,
                                               yAxisLabel,
                                               dataset);
        myPanel = new ChartPanel(chart);
    }


    public void updateExecutor(Executor exe)
    {
        if(null == exe)
        {
            return;
        }
        this.exe = exe;
        exe.registerTemperatureObserver(this);
        timeOut = exe.getTimeoutHandler();
        for(Heater_enum ele : Heater_enum.values())
        {
            if(true == exe.istheHeaterConfigured(ele))
            {
                final Event e = new Event(Action_enum.timeOut, ele, this);
                timeOutId[ele.getValue()] = timeOut.createTimeout(e, MAXIMUM_TIME_BETWEEN_TEMPERATURE_MEASUREMENT_MS);
                timeOut.startTimeout(timeOutId[ele.getValue()]);
            }
            else
            {
                timeOutId[ele.getValue()] = TimeoutHandler.ERROR_FAILED_TO_CREATE_TIMEOUT;
            }
        }
    }

    public Component getPanel()
    {
        return myPanel;
    }

    public void setToOffline()
    {
        // Keep the data available
    }

    public void setToOnline()
    {
        // reset the graph - remove all old data
        dataset.removeAllSeries();
        for(int i = 0; i < Heater_enum.size; i++)
        {
            data[i] = null;
        }
        startTime = System.currentTimeMillis();
    }

    public void close()
    {

    }

    public void setViewMode(int mode)
    {
        switch(mode)
        {
        case MainWindow.VIEW_MODE_EXPERT:
            break;
        case MainWindow.VIEW_MODE_DEVELOPER:
            break;
        case MainWindow.VIEW_MODE_STANDARD:
        default:
            break;
        }
    }
    @Override
    public void update(Heater_enum position, double temperature)
    {
        // TODO Thread Sync problem ???
        XYSeries curSeries = data[position.ordinal()];
        if(null == curSeries)
        {
            // first Temperature Report on this Heater
            curSeries = new XYSeries(position.toString());
            curSeries.setMaximumItemCount(MAXIMUM_ITEM_COUNT);
            data[position.ordinal()] = curSeries;
            dataset.addSeries(curSeries);
        }
        // else nothing to do
        long curTime = System.currentTimeMillis();
        curTime = curTime - startTime;
        curSeries.add(curTime, temperature);
    }

    /** Timeout occurred -> we need to request that temperature reading.
     *
     */
    @Override
    public void reportEventStatus(ActionResponse response)
    {
        final Heater_enum pos = (Heater_enum)response.getObject();
        @SuppressWarnings("unused")
        final double curTemp = exe.requestTemperatureOfHeater(pos);
        // not necessary already happened by Observer update(pos, curTemp);
        timeOut.startTimeout(timeOutId[pos.getValue()]);
    }

}
