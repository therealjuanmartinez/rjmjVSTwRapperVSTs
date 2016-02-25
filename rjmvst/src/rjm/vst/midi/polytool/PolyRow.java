package rjm.vst.midi.polytool;

import java.io.Serializable;

import rjm.midi.tools.Note;

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
	    enabled = true;
	}

	private Note note;
	private int inputChannel;
	private int outputChannel;
	private int minOutputValue;
	private int maxOutputValue;
	private int outputCCNum;
	private int id;
	private boolean enabled;

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

}
 