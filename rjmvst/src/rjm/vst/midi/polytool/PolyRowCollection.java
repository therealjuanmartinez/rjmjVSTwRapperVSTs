package rjm.vst.midi.polytool;

import java.io.Serializable;
import java.util.ArrayList;

import rjm.midi.tools.Note;
import rjm.vst.tools.VstUtils;

public class PolyRowCollection implements Serializable {
	private static final long serialVersionUID = 1769311394643385592L;
	private ArrayList<PolyRow> rows;
	
	int x ;

	public PolyRowCollection()
	{
	    rows = new ArrayList<PolyRow>();
	}
	
	public PolyRow getRow(int index)
	{
	    return rows.get(index);
	}
	
	public int add(PolyRow row)
	{
	    row.setId(getNewId());
	    rows.add(row);
	    return row.getId();
	}
	
	public void emptyCollection()
	{
	    rows.clear();
	}
	
	public void updateRow(PolyRow row)
	{
	    for (int i = 0; i < rows.size(); i++)
	    {
		if (rows.get(i).getId() == row.getId())
		{
		    rows.set(i, row); //This does a replacement
		}
	    }
	}
	
	private int getNewId()
	{
	    int max = 0;
	    for (int i = 0; i < rows.size(); i++)
	    {
		if (rows.get(i).getId() > max)
		{
		    max = rows.get(i).getId(); 
		}
	    }
	    return max + 1;
	}

	public void removeRow(int rowId)
	{
	    for (int i = 0; i < rows.size(); i++)
	    {
		if (rows.get(i).getId() == rowId)
		{
		    rows.remove(rows.get(i));
		    i = rows.size();
		}
	    }
	}

	public int size()
	{
	    return this.rows.size();
	}
	
	public PolyRow getRowByRowId(int id)
	{
	    for (int i = 0; i < rows.size(); i++)
	    {
		PolyRow row = rows.get(i);
		if (row.getId() == id)
		{
		    return row;
		}
	    }
	    return null;
	}
	
	public PolyRow getRowByRowId(String id)
	{
	    return getRowByRowId(Integer.parseInt(id));
	}
	
 }
