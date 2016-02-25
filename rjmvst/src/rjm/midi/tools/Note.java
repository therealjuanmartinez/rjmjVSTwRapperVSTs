package rjm.midi.tools;

import java.io.Serializable;

//This class was borrowed from another project which is why it has more functionality than is used by any dependent project as of this writing

public class Note implements Comparable, Serializable{

	/**
     * 
     */
    private static final long serialVersionUID = -1180161537746845593L;
	private int midiNoteNum;
	private int velocity;

	public Note(int midiNoteNum) throws Exception {
		this.midiNoteNum = midiNoteNum;
		if ((midiNoteNum < 0) || (midiNoteNum > 127)) {
			throw new Exception("out of range...");
		}
		velocity = -1;
	}
	
	public Note(int midiNoteNum, int velocity) throws Exception 
	{
		this.midiNoteNum = midiNoteNum;
		this.velocity = velocity;
		if ((midiNoteNum < 0) || (midiNoteNum > 127)) 
		{ throw new Exception("note number out of range..."); }
		if ((velocity < 0) || (velocity > 127)) 
		{ throw new Exception("velocity out of range..."); }
	}
	
	public int getVelocity()
	{
	    return this.velocity;
	}
	public void setVelocity(int velocity)
	{
	    this.velocity = velocity;
	}
	
	public Note clone()
	{
	    try
	    {
		return new Note(this.midiNoteNum);
	    } catch (Exception e)
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    return null;
	}
	
	public int getMidiNoteNumber()
	{
		return this.midiNoteNum;
	}
	
	public void setMidiNoteNumber(int newNoteNum)
	{
		this.midiNoteNum = newNoteNum;
	}

	public String getNoteName(boolean sharp) {
		
		int num = this.midiNoteNum;
		while (num > 11)
		{ num -= 12; }
		
		if (sharp)
		{
                switch(num)
                {
                case 0: return "C";
                case 1: return "C#";
                case 2: return "D";
                case 3: return "D#";
                case 4: return "E";
                case 5: return "F";
                case 6: return "F#";
                case 7: return "G";
                case 8: return "G#";
                case 9: return "A";
                case 10: return "A#";
                case 11: return "B";
                }
		}
		else
		{
                switch(num)
                {
                case 0: return "C";
                case 1: return "Db";
                case 2: return "D";
                case 3: return "Eb";
                case 4: return "E";
                case 5: return "F";
                case 6: return "Gb";
                case 7: return "G";
                case 8: return "Ab";
                case 9: return "A";
                case 10: return "Bb";
                case 11: return "B";
                }
		}
		
		return null;
	}
	
	public int getOctave()
	{
		int octave = 0;
		int num = this.midiNoteNum;
		while (num > 11)
		{
			num -= 12;
			octave++;
		}
		return octave;
	}
	
	public String getNoteNamePlusOctave() //Default to Sharp name vs. Flat name
	{
		return getNoteName(true) + getOctave();
	}
	
	public String getNoteNamePlusOctave(boolean sharp)
	{
		return getNoteName(sharp) + getOctave();
	}
	
	public String getNoteName() //Default to Shart naem vs. Flat name
	{
		return getNoteName(true);
	}

	@Override
	public int compareTo(Object compnote) {
		int comparenum = ((Note) compnote).midiNoteNum;
		/* For Ascending order */
		return this.midiNoteNum - comparenum;
	}

}
