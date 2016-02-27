//Adapted from https://github.com/thbar/opaz-plugdk/blob/master/plugins/Stuff/MIDIVSTPluginSkeleton.java    

//This is an example "hello world" midi VST that only echoes incoming MIDI data

package rjm.vst.midi.polytool;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import javafx.application.Platform;
import javafx.scene.layout.HBox;
import jvst.wrapper.VSTPluginAdapter;
import jvst.wrapper.valueobjects.VSTEvent;
import jvst.wrapper.valueobjects.VSTEvents;
import jvst.wrapper.valueobjects.VSTMidiEvent;
import rjm.midi.tools.MidiUtils;
import rjm.midi.tools.Note;
import rjm.vst.javafx.UIUtils;
import rjm.vst.tools.VstUtils;

public class PolyTool extends VSTPluginAdapter {

    public final static int PARAM_ID_ROWS = 0;
    // public final static int PARAM_ID_THRU = 1;

    PolyToolGui gui = null;
    
    PolyRowCollection polys = null;

    public static int NUM_PARAMS = 1;

    public static String[] PARAM_NAMES = new String[] { "Rows", "Midi Thru" };
    public static String[] PARAM_LABELS = new String[] { "VolumeLabel", "EnabledLbl" };
    public static float[] PARAM_PRINT_MUL = new float[] { 0, 1 };

    // Some default programs
    private float[][] programs = new float[][] { { 0.0f, 1 } };
    private int currentProgram = 0;

    public PolyTool(long wrapper)
    {
	super(wrapper);

	// setProgramHasChunks(true);
	currentProgram = 0;
	
	polys = new PolyRowCollection();

	// Apparently this line is instrumental in letting DAW know that Chunks
	// are its way of doing business
	// (otherwise I've seen DAWs not ask for Chunks)
	this.programsAreChunks(true);

	// for compatibility, we say that we have 2 ins and outs
	this.setNumInputs(2);
	this.setNumOutputs(2);

	this.canProcessReplacing(true);// mandatory for vst 2.4!
	this.setUniqueID('k' << 24 | 'R' << 16 | 'u' << 8 | 'b');// jRub

	VstUtils.out("LOADING");
	VstUtils.out("Host can receive vst midi?: " + this.canHostDo(CANDO_HOST_RECEIVE_VST_MIDI_EVENT));
    }

    public void resume()
    {
	// Need to call this so the host knows we want MIDI events.
	wantEvents(1);
    }
    
    public PolyRowCollection getPolyCollection()
    {
	return this.polys;
    }

    public int canDo(String feature)
    {
	// the host asks us here what we are able to do
	int ret = CANDO_NO;

	if (feature.equals(CANDO_PLUG_SEND_VST_MIDI_EVENT))
	    ret = CANDO_YES;
	if (feature.equals(CANDO_PLUG_SEND_VST_EVENTS))
	    ret = CANDO_YES;

	if (feature.equals(CANDO_PLUG_RECEIVE_VST_EVENTS))
	    ret = CANDO_YES;
	if (feature.equals(CANDO_PLUG_RECEIVE_VST_MIDI_EVENT))
	    ret = CANDO_YES;
	

	// if (feature.equals(CANDO_PLUG_MIDI_PROGRAM_NAMES)) //TODO: delete ???
	// ret = CANDO_YES;

	VstUtils.out("Host asked canDo: " + feature + " we replied: " + ret);
	return ret;
    }

    public String getProductString()
    {
	return "PolyTool";
    }

    public String getEffectName()
    {
	return "RJM PolyTool";
    }

    public String getProgramNameIndexed(int category, int index)
    {
	return "prog categ=" + category + ", idx=" + index;
    }

    public String getVendorString()
    {
	return "http://jvstwrapper.sourceforge.net/";
    }

    public boolean setBypass(boolean value)
    {
	// do not support soft bypass!
	return false;
    }

    public boolean string2Parameter(int index, String value)
    {
	try
	{
	    if (value != null)
		this.setParameter(index, Float.parseFloat(value));
	    return true;
	} catch (Exception e)
	{ // ignore
	    return false;
	}
    }

    public int getNumParams()
    {
	// VstUtils.out("CALLING " + NUM_PARAMS +
	// Thread.currentThread().getStackTrace()[1].getMethodName());
	return NUM_PARAMS;
    }

    public int getNumPrograms()
    {
	return programs.length;
    }

    public float getParameter(int index)
    {
	if (index < programs[currentProgram].length)
	{
	    VstUtils.out("CALLING " + programs[currentProgram][index] + Thread.currentThread().getStackTrace()[1].getMethodName());
	    // UIUtils.showAlert("Returning " +
	    // programs[currentProgram][index]);
	    return programs[currentProgram][index];
	}
	// UIUtils.showAlert("Returning 0.0f");
	return 0.0f;
    }

    // Obviously this one only works for standard midi items
    public int getParameterMulValue(int index)
    {
	if (index < programs[currentProgram].length)
	{
	    return (int) (PARAM_PRINT_MUL[index] * programs[currentProgram][index]);
	}
	return 0;
    }

    public String getParameterDisplay(int index)
    {
	try
	{
	    if (index < programs[currentProgram].length)
	    {
		return "" + (int) (PARAM_PRINT_MUL[index] * programs[currentProgram][index]);
	    }
	} catch (Exception e)
	{
	}
	return "0";
    }

    public String getParameterLabel(int index)
    {
	try
	{
	    if (index < PARAM_LABELS.length)
		return PARAM_LABELS[index];
	} catch (Exception e)
	{
	}
	return "";
    }

    public String getParameterName(int index)
    {
	try
	{
	    if (index < PARAM_NAMES.length)
	    {
		return PARAM_NAMES[index];
	    }
	} catch (Exception e)
	{
	}
	return "param: " + index;
    }

    public int getProgram()
    {
	return currentProgram;
    }

    public String getProgramName()
    {
	return "program " + currentProgram;
    }

    public void setParameter(int index, float value)
    {
	try
	{
	    programs[currentProgram][index] = value;
	    if (index == PARAM_ID_ROWS)
	    {
		// VstUtils.out("Volume set = false");
	    }
	} catch (Exception e)
	{
	}

    }

    public void setProgram(int index)
    {
	currentProgram = index;
    }

    public void setProgramName(String name)
    {
	// TODO: ignored
    }

    public int getPlugCategory()
    {
	// return PLUG_CATEG_EFFECT; //TODO: maybe return categ synth here ???
	return PLUG_CATEG_SYNTH;
    }

    // Generate / Process the sound!
    public void processReplacing(float[][] inputs, float[][] outputs, int sampleFrames)
    {
	// DO NOTHING HERE
    }

    public int getChunk(byte[][] data, boolean isPreset)
    {
	VstUtils.out("getChunk called");
	try
	{
	    //VstUtils.out("This.polys message: " + this.polys.getMessage());
	    String polysSerializedString = VstUtils.toString(this.polys);
	    //String polysSerializedString = VstUtils.toString(this.polys);
	    data[0] = polysSerializedString.getBytes();
	    VstUtils.out("getChunk successfully returning '" + new String(data[0]) + "'");
	    return data[0].length;
	} catch (IOException e)
	{
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    e.printStackTrace(pw);
	    String stackTrace = sw.toString(); // stack trace as a string	
	    data[0] = stackTrace.getBytes();
	    VstUtils.out("getChunk ERror returning '" + new String(data[0]) + "'");
	    return data[0].length;
	}
    }
    


    public final int setChunk(byte[] data, int byteSize, boolean isPreset)
    {
	VstUtils.out("CALLING " + Thread.currentThread().getStackTrace()[1].getMethodName());
	VstUtils.out("setChunk received " + new String(data));
	try
	{
	    //int rows = Integer.parseInt(new String(data).trim());
	    //setParameter(PARAM_ID_ROWS, rows);
	    //updateGUI();
	    String polysSerializedString = new String(data).trim();
	    this.polys = (PolyRowCollection)VstUtils.fromString(polysSerializedString);
	    updateGUI();
	    
	} catch (Exception e)
	{
	    UIUtils.showAlert("Error loading saved configuration from DAW: " + e.getMessage() + " " + e.getStackTrace() );
	}
	return 0;
    }
    
    
    protected void updateGUI()
    {
	// only access gui elemts if the gui was fully initialized
	// this is to prevent a threading issue on the mac that may cause a npe
	// because the sliders
	// arent there yet (the constructor of the plugin is called, when the
	// gui is not initialized yet)
	// for thread savety on the mac, never call gui stuff in the constructor
	// of the plugin
	// init the gui defaults always when the gui is loaded, not when the
	// plug is loaded.

	if (gui != null)
	{
	    gui.clearGuiRows();
	    VstUtils.out("OK so we got the chunk, now polys size is " + polys.size());
	    for (int i = 0; i < polys.size(); i++)
	    {
		gui.addGuiRow(polys.getRow(i));
	    }
	}

    }
    
    //This is called when a learn button has been pressed and the next midi events come in
    private VSTEvents doLearnButton(VSTEvents events)
    {
	//Find the NOTE ON (if there is one), assign it to the Learned Button, and let all other messages be returned (to continue their flow)

	List<VSTEvent> newEvents = new ArrayList<VSTEvent>();
	Boolean foundNoteOn = false;
	for (int i = 0; i < events.getNumEvents(); i++)
	{
	    VSTEvent event = events.getEvents()[i];
	    if (event.getType() == VSTEvent.VST_EVENT_MIDI_TYPE)
	    {
		byte[] msg_data = ((VSTMidiEvent)event).getData();
		int status = MidiUtils.getStatusWithoutChannelByteFromMidiByteArray(msg_data);

		if ((status == ShortMessage.NOTE_ON) && (foundNoteOn == false))
		{
		    //We have our note
		    try
		    {
			Platform.runLater(new Runnable(){
			    @Override
			    public void run()
			    { 
				try
				{
				    Note n = MidiUtils.getNote(MidiUtils.getShortMessage((VSTMidiEvent)event));
				    String message = n.getNoteNamePlusOctave();
				    //VstUtils.out("message is " + message);
				    gui.currentLearnButton.setText(message);
				    
				    PolyRow row = polys.getRowByRowId(gui.currentLearnButton.getId());
				    row.setNote(n);
				    polys.updateRow(row);
				    //VstUtils.out("Button not null... setting to null now.");
				    gui.currentLearnButton = null;

				} catch (Exception e)
				{
				    // TODO Auto-generated catch block
				    e.printStackTrace();
				}
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
    
    
    //private List<VSTEvent> convertPolyAftertouchToCCAndReturnOnlyNewCCEvents(int cc, int inputChannel, int outputChannel, List<VSTEvent> ev, int max, int min)
    private List<VSTEvent> convertPolyAftertouchToCCAndReturnOnlyNewCCEvents(List<VSTEvent> ev, PolyRow row)
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

		if (msg_channel == row.getInputChannel())
		{
		    //VstUtils.out("Channel is correct at " + msg_channel + " and status is " + status + " though we're looking for a " + ShortMessage.POLY_PRESSURE );

		    boolean foundPressureOrAftertouch = false;
		    int noteNum = ctrl_index;
		    
		    //VstUtils.out("Looking for poly pressure against note " + row.getNote().getMidiNoteNumber());
		    //VstUtils.out("Current is " + noteNum);
                    ctrl_index = row.getOutputCCNum();
		   
		    if ((status == ShortMessage.POLY_PRESSURE)&&(row.getNote().getMidiNoteNumber() == noteNum)) //Poly for the desired note...
		    {
			status = ShortMessage.CONTROL_CHANGE;
			//VstUtils.out("Found poly value " + ctrl_value);

			//First change the value to adhere to the max/min supplied to this funciton
			double val = ctrl_value; //Converting to double seems to have fixed one bug in testing
			double ratio = val / 127;
			int delta = row.getMaxOutputValue() - row.getMinOutputValue();
			int newval = (int)(delta * ratio) + row.getMinOutputValue();
			ctrl_value = newval; //This is the new value that will be output to CC

			try
			{
			    //Create new midi message
			    ShortMessage s = new ShortMessage(ShortMessage.CONTROL_CHANGE,  row.getOutputChannel() - 1, ctrl_index, ctrl_value);
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
		    else if ((status == ShortMessage.NOTE_OFF)&&(row.getNote().getMidiNoteNumber() == noteNum)) //Poly for the desired note...
		    {
			try
			{
			    ctrl_value = row.getNoteOffCCValue();
			    //Create new midi message
			    ShortMessage s = new ShortMessage(ShortMessage.CONTROL_CHANGE,  row.getOutputChannel() - 1, ctrl_index, ctrl_value);
			    //out("ShortMessage channel is " + s.getChannel() + " while output channel is " + outputChannel);
			    out("OK got a note off so sending CC value " + ctrl_value + " to CC " + ctrl_index);
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
    
    public List<VSTEvent> stripMessagesBasedOnNoteNum(List<VSTEvent> events, int stripChan, int stripNoteNum)
    {
	List<VSTEvent> cleanedEvents = new ArrayList<VSTEvent>();
	
	out("Considering " + events.size() + " to clean");

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

	out("returning " + cleanedEvents.size());

	return cleanedEvents;
    }


    // process MIDI
    public int processEvents(VSTEvents events)
    {
	List<VSTEvent> origEvents = VstUtils.cloneVSTEventsToList(events);
	if (gui != null) 
	{
	    if (gui.currentLearnButton != null) //"Learn" the note from Learn button
	    { events = doLearnButton(events); }

	    List<VSTEvent> newEvents = new ArrayList<VSTEvent>();

	    VstUtils.out("polys size is " + this.polys.size());

	    for (int i = 0; i < this.polys.size(); i++)
	    {
		out("Checking row" + i);
		PolyRow row = polys.getRow(i);
		out(row.getDebugString());
		out("Here we are");
		if (row.isGoodToGo()) //Instantiated and has usable values
		{
		    //Get new CC events that need to be added to output
		    List<VSTEvent> e = convertPolyAftertouchToCCAndReturnOnlyNewCCEvents(origEvents, row);
		    out("e has " + e.size());
		    newEvents.addAll(e); //Add new events to output
		}
		else
		{
		    out("Row was NOT good to go");
		}
	    }

	    for (int i = 0; i < this.polys.size(); i++)
	    {
		PolyRow row = polys.getRow(i);
		if (row.isGoodToGo()) //Instantiated and has usable values
		{
		    //Finally, after all rows, strip outgoing events of all incoming noteon/off events that were actioned
		    origEvents = stripMessagesBasedOnNoteNum(origEvents, row.getInputChannel(), row.getNote().getMidiNoteNumber());
		    out("origEvents now has " + origEvents.size() );
		}
	    }
		
	    //FINALLY now we just make sure the CC events are on the top of the collection and then lets ship it off
	    out("newevents has " + newEvents.size() + " and adding " + origEvents.size() + " rfomr origEvents");
	    out("newevents content before addition of orig:");
	    VstUtils.outputVstMidiEventsForDebugPurposes(VstUtils.convertToVSTEvents(newEvents));

	    newEvents.addAll(origEvents);
	    origEvents = newEvents;
	    
	    out("NEW EVENTS:");
            VstUtils.outputVstMidiEventsForDebugPurposes(VstUtils.convertToVSTEvents(newEvents));
	}

	this.sendVstEventsToHost(VstUtils.convertToVSTEvents(origEvents)); //Now shoot those MIDI events back out to the host
	return 0;
    }

   

    public static void out(String message)
    {

	//This function as of right now is a total hack and should only be used for debugging purposes and is known to slightly degrade plugin performance

	if (true) //This prevents this hack of a logging function from running unless manually enabled
	{ return; }

	try
	{
	    //HACK JMM
	    //TODO remove this thing
	    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(

		    new FileOutputStream("c:\\temp\\jwrapper_log.txt", true), "UTF-8"));

	    try
	    {
		writer.write(message + "\n");
		writer.close();
	    } catch (IOException e)
	    {
		try
		{
		    writer.write(message + "\n");
		    writer.close();
		} catch (IOException e1)
		{
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		}
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	} catch (UnsupportedEncodingException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (FileNotFoundException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
     
  


}
