package rjm.vst.midi.polytool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rjm.midi.tools.Note;
import rjm.vst.tools.VstUtils;

public class PolyRow implements Serializable {
	
	private static final long serialVersionUID = -5877708938899912589L;
	
	private class DatedMidiVal {

	    private int value;
	    private Date timeReceived;

	    public DatedMidiVal(int value, Date timeReceived) throws Exception
	    {
		super();
		if ((value < 0)||(value > 127))
		{
		    throw new Exception("Error, non >=0, <=127 value received.");
		}
		this.value = value;
		this.timeReceived = timeReceived;
	    }
	    
	    public DatedMidiVal(int value) throws Exception
	    { this(value, new Date()); }

	    public int getValue()
	    { return value; }

	    public Date getTimeReceived()
	    { return timeReceived; }
	}

	public PolyRow()
	{
	    note = null;
	    inputChannel = -1;
	    outputCCNum = -1;
	    outputChannel = -1;
	    minOutputValue = 0;
	    maxOutputValue = 127;
	    noteOffCCValue = 0;
	    enabled = true;
	    inverse = false;
	    useAllKeys = false;
	    
	    doAveraging = false;
	    latestVals = new ArrayList<DatedMidiVal>();
	    isPlayNotes = true;
	}
	
	public boolean isDoingAveraging()
	{
	    return this.doAveraging;
	}
	
	//This whole boolean can probably be removed ultimately in favor of just
	//doing this automatically when not in single-note mode
	public void setIsDoingAveraging(boolean b)
	{
	    this.doAveraging = b;
	}
	
	
	public void submitRealtimeValue(int val)
	{
	    try
	    {
		latestVals.add(new DatedMidiVal(val));
	    } 
	    catch (Exception e)
	    { }
	}
	
	private void freshenValsArray()
	{
	    //Make sure values are less than X milliseconds old
	    List<DatedMidiVal> oldVals = new ArrayList<DatedMidiVal>();
	    
	    for (int i = 0; i < latestVals.size(); i++)
	    {
		Date now = new Date();
		long nowMillis = now.getTime();
		long thenMillis = latestVals.get(i).getTimeReceived().getTime();
		
		long diff = nowMillis - thenMillis;

		if (diff > 500)
		{
		    oldVals.add(latestVals.get(i));
		}
	    }
	    
	    for (int i = 0; i < oldVals.size(); i++)
	    {
		this.latestVals.remove(oldVals.get(i));
	    }
	}

	public int getAverageValue()
	{
	    freshenValsArray();
	    int avg;
	    if (latestVals.get(latestVals.size() - 1).value == 127)
	    {
		avg = 127; //HACK... take this out once PolyRow class does its own processing of data
                           //so that the 'decay' can be done over time even if no MIDI input is being recieved
	    }
	    else
	    {
		int total = 0;
		for (int i = 0; i < latestVals.size(); i++)
		{
		    total += latestVals.get(i).getValue();
		}
		avg = total / latestVals.size();
	    }
	    return avg;
	}
	
	
	private boolean isPlayNotes;


	public boolean isPlayNotesActive()
	{
	    return isPlayNotes;
	}

	public void setIsPlayNotesActive(boolean isPlayNotes)
	{
	    this.isPlayNotes = isPlayNotes;
	}

	public String getDebugString()
	{
	    StringBuilder sb = new StringBuilder();
	    sb.append("\n");
	    sb.append("Enabled " + this.enabled + "\n");
	    sb.append("ID " + this.id + "\n");
	    sb.append("Input Channel " + this.inputChannel + "\n");
	    sb.append("Output Channel " + this.outputChannel + "\n");
	    sb.append("Output CC " + this.outputCCNum + "\n");
	    sb.append("Max Out " + this.maxOutputValue + "\n");
	    sb.append("Min Out " + this.minOutputValue + "\n");
	    //sb.append("Note " + this. + "\n");
	    return sb.toString();
	}
	
	public Boolean isGoodForProcessing()
	{
	    if (!enabled)
	    {
		return false;
	    }
	    if (!this.isUseAllKeys())
	    {
		try
		{
		    note.getNoteName();
		}
		catch (Exception e){VstUtils.out("couldn't get note"); return false;}
	    }

	    if ((inputChannel < 0)||
            (outputChannel < 0)||
            (outputCCNum < 0)||
            (minOutputValue < 0)||
            (maxOutputValue < 0)
            )
	    {
		VstUtils.out("one of the values was bad");
		return false;
            }
	    
	    return true;
	}

	private Note note;
	private String name;
	public String getName()
	{ return name; }

	public void setName(String name)
	{ this.name = name; }

	private int inputChannel;
	private int outputChannel;
	private int minOutputValue;
	private int maxOutputValue;
	private int noteOffCCValue;
	
	private boolean useAllKeys;

	public int getNoteOffCCValue()
	{ return noteOffCCValue; }

	public void setNoteOffCCValue(int noteOffCCValue)
	{ this.noteOffCCValue = noteOffCCValue; }

	private int outputCCNum;
	private int id;
	private boolean enabled;
	private boolean inverse;

	private boolean doAveraging;
	private List<DatedMidiVal> latestVals;
	
	public boolean isInverse()
	{ return inverse; }
	public void setInverse(boolean inverse)
	{ this.inverse = inverse; }
	public void setEnabled(boolean enabled)
	{ this.enabled = enabled; }
	public boolean getEnabled ()
	{ return this.enabled; }
	public int getId()
	{ return id; }
	public void setId(int id)
	{ this.id = id; }
	public Note getNote()
	{ return note; }
	public void setNote(Note note)
	{ this.note = note; }
	public int getInputChannel()
	{ return inputChannel; }
	public void setInputChannel(int inputChannel)
	{ this.inputChannel = inputChannel; }
	public int getOutputChannel()
	{ return outputChannel; }
	public void setOutputChannel(int outputChannel)
	{ this.outputChannel = outputChannel; }
	public int getMinOutputValue()
	{ return minOutputValue; }
	public void setMinOutputValue(int minOutputValue)
	{ this.minOutputValue = minOutputValue; }
	public int getMaxOutputValue()
	{ return maxOutputValue; }
	public void setMaxOutputValue(int maxOutputValue)
	{ this.maxOutputValue = maxOutputValue; }
	public int getOutputCCNum()
	{ return outputCCNum; }
	public void setOutputCCNum(int outputCCNum)
	{ this.outputCCNum = outputCCNum; }
	public boolean isUseAllKeys()
	{ return useAllKeys; } 
	public void setUseAllKeys(boolean useAllKeys)
	{ this.useAllKeys = useAllKeys; } 
}
 