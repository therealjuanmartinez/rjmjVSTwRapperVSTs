package rjm.vst.midi.polytool;

import java.io.Serializable;

import rjm.midi.tools.Note;
import rjm.vst.tools.VstUtils;

public class PolyRow implements Serializable {
	
	private static final long serialVersionUID = -5877708938899912589L;
	
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
 