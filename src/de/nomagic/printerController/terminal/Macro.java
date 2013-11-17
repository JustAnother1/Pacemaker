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
package de.nomagic.printerController.terminal;

import java.io.Serializable;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class Macro implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int Order;
    private int ParameterLength;
    private int[] parameter;
    private String Name;

    public Macro()
    {
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return Name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        Name = name;
    }

    /**
     * @return the order
     */
    public int getOrder()
    {
        return Order;
    }

    /**
     * @param order the order to set
     */
    public void setOrder(int order)
    {
        Order = order;
    }

    /**
     * @return the parameterLength
     */
    public int getParameterLength()
    {
        return ParameterLength;
    }

    /**
     * @param parameterLength the parameterLength to set
     */
    public void setParameterLength(int parameterLength)
    {
        ParameterLength = parameterLength;
    }

    /**
     * @return the parameter
     */
    public int[] getParameter()
    {
        return parameter;
    }

    /**
     * @param parameter the parameter to set
     */
    public void setParameter(int[] parameter)
    {
        this.parameter = parameter;
    }

}
