package rjm.vst.midi.polytool;

import java.io.Serializable;
import java.util.ArrayList;

public class MidiRowCollection implements Serializable {
	private static final long serialVersionUID = 1769311394643385592L;
	private ArrayList<MidiRow> rows;
	
	public MidiRowCollection()
	{
	    rows = new ArrayList<MidiRow>();
	}
	
	public MidiRow getRow(int index)
	{
	    return rows.get(index);
	}
	
	public int add(MidiRow row)
	{
	    row.setId(getNewId());
	    rows.add(row);
	    return row.getId();
	}
	
	public void emptyCollection()
	{
	    rows.clear();
	}
	
	public void updateRow(MidiRow row)
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
	
	public MidiRow getRowByRowId(int id)
	{
            for (int i = 0; i < rows.size(); i++)
	    {
		MidiRow row = rows.get(i);
		if (row.getId() == id)
		{
		    return row;
		}
	    }
	    return null;
	}
	
	public MidiRow getRowByRowId(String id)
	{
	    return getRowByRowId(Integer.parseInt(id));
	}
	
	public int getIndexFromRowId(int id)
	{
	    for (int i = 0; i < rows.size(); i++)
	    {
		MidiRow row = rows.get(i);
		if (row.getId() == id)
		{
		    return i;
		}
	    }
	    return -1;
	}
	
	public boolean areAllRowsDisabled()
	{
	    for (int i = 0; i < rows.size(); i++)
	    {
		MidiRow row = rows.get(i);
		if (row.getEnabled())
		{
		    return false;
		}
	    }
	    return true;
	}
	
	public MidiRow getSuccessorToThisMidiRow(MidiRow row) //Used for chaining
	{
	    int i = getIndexFromRowId(row.getId());
	    if (this.rows.size() > i + 1)
	    {
		return this.rows.get(i + 1);
	    }
	    else
	    { return null; }
	}
	
 }
