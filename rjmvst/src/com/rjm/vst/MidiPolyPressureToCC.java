//Adapted from https://github.com/thbar/opaz-plugdk/blob/master/plugins/Stuff/MIDIVSTPluginSkeleton.java    


package com.rjm.vst;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import jvst.wrapper.VSTPluginAdapter;
import jvst.wrapper.valueobjects.VSTEvent;
import jvst.wrapper.valueobjects.VSTEvents;
import jvst.wrapper.valueobjects.VSTMidiEvent;

public class MidiPolyPressureToCC extends VSTPluginAdapter{


    //cached instances --> avoid GC --> GOOD!
    VSTMidiEvent vme = new VSTMidiEvent();
    VSTEvents ves = new VSTEvents();


    public static int NUM_PARAMS = 1;
    public static String[] PARAM_NAMES = new String[] { "MIDI CC Value" };
    public static String[] PARAM_LABELS = new String[] { "CC Value" };
    public static float[] PARAM_PRINT_MUL = new float[] { 127 };

    // Some default programs
    private float[][] programs = new float[][] { { 0.0f } };
    private int currentProgram = 0;

    public MidiPolyPressureToCC(long wrapper) {
	super(wrapper);

	currentProgram = 0;

	// for compatibility, we say that we have 2 ins and outs
	this.setNumInputs(2);
	this.setNumOutputs(2);

	this.canProcessReplacing(true);// mandatory for vst 2.4!
	this.setUniqueID('k' << 24 | 'R' << 16 | 'u' << 8 | 'b');// jRub

	log("Construktor INVOKED!");

	out("LOADING MIDI TO CC PLUGIN");
	out("Host can receive vst midi?: " + this.canHostDo(CANDO_HOST_RECEIVE_VST_MIDI_EVENT));
    }

    public void resume() {
	//Need to call this so the host knows we want MIDI events.
	wantEvents(1);
    }

    public int canDo(String feature) {
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

	//if (feature.equals(CANDO_PLUG_MIDI_PROGRAM_NAMES)) //TODO: delete ???
	//	ret = CANDO_YES;

	log("Host asked canDo: " + feature + " we replied: " + ret);
	return ret;
    }

    public String getProductString() {
	return "product1";
    }

    public String getEffectName() {
	return "RJM Midi Echo Vst";
    }

    public String getProgramNameIndexed(int category, int index) {
	return "prog categ=" + category + ", idx=" + index;
    }

    public String getVendorString() {
	return "http://jvstwrapper.sourceforge.net/";
    }

    public boolean setBypass(boolean value) {
	// do not support soft bypass!
	return false;
    }

    public boolean string2Parameter(int index, String value) {
	try {
	    if (value != null) this.setParameter(index, Float.parseFloat(value));
	    return true;
	} catch (Exception e) { // ignore
	    return false;
	}
    }

    public int getNumParams() {
	return NUM_PARAMS;
    }

    public int getNumPrograms() {
	return programs.length;
    }

    public float getParameter(int index) {
	if (index < programs[currentProgram].length)
	    return programs[currentProgram][index];
	return 0.0f;
    }

    public String getParameterDisplay(int index) {
	if (index < programs[currentProgram].length) {
	    return "" + (int) (PARAM_PRINT_MUL[index] * programs[currentProgram][index]);
	}
	return "0";
    }

    public String getParameterLabel(int index) {
	if (index < PARAM_LABELS.length) return PARAM_LABELS[index];
	return "";
    }

    public String getParameterName(int index) {
	if (index < PARAM_NAMES.length) return PARAM_NAMES[index];
	return "param: " + index;
    }

    public int getProgram() {
	return currentProgram;
    }

    public String getProgramName() {
	return "program " + currentProgram;
    }

    public void setParameter(int index, float value) {
	programs[currentProgram][index] = value;
    }

    public void setProgram(int index) {
	currentProgram = index;
    }

    public void setProgramName(String name) {
	// TODO: ignored
    }

    public int getPlugCategory() {
	log("getPlugCategory");
	return PLUG_CATEG_EFFECT;  //TODO: maybe return categ synth here ???
	//return PLUG_CATEG_SYNTH;
    }


    // Generate / Process the sound!
    public void processReplacing(float[][] inputs, float[][] outputs, int sampleFrames) {
	//DO NOTHING HERE
    }

    /*
     * 
     * Message type = status = 128 for note off etc



Value = the value 0-127 when using POLY
Index = the value 0-127 when using PRESSURE
Status = first byte


What must change for Poly->CC
"the value" goes to Value
Index = CC #

OK so also, the "status" int isn't consistent between channel changes... 

     */

    private VSTEvents convertPolyAftertouchAndChannelPressureToCCAndTrashOtherData(int cc, int inputChannel, int outputChannel, VSTEvents ev, int max, int min)
    {

	List<VSTEvent> newEvs = new ArrayList<VSTEvent>();
	if (ev.getNumEvents() > 0)
	{
	    out("Processing ev set which includes " + ev.getEvents().length + " items");
	}

	for (int i = 0; i < ev.getEvents().length; i++)
	{
	    VSTEvent e = ev.getEvents()[i];

	    if( e.getType() == VSTEvent.VST_EVENT_MIDI_TYPE ) 
	    {
		//out("Considering midi event...");

		byte[] msg_data = ((VSTMidiEvent)e).getData();

		int ctrl_index, ctrl_value, msg_status, msg_channel;
		msg_status = ( msg_data[ 0 ] & 0xF0 ) >> 4;
		if( msg_status == 0xF ) {
		    /* Ignore system messages.*/
		    //return;
		}
		msg_channel = ( msg_data[ 0 ] & 0xF ) + 1;

		ctrl_index = msg_data[ 1 ] & 0x7F;
		ctrl_value = msg_data[ 2 ] & 0x7F;

		/* switch msg_status
                                    case 0x8: /* Note off.*/
		//case 0x9: /* Note on.*/
		//case 0xB: /* Control change.*/
		//case 0xC: /* Program change.*/
		//case 0xE: /* Pitch wheel.*/


		int status = (int) (e.getData()[0] & 0xFF) - msg_channel + 1; //different but related to msg_status

		out("Status for incoming message is " + status);
		//out("Channel is supposedly " + msg_channel);

		if (msg_channel == inputChannel)
		{
		    //out("Channel is correct at " + inputChannel + " and status is " + status + " though we're looking for a " + ShortMessage.POLY_PRESSURE + " or a " + ShortMessage.CONTROL_CHANGE);

		    boolean foundPressureOrAftertouch = false;
		    if (status == ShortMessage.CONTROL_CHANGE)
		    {
			try
			{
			    //Set correct output channel and just pass along the message
			    if (ctrl_index == 1) {ctrl_index = 11;}
			    ShortMessage s = new ShortMessage(ShortMessage.CONTROL_CHANGE, outputChannel - 1, ctrl_index, ctrl_value);
			    e.setData(s.getMessage());
			    newEvs.add(e);
			} catch (InvalidMidiDataException e1)
			{
			    // TODO Auto-generated catch block
			    e1.printStackTrace();
			}
		    }
		    if (status == ShortMessage.POLY_PRESSURE)
		    {
			//ctrl_value = ctrl_value;
			ctrl_index = cc;
			status = ShortMessage.CONTROL_CHANGE;
			out("Found poly value " + ctrl_value);
			foundPressureOrAftertouch = true;
		    }
		    else if (status == ShortMessage.CHANNEL_PRESSURE)
		    {
			ctrl_value = ctrl_index;
			ctrl_index = cc;
			status = ShortMessage.CONTROL_CHANGE;
			out("Found channel pressure value " + ctrl_value);
			foundPressureOrAftertouch = true;
		    }
		    else {out("Status is " + status + " so ignoring...");}


		    if (foundPressureOrAftertouch)
		    {
			int val = ctrl_value;
			double ratio = val / 127;
			int delta = max - min;
			int newval = (int)(delta * ratio + min);
			ctrl_value = newval;
			//int data1 = (int) (e.getData()[1] & 0xFF);
			//int data2 = (int) (e.getData()[2] & 0xFF);
			try
			{
			    //Create new midi message
			    //ShortMessage s = new ShortMessage(ShortMessage.CONTROL_CHANGE, outputChannel, data1, data2);
			    out("Output channel should be set to " + outputChannel + " but will decrement by 1 and try that.... ");
			    ShortMessage s = new ShortMessage(ShortMessage.CONTROL_CHANGE,  outputChannel - 1, ctrl_index, ctrl_value);
			    //out("ShortMessage channel is " + s.getChannel() + " while output channel is " + outputChannel);
			    VSTMidiEvent newEvent = new VSTMidiEvent();
			    newEvent.setData(s.getMessage());

			    out("newEvent channel is " + (newEvent.getData()[0] & 0xF));
			    VSTEvent event = new VSTEvent();
			    event = newEvent;
			    event.setType(VSTEvent.VST_EVENT_MIDI_TYPE); //Apparently this is needed...
			    newEvs.add(event);
			    //out("Added new event to return list, which now has " + newEvs.size() + " elements");
			} catch (InvalidMidiDataException e1)
			{
			    // TODO Auto-generated catch block
			    e1.printStackTrace();
			    out(e1.getStackTrace().toString());
			}
		    }
		}
		else //Ignore i.e. simply pass through the message without modification
		{ newEvs.add(e); }

	    }
	    else //Ignore i.e. simply pass through the message without modification
	    { newEvs.add(e); }
	}

	out("newEvs has " + newEvs.size() + " elements");

	VSTEvent[] newVstEvents = new VSTEvent[newEvs.size()];
	for (int i = 0; i < newVstEvents.length; i++)
	{
	    newVstEvents[i] = newEvs.get(i);
	}
	//newVSTEvents.setEvents(Arrays.asList(newEvs).toArray(new VSTEvent[newEvs.size()]));
	//newVSTEvents.setEvents(Arrays.copyOf(newEvs.toArray(), newEvs.size(), VSTEvent[].class));
	out("New events has " + newVstEvents.length + " items");

	VSTEvents eventsOut = new VSTEvents();
	eventsOut.setEvents(newVstEvents);
	eventsOut.setNumEvents(newVstEvents.length); //I hate this is needed but I don't want to modify legacy jvstwrapper code just yet

	out("Events out has " + eventsOut.getEvents().length + " items");
	return eventsOut;
    }


    // process MIDI
    public int processEvents(VSTEvents ev) {

	// TODO: midi impl. here
	// for now, all incoming MIDI is echoed.  Also code has been added below to log various MIDI information if logging is enabled 


	VSTEvents newEvents = new VSTEvents();
	if (ev.getNumEvents() > 0)
	{
	    out("\n\n********************** Starting logic");
	    out("BEFORE:");
	    outputVstMidiEventsForDebug(ev);
	    newEvents = convertPolyAftertouchAndChannelPressureToCCAndTrashOtherData(1, 2, 1, ev, 80,0);//11
	    out("\nAFTER (there are " + newEvents.getNumEvents() + " Events:");
	    outputVstMidiEventsForDebug(newEvents);
	    out("NOW SENDING EVENTS FROMonarAFTER LIST");
	    //this.sendVstEventsToHost(ev); //simply echo all incoming events
	    this.sendVstEventsToHost(newEvents); //simply echo all incoming events
	    out("*******END***********");
	}


	return 0;
    }


    public void outputVstMidiEventsForDebug(VSTEvents ev)
    {
	//Now just doing logging for debug purposes mainly

	out("Looks like there are " + ev.getNumEvents() + " events to output here...");

	for (int i = 0; i < ev.getEvents().length; i++)
	{
	    VSTEvent e = ev.getEvents()[i];

	    if( e.getType() == VSTEvent.VST_EVENT_MIDI_TYPE ) 
	    {
		byte[] msg_data = ((VSTMidiEvent)e).getData();

		int ctrl_index, ctrl_value, msg_status, msg_channel;
		msg_status = ( msg_data[ 0 ] & 0xF0 ) >> 4;
		if( msg_status == 0xF ) {
		    /* Ignore system messages.*/
		    //return;
		}
		msg_channel = ( msg_data[ 0 ] & 0xF ) + 1;

		ctrl_index = msg_data[ 1 ] & 0x7F;
		ctrl_value = msg_data[ 2 ] & 0x7F;

		/*
	                        case 0x8: /* Note off.*/
		//case 0x9: /* Note on.*/
		//case 0xB: /* Control change.*/
		//case 0xC: /* Program change.*/
		//case 0xE: /* Pitch wheel.*/

		int status = (int) (e.getData()[0] & 0xFF) - msg_channel + 1; //different but related to msg_status

		out("Status for incoming message is " + status);

		ShortMessage s = new ShortMessage();

		out("\n");
		Class<ShortMessage> c = ShortMessage.class;
		for (java.lang.reflect.Field f : c.getDeclaredFields()) {
		    int mod = f.getModifiers();
		    if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod)) {
			try {
			    //System.out.printf("%s = %d%n", f.getName(), f.get(null));
			    Integer code = (Integer)f.get(null);
			    if (status == code.intValue())
			    {
				//Print the type of message we've received
				out(String.format("%s = %d", f.getName(), f.get(null)));
			    }
			} catch (IllegalAccessException e2) {
			    out("ERROR doing the comparison thing");
			    e2.printStackTrace();
			}
		    }
		}
		out("Channel: " + msg_channel);
		out("Status: " + msg_status);
		out("Value: " + ctrl_value);
		out("Index: " + ctrl_index);
	    }
	    else {out("Not midi?");}
	}	
    }




    public void out(String message)
    {

	//This function as of right now is a total hack and should only be used for debugging purposes and is known to degrade even midi-only/non-sound plugin performance

	if (true) //This prevents this hack of a logging function from running unless manually enabled
	{ return; }

	try
	{
	    //HACK JMM
	    //TODO remove this thing
	    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(

		    new FileOutputStream("c:\\program files\\common files\\vst3\\jwrapper\\jwrapper_log.txt", true), "UTF-8"));

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

