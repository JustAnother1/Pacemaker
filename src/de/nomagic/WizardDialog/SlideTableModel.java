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
package de.nomagic.WizardDialog;

import java.util.HashMap;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * @author Lars P&ouml;tter
 * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
 *
 */
public class SlideTableModel extends AbstractTableModel
{
    public static final int MAX_COLUMNS = 100;

    private static final long serialVersionUID = 1L;
    private Vector<String> columnNames = new Vector<String>();
    private Vector<Boolean> columnEditable = new Vector<Boolean>();
    @SuppressWarnings("rawtypes")
    private Vector<Class> columnType = new Vector<Class>();
    private HashMap<Integer, Object> data = new HashMap<Integer, Object>();
    private int max_row = -1;

    public SlideTableModel()
    {
        // TODO Auto-generated constructor stub
    }

    @SuppressWarnings("rawtypes")
    public void addColumn(String Name, boolean editable, Class type)
    {
        columnNames.add(Name);
        columnEditable.add(editable);
        columnType.add(type);
    }



    @Override
    public int getRowCount()
    {
        return max_row + 1;
    }

    @Override
    public int getColumnCount()
    {
        return columnNames.size();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Class getColumnClass(int c)
    {
        return columnType.get(c);
    }

    public String getColumnName(int col)
    {
        return columnNames.get(col);
    }

    public boolean isCellEditable(int row, int col)
    {
        return columnEditable.get(col);
    }

    public void setValueAt(Object value, int row, int col)
    {
        if(row > max_row)
        {
            max_row = row;
        }
        data.put((row * MAX_COLUMNS) + col, value);
        fireTableCellUpdated(row, col);
    }

    @Override
    public Object getValueAt(int row, int col)
    {
        return data.get((row * MAX_COLUMNS) + col);
    }

}
