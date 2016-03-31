package rjm.vst.midi.polytool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import jvst.wrapper.valueobjects.VSTEvent;
import jvst.wrapper.valueobjects.VSTEvents;
import jvst.wrapper.valueobjects.VSTMidiEvent;
import rjm.midi.tools.MidiUtils;
import rjm.midi.tools.Note;
import rjm.vst.tools.VstUtils;

public class PolyRow implements Serializable, MidiRow {
	
	private static final long serialVersionUID = -5877708938899912589L;
        private static int PITCH_BEND = 70;
	
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

	PolyTool p;
	public PolyRow(PolyTool p)
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
	    this.p = p;
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
	
	public boolean isGoodForProcessing()
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
	
	
	
	
	 // process MIDI
	    public void processEvents(VSTEvents events)
	    {
		//TODO process this through "MidiRow" interface objects 
		
		List<VSTEvent> origEvents = VstUtils.cloneVSTEventsToList(events);
	        List<VSTEvent> newEvents = new ArrayList<VSTEvent>();

			VstUtils.out(getDebugString());
			VstUtils.out("Here we are");
			if (this.isGoodForProcessing()) //Instantiated and has usable values
			{
			    //Get new CC events that need to be added to output
			    List<VSTEvent> justNewCCEvents = convertPolyAftertouchToCCAndReturnOnlyNewCCEvents(origEvents);
			    VstUtils.out("e has " + justNewCCEvents.size());

	                    //VstUtils.out("****************BELOW SHOULD ONLY BE CC EVENTS:");
			    newEvents.addAll(justNewCCEvents); //Add new events to output
	                    //VstUtils.outputVstMidiEventsForDebugPurposes(VstUtils.convertToVSTEvents(newEvents));
			    //VstUtils.out("****************END OF ONLY CC EVENTS");
			}
			else
			{
			    VstUtils.out("Row was NOT good to go");
			}

			if (this.isGoodForProcessing()) //Instantiated and has usable values
			{
			    if (!this.isUseAllKeys())
			    {
	                        //Finally, after all rows, strip outgoing events of all incoming noteon/off events that were actioned
	                        origEvents = stripMessagesBasedOnNoteNum(origEvents, this.inputChannel, this.note.getMidiNoteNumber());
			    }
			    else
			    {
				origEvents = stripPolyMessagesAndUpdateNoteOnOffChannel(origEvents, this.inputChannel, this.outputChannel, this.isPlayNotes);
			    }
			}
			
		    //FINALLY now we just make sure the CC events are on the top of the collection and then lets ship it off
		    newEvents.addAll(origEvents);
		    
		    VstUtils.out("******************NEW EVENTS:");
	            VstUtils.outputVstMidiEventsForDebugPurposes(VstUtils.convertToVSTEvents(newEvents));
		    VstUtils.out("******************END OF NEW EVENTS:");

		p.processMidiFromRow(VstUtils.convertToVSTEvents(newEvents), this); //Now shoot those MIDI events back out to parent plugin for further processing and/or chaining
	    }
	    
	    
	    
	    private List<VSTEvent> stripPolyMessagesAndUpdateNoteOnOffChannel(List<VSTEvent> events, int inputChannel, int outputChannel, boolean allowNotesToPlayThrough)
	    {
		List<VSTEvent> cleanedEvents = new ArrayList<VSTEvent>();
		VstUtils.out("Considering " + events.size() + " to clean");

		for (int i = 0; i < events.size(); i++)
		{
		    VSTEvent e = events.get(i);

		    if( e.getType() == VSTEvent.VST_EVENT_MIDI_TYPE ) 
		    {
			//out("Considering midi event...");

			byte[] msg_data = ((VSTMidiEvent)e).getData();
			int ctrl_index, ctrl_value, msg_status, msg_channel;
			msg_status = MidiUtils.getStatusFromMidiByteArray(msg_data);
			msg_channel = MidiUtils.getChannelFromMidiByteArray(msg_data);
			int status = MidiUtils.getStatusWithoutChannelByteFromMidiByteArray(msg_data);

			if (msg_channel != inputChannel)//Incoming message not on captured input channel
			{
			    //Let it through, since it's not on the channel we're capturing
	                    cleanedEvents.add(e); 
			}
			else //matches input channel we're capturing
			{
			    if (status != ShortMessage.POLY_PRESSURE) //Ignoring all POLY messages
			    {
				if ((status == ShortMessage.NOTE_ON) || (status == ShortMessage.NOTE_OFF))
				{
				    //Update channel for this note on/off
				    if (allowNotesToPlayThrough) //Only send if note play-through is enabled
				    {
	                                    cleanedEvents.add(VstUtils.convertMidiChannel(e, inputChannel, outputChannel)); 
				    }
	                        }
				else //Passing through messages as is, since they are not relevant to this logic 
				    //and we don't want to drop them by default
				{
				    cleanedEvents.add(e);
				}
			    }
			}
		    }
		}
		VstUtils.out("returning " + cleanedEvents.size());
		return cleanedEvents;
	    }
	    
	    
	    private List<VSTEvent> stripMessagesBasedOnNoteNum(List<VSTEvent> events, int stripChan, int stripNoteNum)
	    {
		List<VSTEvent> cleanedEvents = new ArrayList<VSTEvent>();
		
		VstUtils.out("Considering " + events.size() + " to clean");

		for (int i = 0; i < events.size(); i++)
		{
		    VSTEvent e = events.get(i);

		    if( e.getType() == VSTEvent.VST_EVENT_MIDI_TYPE ) 
		    {
			//out("Considering midi event...");

			byte[] msg_data = ((VSTMidiEvent)e).getData();

			int ctrl_index, ctrl_value, msg_status, msg_channel;
			msg_status = MidiUtils.getStatusFromMidiByteArray(msg_data);
			if( msg_status == 0xF ) {
			    /* Ignore system messages.*/
			    //return;
			}
			msg_channel = MidiUtils.getChannelFromMidiByteArray(msg_data);
			ctrl_index = MidiUtils.getData1FromMidiByteArray(msg_data);
			ctrl_value = MidiUtils.getData2FromMidiByteArray(msg_data);
			int status = MidiUtils.getStatusWithoutChannelByteFromMidiByteArray(msg_data);
			int noteNum = ctrl_index;

			//if (!((status == ShortMessage.NOTE_OFF)||(status == ShortMessage.NOTE_ON))&& (noteNum == stripNoteNum)&&(msg_channel == stripChan - 1))
			if (!((noteNum == stripNoteNum)&&(msg_channel == stripChan)))
			{
			    cleanedEvents.add(e);
			}
		    }
		}

		VstUtils.out("returning " + cleanedEvents.size());

		return cleanedEvents;
	    }
	    
	    
	    private List<VSTEvent> convertPolyAftertouchToCCAndReturnOnlyNewCCEvents(List<VSTEvent> ev)
	    {
		List<VSTEvent> newEvs = new ArrayList<VSTEvent>();
		
		//out("Got " + ev.size() + " events to consider");

		for (int i = 0; i < ev.size(); i++)
		{
		    VSTEvent e = ev.get(i);

		    if( e.getType() == VSTEvent.VST_EVENT_MIDI_TYPE ) 
		    {
			//out("Considering midi event...");

			byte[] msg_data = ((VSTMidiEvent)e).getData();

			int ctrl_index, ctrl_value, msg_status, msg_channel;
	                msg_status = MidiUtils.getStatusFromMidiByteArray(msg_data);
			if( msg_status == 0xF ) {
			    /* Ignore system messages.*/
			    //return;
			}
			msg_channel = MidiUtils.getChannelFromMidiByteArray(msg_data);
			ctrl_index = MidiUtils.getData1FromMidiByteArray(msg_data);
			ctrl_value = MidiUtils.getData2FromMidiByteArray(msg_data);
			int status = MidiUtils.getStatusWithoutChannelByteFromMidiByteArray(msg_data);

			//VstUtils.out("Status for incoming message is " + status);

			//VstUtils.out("Channel is supposedly " + msg_channel + " and must match " + row.getInputChannel() + " in order to go further...");

			if (msg_channel == this.getInputChannel())
			{
			    //VstUtils.out("Channel is correct at " + msg_channel + " and status is " + status + " though we're looking for a " + ShortMessage.POLY_PRESSURE );

			    //boolean foundPressureOrAftertouch = false;
			    int noteNum = ctrl_index;
			    
			    //VstUtils.out("Looking for poly pressure against note " + row.getNote().getMidiNoteNumber());
			    //VstUtils.out("Current is " + noteNum);
	                    ctrl_index = this.getOutputCCNum();
	                    
	                    boolean noteMatchesOrUsingAllKeys = false;
	                    if (this.isUseAllKeys())
	                    {
	                	noteMatchesOrUsingAllKeys = true; //Apply effect regardless of which note is played 
	                    }
	                    else if (this.getNote() != null)
	                    {
	                	if (this.getNote().getMidiNoteNumber() == noteNum)
	                	{ noteMatchesOrUsingAllKeys = true; } //Poly for the desired note...
	                    }

			    if ((status == ShortMessage.POLY_PRESSURE)&&(noteMatchesOrUsingAllKeys))
			    {
				//VstUtils.out("Found poly value " + ctrl_value);
				//First change the value to adhere to the max/min supplied to this funciton
				double val = ctrl_value; //Converting to double seems to have fixed one bug in testing
				double ratio = val / 127;
				int delta = this.getMaxOutputValue() - this.getMinOutputValue();
				int newval = 0;
				if (!this.isInverse())
				{ newval = (int)(delta * ratio) + this.getMinOutputValue(); }
				else
				{ newval = this.getMaxOutputValue() - (int)(delta * ratio); }
				ctrl_value = newval; //This is the new value that will be output to CC

				if (this.isDoingAveraging())
				{
				    this.submitRealtimeValue(ctrl_value);
				    ctrl_value = this.getAverageValue();
				}

				try
				{
				    //Create new midi message

				    ShortMessage s = null;
				    if (this.getOutputCCNum() <= 64) //Not a special, non-CC like Pitch Bend
				    {
	                                    s = new ShortMessage(ShortMessage.CONTROL_CHANGE,  this.getOutputChannel() - 1, ctrl_index, ctrl_value);
				    }
				    else if (this.getOutputCCNum() == PITCH_BEND) //Yes this is a hack since PITCH_BEND is NOT a CC
				    {
					//For Pitch Bend, 2 bytes need to be [0][40](hex) when pitch bend is "Off"
					//Otherwise go from 0 - 127 on both bytes, ~64 is middle (pitchwise)
					//I'm certain there's a better high-res way to do this, but this is exactly how my CME keyboard does it
					if (ctrl_value == 64) {ctrl_value--;} //I dunno, my keyboard never sends a [40][40] so I'm following that logic
	                                ctrl_index = ctrl_value;
	                                s = new ShortMessage(ShortMessage.PITCH_BEND, this.getOutputChannel() - 1, ctrl_index, ctrl_value);
				    }
				    //out("ShortMessage channel is " + s.getChannel() + " while output channel is " + outputChannel);
				    VSTMidiEvent newEvent = new VSTMidiEvent();
				    newEvent.setData(s.getMessage());
				    //VstUtils.out("newEvent channel is " + (newEvent.getData()[0] & 0xF));
				    VSTEvent event = new VSTEvent();
				    event = newEvent;
				    event.setType(VSTEvent.VST_EVENT_MIDI_TYPE); //Apparently this is needed...
				    newEvs.add(event);
				    //out("Added new event to return list, which now has " + newEvs.size() + " elements");
				} catch (InvalidMidiDataException e1)
				{
				    VstUtils.out(e1.getStackTrace().toString());
				}
			    }
			    else if ((status == ShortMessage.NOTE_OFF)&&(noteMatchesOrUsingAllKeys)) 
			    {
				try
				{
				    ctrl_value = this.getNoteOffCCValue();
				    //Create new midi message
				    ShortMessage s = null;
				    if (this.getOutputCCNum() <= 64) //Not a special, non-CC like Pitch Bend
				    {
				        s = new ShortMessage(ShortMessage.CONTROL_CHANGE,  this.getOutputChannel() - 1, ctrl_index, ctrl_value);
				    }
				    else if (this.getOutputCCNum() == PITCH_BEND)
				    {
					//For Pitch Bend, 2 bytes need to be [0][40](hex) when pitch bend is "Off" as MIDI standard
	                                s = new ShortMessage(ShortMessage.PITCH_BEND, this.getOutputChannel() - 1, 0, 64); //64=40-hex
				    }
				    //out("ShortMessage channel is " + s.getChannel() + " while output channel is " + outputChannel);
				    VstUtils.out("OK got a note off so sending CC value " + ctrl_value + " to CC " + ctrl_index);
				    VSTMidiEvent newEvent = new VSTMidiEvent();
				    newEvent.setData(s.getMessage());

				    //VstUtils.out("newEvent channel is " + (newEvent.getData()[0] & 0xF));
				    VSTEvent event = new VSTEvent();
				    event = newEvent;
				    event.setType(VSTEvent.VST_EVENT_MIDI_TYPE); //Apparently this is needed...
				    newEvs.add(event);
				    //out("Added new event to return list, which now has " + newEvs.size() + " elements");
				} catch (InvalidMidiDataException e1)
				{
				    VstUtils.out(e1.getStackTrace().toString());
				}
			    }
			}

		    }
		}

		VstUtils.out("newEvs has " + newEvs.size() + " elements");
		return newEvs;
	    }
	    

}
 