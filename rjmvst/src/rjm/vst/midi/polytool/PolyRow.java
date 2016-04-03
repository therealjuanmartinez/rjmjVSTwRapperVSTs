package rjm.vst.midi.polytool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import com.sun.scenario.effect.light.Light.Type;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import jvst.wrapper.valueobjects.VSTEvent;
import jvst.wrapper.valueobjects.VSTEvents;
import jvst.wrapper.valueobjects.VSTMidiEvent;
import rjm.midi.tools.MidiUtils;
import rjm.midi.tools.Note;
import rjm.vst.midi.polytool.PolyTool.TypedVSTEvent;
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

    transient PolyTool p;
    transient Button learnBtn;
    private boolean letNotesPlayThrough;
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
	letNotesPlayThrough = true;
	this.p = p;
    }

    public void setPlugin(PolyTool p)
    {
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
	{ 
	    latestVals = new ArrayList<DatedMidiVal>();
	    try
	    {
		latestVals.add(new DatedMidiVal(val));
	    } catch (Exception e1)
	    { }
	}
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

    private int getAverageValue()
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
    transient private List<DatedMidiVal> latestVals;

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
    private int getInputChannel()
    { return inputChannel; }
    private void setInputChannel(int inputChannel)
    { this.inputChannel = inputChannel; }
    private int getOutputChannel()
    { return outputChannel; }
    private void setOutputChannel(int outputChannel)
    { this.outputChannel = outputChannel; }
    private int getMinOutputValue()
    { return minOutputValue; }
    private void setMinOutputValue(int minOutputValue)
    { this.minOutputValue = minOutputValue; }
    private int getMaxOutputValue()
    { return maxOutputValue; }
    private void setMaxOutputValue(int maxOutputValue)
    { this.maxOutputValue = maxOutputValue; }
    private int getOutputCCNum()
    { return outputCCNum; }
    private void setOutputCCNum(int outputCCNum)
    { this.outputCCNum = outputCCNum; }
    private boolean isUseAllKeys()
    { return useAllKeys; } 
    private void setUseAllKeys(boolean useAllKeys)
    { this.useAllKeys = useAllKeys; } 




    private boolean doLearnButton;
    // process MIDI
    public void processEvents(VSTEvents events)
    {
	//TODO process this through "MidiRow" interface objects 
	
	if (doLearnButton) //"Learn" the note from Learn button
	  { events = doLearnButton(events, this.learnBtn); }


	List<VSTEvent> origEvents = VstUtils.cloneVSTEventsToList(events);
	List<TypedVSTEvent> newEvents = new ArrayList<TypedVSTEvent>();
	List<TypedVSTEvent> newOrigEvents = new ArrayList<TypedVSTEvent>();

	VstUtils.out(getDebugString());
	VstUtils.out("Here we are");
	if (this.isGoodForProcessing()) //Instantiated and has usable values
	{
	    //Get new CC events that need to be added to output
	    List<TypedVSTEvent> justNewCCEvents = convertPolyAftertouchToCCAndReturnOnlyNewCCEvents(origEvents);
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
		//Finally, after all rows, strip outgoing events of all incoming noteon/off events that were actioned if play notes checkbox allows
		List<TypedVSTEvent> set = stripMessagesBasedOnNoteNum(origEvents);
		newOrigEvents = set;
	    }
	    else
	    {
		newOrigEvents = stripPolyMessagesAndUpdateNoteOnOffChannel(origEvents);
	    }
	}

	//FINALLY now we just make sure the CC events are on the top of the collection and then lets ship it off
	newEvents.addAll(newOrigEvents);

	VstUtils.out("******************NEW EVENTS:");
	VstUtils.outputVstMidiEventsForDebugPurposes(VstUtils.convertToVSTEvents2(newEvents));
	VstUtils.out("******************END OF NEW EVENTS:");

	p.processMidiFromRow(newEvents, this); //Now shoot those MIDI events back out to parent plugin for further processing and/or chaining
    }



    private List<TypedVSTEvent> stripPolyMessagesAndUpdateNoteOnOffChannel(List<VSTEvent> events)
    {
	List<TypedVSTEvent> cleanedEvents = new ArrayList<TypedVSTEvent>();
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
		    cleanedEvents.add(new TypedVSTEvent(e, TypedVSTEvent.PASSTHROUGH)); 
		}
		else //matches input channel we're capturing
		{
		    if (status != ShortMessage.POLY_PRESSURE) //Ignoring all POLY messages
		    {
			if ((status == ShortMessage.NOTE_ON) || (status == ShortMessage.NOTE_OFF))
			{
			    cleanedEvents.add(new TypedVSTEvent(e, TypedVSTEvent.REMOVAL));  //remove original note-on
			    //Update channel for this note on/off
			    if (letNotesPlayThrough) //Only send if note play-through is enabled
			    {
				cleanedEvents.add(new TypedVSTEvent(VstUtils.convertMidiChannel(e, inputChannel, outputChannel), TypedVSTEvent.NEW)); 
			    }
			}
			else //Passing through messages as is, since they are not relevant to this logic 
			    //and we don't want to drop them (here) by default
			{
                             cleanedEvents.add(new TypedVSTEvent(e, TypedVSTEvent.PASSTHROUGH));  //passthrough event
			}
		    }
		}
	    }
	}
	VstUtils.out("returning " + cleanedEvents.size());
	return cleanedEvents;
    }


    private List<TypedVSTEvent>  stripMessagesBasedOnNoteNum(List<VSTEvent> events)
    {
	List<TypedVSTEvent> returnEvents = new ArrayList<TypedVSTEvent>();

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

	        if ((noteNum == note.getMidiNoteNumber()) && ((status == ShortMessage.NOTE_ON)||(status == ShortMessage.NOTE_OFF)))
	        {
	            //Remove all original Note On/Off messages (this COULD be 
		     returnEvents.add(new TypedVSTEvent(e, TypedVSTEvent.REMOVAL)); //Removal of note on/off message on original channel
	        }

		if 
		(
			((letNotesPlayThrough) || !(noteNum == note.getMidiNoteNumber()))
			&&(msg_channel == inputChannel)
		)
		{

		    //Basically this if statements will allow all messages to go through if they aren't associated with the learned note 
		    //OR if they ARE associated with the learned note, they must not be POLY PRESSURE messages in order to get passed back to the calling function
		    if ((noteNum == note.getMidiNoteNumber())&&(status != ShortMessage.POLY_PRESSURE))
                        {
			    //Generally this will be NOTE ON/OFF messages; just need to convert to proper output channel

			    if (letNotesPlayThrough)
			    {
				//Only send this message if note playthrough is allowed
                                    returnEvents.add(new TypedVSTEvent(VstUtils.convertMidiChannel(e, getInputChannel(), getOutputChannel()), TypedVSTEvent.NEW));
			    }
                        }
                        else if
			(!(noteNum == note.getMidiNoteNumber()))
                        {
                            returnEvents.add(new TypedVSTEvent(e, TypedVSTEvent.PASSTHROUGH)); //passthrough
                        }
		}
	    }
	}

	//VstUtils.out("returning " + returnEvents.size());
	//VstEventSet set = new VstEventSet(VstUtils.convertToVSTEvents(cleanedEvents), VstUtils.convertToVSTEvents(eventsWeDontWantTheHostToGet));
	return returnEvents;
    }
    


    private List<TypedVSTEvent> convertPolyAftertouchToCCAndReturnOnlyNewCCEvents(List<VSTEvent> ev)
    {
	List<TypedVSTEvent> newEvs = new ArrayList<TypedVSTEvent>();

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

		if (msg_channel == getInputChannel())
		{
		    //VstUtils.out("Channel is correct at " + msg_channel + " and status is " + status + " though we're looking for a " + ShortMessage.POLY_PRESSURE );

		    //boolean foundPressureOrAftertouch = false;
		    int noteNum = ctrl_index;

		    //VstUtils.out("Looking for poly pressure against note " + row.getNote().getMidiNoteNumber());
		    //VstUtils.out("Current is " + noteNum);
		    ctrl_index = getOutputCCNum();

		    boolean noteMatchesOrUsingAllKeys = false;
		    if (isUseAllKeys())
		    {
			noteMatchesOrUsingAllKeys = true; //Apply effect regardless of which note is played 
		    }
		    else if (getNote() != null)
		    {
			if (getNote().getMidiNoteNumber() == noteNum)
			{ noteMatchesOrUsingAllKeys = true; } //Poly for the desired note...
		    }

		    if ((status == ShortMessage.POLY_PRESSURE)&&(noteMatchesOrUsingAllKeys))
		    {
			newEvs.add(new TypedVSTEvent(e, TypedVSTEvent.REMOVAL)); //Adding this POLY event as a removal item so no other rows pass them through

			//VstUtils.out("Found poly value " + ctrl_value);
			//First change the value to adhere to the max/min supplied to this funciton
			double val = ctrl_value; //Converting to double seems to have fixed one bug in testing
			double ratio = val / 127;
			int delta = getMaxOutputValue() - getMinOutputValue();
			int newval = 0;
			if (!isInverse())
			{ newval = (int)(delta * ratio) + getMinOutputValue(); }
			else
			{ newval = getMaxOutputValue() - (int)(delta * ratio); }
			ctrl_value = newval; //This is the new value that will be output to CC

			if (isDoingAveraging())
			{
			    submitRealtimeValue(ctrl_value);
			    ctrl_value = getAverageValue();
			}

			try
			{
			    //Create new midi message

			    ShortMessage s = null;
			    if (getOutputCCNum() <= 64) //Not a special, non-CC like Pitch Bend
			    {
				s = new ShortMessage(ShortMessage.CONTROL_CHANGE,  getOutputChannel() - 1, ctrl_index, ctrl_value);
			    }
			    else if (getOutputCCNum() == PITCH_BEND) //Yes this is a hack since PITCH_BEND is NOT a CC
			    {
				//For Pitch Bend, 2 bytes need to be [0][40](hex) when pitch bend is "Off"
				//Otherwise go from 0 - 127 on both bytes, ~64 is middle (pitchwise)
				//I'm certain there's a better high-res way to do this, but this is exactly how my CME keyboard does it
				if (ctrl_value == 64) {ctrl_value--;} //I dunno, my keyboard never sends a [40][40] so I'm following that logic
				ctrl_index = ctrl_value;
				s = new ShortMessage(ShortMessage.PITCH_BEND, getOutputChannel() - 1, ctrl_index, ctrl_value);
			    }
			    //out("ShortMessage channel is " + s.getChannel() + " while output channel is " + outputChannel);
			    VSTMidiEvent newEvent = new VSTMidiEvent();
			    newEvent.setData(s.getMessage());
			    //VstUtils.out("newEvent channel is " + (newEvent.getData()[0] & 0xF));
			    VSTEvent event = new VSTEvent();
			    event = newEvent;
			    event.setType(VSTEvent.VST_EVENT_MIDI_TYPE); //Apparently this is needed...
			    newEvs.add(new TypedVSTEvent(event, TypedVSTEvent.NEW));
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
			    ctrl_value = getNoteOffCCValue();
			    //Create new midi message
			    ShortMessage s = null;
			    if (getOutputCCNum() <= 64) //Not a special, non-CC like Pitch Bend
			    {
				s = new ShortMessage(ShortMessage.CONTROL_CHANGE,  getOutputChannel() - 1, ctrl_index, ctrl_value);
			    }
			    else if (getOutputCCNum() == PITCH_BEND)
			    {
				//For Pitch Bend, 2 bytes need to be [0][40](hex) when pitch bend is "Off" as MIDI standard
				s = new ShortMessage(ShortMessage.PITCH_BEND, getOutputChannel() - 1, 0, 64); //64=40-hex
			    }
			    //out("ShortMessage channel is " + s.getChannel() + " while output channel is " + outputChannel);
			    VstUtils.out("OK got a note off so sending CC value " + ctrl_value + " to CC " + ctrl_index);
			    VSTMidiEvent newEvent = new VSTMidiEvent();
			    newEvent.setData(s.getMessage());

			    //VstUtils.out("newEvent channel is " + (newEvent.getData()[0] & 0xF));
			    VSTEvent event = new VSTEvent();
			    event = newEvent;
			    event.setType(VSTEvent.VST_EVENT_MIDI_TYPE); //Apparently this is needed...
			    newEvs.add(new TypedVSTEvent(event, TypedVSTEvent.NEW));
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

    public GridPane getGuiRow() //Create a row in the UI
    { 
	int index = 0;

	//This is meant to be a single 1 row grid
	GridPane rowGrid = new GridPane();
	rowGrid.setPadding(new Insets(4));
	rowGrid.setHgap(10);
	rowGrid.setVgap(5);
	rowGrid.setStyle("-fx-background-color: #C0C0C0;");

	//int numWidth = 50; //width of text fields that contain numbers

	//TODO - Fix bug where this text field doesn't seem to get saved/recalled
	TextField tfRowName = new TextField();
	if (getName() != null)
	{ tfRowName.setText(getName()); }
	else 
	{ tfRowName.setText("Row " + getId()); }
	tfRowName.setId("" + getId());  //The "" in front makes it cast the input as a string
	tfRowName.setMaxWidth(90);
	tfRowName.setStyle("-fx-background-color: #A0A0A0;");
	tfRowName.textProperty().addListener((observable, oldValue, newValue) -> {
	    HandleRowName(tfRowName);
	});
	//tfRowName.setOnAction(e -> this.HandleRowName(tfRowName));  //Apparently this is only if Return is pressed
	rowGrid.add(tfRowName,index,0);
	index++;

	CheckBox cb = new CheckBox();
	cb.setText("");
	if (getEnabled())
	{ cb.setSelected(true); }
	else
	{ cb.setSelected(false); }
	cb.setId("" + getId());
	//This style of event handling below wasn't supposedly available until Java 8, and it's quite nice
	cb.setOnAction(e -> HandleEnabledCheckbox(cb)); 
	rowGrid.add(cb,index,0);
	rowGrid.add(getRowLabel("Enabled"), index, 1);
	index++;

	CheckBox cb1 = new CheckBox();
	cb1.setText("");
	if (isInverse())
	{ cb1.setSelected(true); }
	else
	{ cb1.setSelected(false); }
	cb1.setId("" + getId());
	cb1.setOnAction(e -> HandleInverseCheckbox(cb1)); 
	rowGrid.add(cb1,index,0);
	rowGrid.add(getRowLabel("Inverse"), index, 1);
	index++;

	String[] channels = new String[16];
	for (int i = 0; i < 16; i++){ channels[i] = Integer.toString(i + 1);}

	ComboBox<String> cbInputChannel = new ComboBox<String>();
	cbInputChannel.getItems().addAll(channels);
	if (getInputChannel() >= 0)
	{ //Here getting string instead of int so we pick the item based on the visible GUI value
	    cbInputChannel.getSelectionModel().select(Integer.toString(getInputChannel())); }
	else
	{ cbInputChannel.getSelectionModel().select(0);}//Auto select channel 1 for input }
	cbInputChannel.setId("" + getId());
	cbInputChannel.setOnAction(e -> HandleInChannelCombo(cbInputChannel));
	rowGrid.add(cbInputChannel,index,0);
	rowGrid.add(getRowLabel("Input Channel"),index,1);
	HandleInChannelCombo(cbInputChannel);
	index++;

	ComboBox<String> cbOutputChannel = new ComboBox<String>();
	cbOutputChannel.getItems().addAll(channels);
	if (getOutputChannel() >= 0)
	{ cbOutputChannel.getSelectionModel().select(Integer.toString(getOutputChannel())); }
	else
	{ cbOutputChannel.getSelectionModel().select(0);}//Auto select channel 1 for input }
	cbOutputChannel.setId("" + getId());
	cbOutputChannel.setOnAction(e -> HandleOutChannelCombo(cbOutputChannel));
	rowGrid.add(cbOutputChannel,index,0);
	rowGrid.add(getRowLabel("Output Channel"),index,1);
	HandleOutChannelCombo(cbOutputChannel);
	index++;

        learnBtn = new Button(); //Declaring this early so we can use it with the toggle switch 

	RadioButton rb1 = new RadioButton("Single Note");
	RadioButton rb2 = new RadioButton("All Notes");
	rb1.setSelected(true);
	ToggleGroup gr = new ToggleGroup();
	rb1.setToggleGroup(gr);
	rb2.setToggleGroup(gr);
	rowGrid.add(rb1,index,0);
	rowGrid.add(rb2,index,1);
	index++;


	CheckBox cb3 = new CheckBox();
	cb3.setText("");
	if (letNotesPlayThrough)
	{ cb3.setSelected(true); }
	else
	{ cb3.setSelected(false); }
	cb3.setId("" + getId());
	cb3.setOnAction(e -> HandlePlayNotesCheckbox(cb3)); 
	rowGrid.add(cb3,index,0);
	Label labelNotesPlay = getRowLabel("Notes Play");
	rowGrid.add(labelNotesPlay, index, 1);
	index++;


	rb1.setOnAction(e -> HandleNoteModeSwitch(rb1.isSelected(), getId(), learnBtn, cb3, labelNotesPlay));
	rb2.setOnAction(e -> HandleNoteModeSwitch(rb1.isSelected(), getId(), learnBtn, cb3, labelNotesPlay));

	learnBtn.setText("Learn Note");
	if (getNote() != null)
	{ learnBtn.setText(getNote().getNoteNamePlusOctave()); }
	learnBtn.setId("" + getId());
	learnBtn.setMinWidth(77);
	learnBtn.setOnAction(e -> HandleLearnButton(learnBtn)); 
	rowGrid.add(learnBtn,index,0);
	index++;

	String[] vals = new String[128];
	for (int i = 0; i < 128; i++){ vals[i] = Integer.toString(i);}

	ComboBox<String> minValue = new ComboBox<String>();
	minValue.getItems().addAll(vals);
	if (getMinOutputValue() >= 0)
	{ minValue.getSelectionModel().select(Integer.toString(getMinOutputValue())); }
	else
	{ minValue.getSelectionModel().select(0); }//Auto select channel 1 for input }
	minValue.setId("" + getId());
	minValue.setOnAction(e -> HandleMinOutValueCombo(minValue));
	rowGrid.add(minValue,index,0);
	rowGrid.add(getRowLabel("Min Value"),index,1);
	HandleMinOutValueCombo(minValue);
	index++;


	ComboBox<String> maxValue = new ComboBox<String>();
	maxValue.getItems().addAll(vals);
	if (getMaxOutputValue() >= 0)
	{ maxValue.getSelectionModel().select(Integer.toString(getMaxOutputValue())); }
	else
	{ maxValue.getSelectionModel().select(127); }//Auto select channel 1 for input }
	maxValue.setId("" + getId());
	maxValue.setOnAction(e -> HandleMaxOutValueCombo(maxValue));
	rowGrid.add(maxValue,index,0);
	rowGrid.add(getRowLabel("Max Value"),index,1);
	HandleMaxOutValueCombo(maxValue);
	index++;

	String[] ccVals = new String[64];
	for (int i = 0; i < 64; i++){ ccVals[i] = Integer.toString(i + 1);}

	ComboBox<String> ccOut = new ComboBox<String>();
	ccOut.getItems().addAll(ccVals);
	ccOut.getItems().add("Pitch Bend");
	if (getOutputCCNum() >= 0)
	{ ccOut.getSelectionModel().select(Integer.toString(getOutputCCNum())); }
	else
	{ ccOut.getSelectionModel().select(12); }//Auto select CC 11 for arbitrary reasons
	ccOut.setId("" + getId());
	ccOut.setOnAction(e -> HandleOutCCCombo(ccOut));
	rowGrid.add(ccOut,index,0);
	rowGrid.add(getRowLabel("CC Output"),index,1);
	HandleOutCCCombo(ccOut);
	index++;


	//TODO make this field invisible when doing pitch bend
	//This is the value we set CC to on note off 
	ComboBox<String> noteOffValue = new ComboBox<String>();
	noteOffValue.getItems().addAll(vals);
	if (getNoteOffCCValue() >= 0)
	{ noteOffValue.getSelectionModel().select(Integer.toString(getNoteOffCCValue())); }
	else
	{ noteOffValue.getSelectionModel().select(0); }
	noteOffValue.setId("" + getId());
	noteOffValue.setOnAction(e -> HandleNoteOffValueCombo(noteOffValue));
	rowGrid.add(noteOffValue,index,0);
	rowGrid.add(getRowLabel("CC Val on NoteOff"),index,1);
	HandleNoteOffValueCombo(noteOffValue);
	index++;

	Button delBtn = new Button();
	delBtn.setText("Delete");
	delBtn.setId("" + getId());
	delBtn.setOnAction(e ->  p.gui.removeRow(rowGrid, Integer.parseInt(delBtn.getId()))); 
	rowGrid.add(delBtn,index,0);
	index++;

	rowHeight = (int)rowGrid.heightProperty().doubleValue(); //so we know how high these things are


	return rowGrid;

	//VstUtils.out("poly collection now has " + ((PolyTool)plugin).getPolyCollection().size());
    }


    private double rowHeight;

    private Label getRowLabel(String text)
    {
	Label label = new Label();
	label.setFont(new Font("Arial", 9));
	label.setText(text);
	return label;
    }

    public int getRowHeight()
    {
	return (int) rowHeight;
    }

    private void HandleLearnButton(Button learnBtn)
    {
	doLearnButton = true;
	Platform.runLater(new Runnable(){
	    @Override
	    public void run()
	    { learnBtn.setText("Play a key");
	    }
	});
    }

    private void HandleEnabledCheckbox(CheckBox cb)
    {
	Boolean checked = cb.selectedProperty().get();
	setEnabled(checked);
    }

    private void HandleInverseCheckbox(CheckBox cb)
    {
	Boolean checked = cb.selectedProperty().get();
	setInverse(cb.isSelected());
    }

    private void HandlePlayNotesCheckbox(CheckBox cb)
    {
	letNotesPlayThrough = cb.selectedProperty().get();
    }

    private void HandleInChannelCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	setInputChannel(Integer.parseInt(value));
    }

    private void HandleOutChannelCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	setOutputChannel(Integer.parseInt(value));
    }

    private void HandleMinOutValueCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	setMinOutputValue(Integer.parseInt(value));
    }

    private void HandleMaxOutValueCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	setMaxOutputValue(Integer.parseInt(value));
    }

    private void HandleOutCCCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	if (value == "Pitch Bend")
	{ setOutputCCNum(PolyTool.PITCH_BEND); }
	else
	{ setOutputCCNum(Integer.parseInt(value)); }
	setOutputCCNum(Integer.parseInt(value));
    }

    private void HandleNoteOffValueCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	setNoteOffCCValue(Integer.parseInt(value)); 
    }

    private void HandleRowName(TextField cb)
    {
	String value = cb.getText();
	setName(value);
    }

    private void HandleNoteModeSwitch(boolean isSingleNoteMode, int rowId, Button learnButton, CheckBox cbPlayNotes, Label labelNotesPlay)
    {
	setUseAllKeys(!isSingleNoteMode);
	setIsDoingAveraging(!isSingleNoteMode); //default this to true when using multi-key mode.  

	//'Row' updated, the rest is just UI in this function
	learnButton.setVisible(isSingleNoteMode);
	cbPlayNotes.setVisible(!isSingleNoteMode);
	labelNotesPlay.setVisible(!isSingleNoteMode);


	if (isSingleNoteMode) 
	{learnButton.setMinWidth(77);
	labelNotesPlay.setMaxWidth(1);
	cbPlayNotes.setMaxWidth(1);
	learnButton.setMaxWidth(200); } //The 200 is just a random large-ish value
	else
	{ learnButton.setMaxWidth(1); 
	labelNotesPlay.setMaxWidth(200);
	cbPlayNotes.setMaxWidth(200);
	learnButton.setMinWidth(1);}
    }


 
    //This is called when a learn button has been pressed and the next midi events come in
    //Find the NOTE ON (if there is one), assign it to the Learned Button, and let all other messages be returned (to continue their flow)
    private VSTEvents doLearnButton(VSTEvents events, Button thisLearnButton)
    {
	List<VSTEvent> newEvents = new ArrayList<VSTEvent>();
	Boolean foundNoteOn = false;
	for (int i = 0; i < events.getNumEvents(); i++)
	{
	    VSTEvent event = events.getEvents()[i];
	    PolyRow row = this;
	    if (event.getType() == VSTEvent.VST_EVENT_MIDI_TYPE)
	    {
		byte[] msg_data = ((VSTMidiEvent)event).getData();
		int status = MidiUtils.getStatusWithoutChannelByteFromMidiByteArray(msg_data);

		if ((status == ShortMessage.NOTE_ON) && (foundNoteOn == false))
		{
		    //We have our note
		    try
		    {
			learnBtn = null;
			Platform.runLater(new Runnable(){
			    @Override
			    public void run()
			    { 
				try
				{
				    Note n = MidiUtils.getNote(MidiUtils.getShortMessage((VSTMidiEvent)event));
				    thisLearnButton.setText(n.getNoteNamePlusOctave());
                                    doLearnButton = false; //Because we've got the note

				    row.setNote(n);
				} catch (Exception e)
				{ e.printStackTrace(); }
			    }
			});
                        foundNoteOn = true; //So that we don't use other note ons

		    } catch (Exception e)
		    {
			VstUtils.out(e.getMessage()); 
		    }
		}
		else //Add to returned events this event since it is NOT the Note On
		{
		    newEvents.add(events.getEvents()[i]);
		}
	    }
	}
	return VstUtils.convertToVSTEvents(newEvents);
    }
    


}
