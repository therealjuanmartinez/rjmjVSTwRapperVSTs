
//Reads input MIDI data, taking poly aftertouch and applying to note on velocity for note-on messages that match the note associated with poly pressure.


//As of this writing everything is hard-coded... need to pull into a UI or something similar....





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

import com.rjm.midi.MidiUtils;
import com.rjm.midi.Note;

import jvst.wrapper.VSTPluginAdapter;
import jvst.wrapper.valueobjects.VSTEvent;
import jvst.wrapper.valueobjects.VSTEvents;
import jvst.wrapper.valueobjects.VSTMidiEvent;

public class MidiPolyAftertouchToNoteOnVelocities extends VSTPluginAdapter{


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
    
    private int currentVelocity;
    
    private List<Note> onNotes;

    public MidiPolyAftertouchToNoteOnVelocities(long wrapper) {
	super(wrapper);

	currentProgram = 0;
	currentVelocity = 0;
	
	onNotes = new ArrayList<Note>();

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
    
    private void updateVelocityArray(Note n)
    {
	boolean foundMatch = false;
	for (int i = 0; i < onNotes.size(); i++)
	{
	    if (n.getMidiNoteNumber() == onNotes.get(i).getMidiNoteNumber()) //Note already present
	    {
		onNotes.get(i).setVelocity(n.getVelocity());
		foundMatch = true;
	    }
	}
	if (!foundMatch)
	{
	    onNotes.add(n);
	}
	return;
    }
    
    private int getCurrentVelocity(int noteNumber)
    {
	boolean foundMatch = false;
	int vel = -1;
	for (int i = 0; i < onNotes.size(); i++)
	{
	    if (noteNumber == onNotes.get(i).getMidiNoteNumber()) //Note already present
	    {
		vel = onNotes.get(i).getVelocity();
	    }
	}
	return vel;
    }
    
    private void removeNoteFromVelocityArray(Note n)
    {
	boolean done = false;
	boolean foundMatch = false;
	int removeIndex = -1;
	
	while (!done)
	{
                for (int i = 0; i < onNotes.size(); i++)
                {
                    if (n.getMidiNoteNumber() == onNotes.get(i).getMidiNoteNumber()) //Note already present
                    {
                        //onNotes.get(i).setVelocity(n.getVelocity());
                        removeIndex = i;
                    }
                }
                if (removeIndex > -1)
                {
                        onNotes.remove(removeIndex);
                        removeIndex = -1;
                }
                else { done = true; }
	}
    }

    private VSTEvents applyPolyAftertouchToNoteOnVelocities(VSTEvents inputEvents, int inputChannelForPressureAftertouch, int outputChannel, int maxOutputVel, int minOutputVel) throws Exception
    {

	List<VSTEvent> outputEvents = new ArrayList<VSTEvent>();
	

	for (int i = 0; i < inputEvents.getEvents().length; i++)
	{
	    VSTEvent e = inputEvents.getEvents()[i];

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

		/* switch msg_status
                                    case 0x8: /* Note off.*/
		//case 0x9: /* Note on.*/
		//case 0xB: /* Control change.*/
		//case 0xC: /* Program change.*/
		//case 0xE: /* Pitch wheel.*/

		int status = (int) (e.getData()[0] & 0xFF) - msg_channel + 1; //different but related to msg_status

		out("Status for incoming message is " + status);
		//out("Channel is supposedly " + msg_channel);

		if (msg_channel == inputChannelForPressureAftertouch)
		{
		    //out("Channel is correct at " + inputChannel + " and status is " + status + " though we're looking for a " + ShortMessage.POLY_PRESSURE + " or a " + ShortMessage.CONTROL_CHANGE);
		    
		    boolean foundPressureOrAftertouch = false;
		    
		    if ((status == ShortMessage.NOTE_OFF)||(status == ShortMessage.NOTE_ON)) 
		    {
			int noteNumber = ctrl_index;
			int velocity = ctrl_value;
			
			if ((velocity == 0)||(status == ShortMessage.NOTE_OFF))
			{
                                removeNoteFromVelocityArray(new Note(noteNumber));
			}
		    }
		    
		    if (status == ShortMessage.POLY_PRESSURE)
		    {
			int pressure = ctrl_value;
			int noteNumber = ctrl_index;
			//TODO add note + pressure to private array
			//ctrl_value = ctrl_value;
			//ctrl_index = cc;
			status = ShortMessage.CONTROL_CHANGE;
			out("Found poly value " + ctrl_value);
			foundPressureOrAftertouch = true;

			if ((minOutputVel > 0) || (maxOutputVel < 127))
			{
                                ctrl_value = MidiUtils.getScaledValue(ctrl_value, maxOutputVel, minOutputVel);
			}
			updateVelocityArray(new Note(noteNumber, pressure)); //That's right, putting "pressure" as the Note's velocity
		    }
		}
		else 
		{ 
		    if ((status == ShortMessage.NOTE_OFF)||(status == ShortMessage.NOTE_ON)) //i.e. these are note off/on messages that we have chosen not to ignore
		    {
			//TODO apply current velocity if > 0 & ignore/trash any note ons that would get 0 velocity (but keep note-offs b/c that seems safe)
			//ShortMessage.
			
			int noteNumber = ctrl_index;
			int velocity = ctrl_value;

			if ((velocity > 0)&&(status == ShortMessage.NOTE_ON)) //Otherwise it is technically a note-off from the device generating note on/off messages
			{
			    //Apply the new velocity from the velocity array
			    velocity = getCurrentVelocity(noteNumber);
			    if (velocity < 1)
			    {
				velocity = 80; //TODO find better non-hardcoded method
			    }
			    out("Velocity is " + velocity);
			    ShortMessage s = new ShortMessage(ShortMessage.NOTE_ON,  outputChannel - 1, noteNumber, velocity);
			    //out("ShortMessage channel is " + s.getChannel() + " while output channel is " + outputChannel);
			    VSTMidiEvent newEvent = new VSTMidiEvent();
			    newEvent.setData(s.getMessage());
                            VSTEvent v = new VSTEvent();
                            v = newEvent;
                            v.setType(VSTEvent.VST_EVENT_MIDI_TYPE); //Apparently this is needed...
                            outputEvents.add(v);
			}
                        else{ outputEvents.add(e); } //Append the note off

		    }
		    else{ outputEvents.add(e); } //Whatever this is, pass it along also
                }
	    }
	    else //Ignore i.e. simply pass through the message without modification
	    { outputEvents.add(e); }
	}

	//out("outputEvents has " + outputEvents.size() + " elements");
	
	//Convert output to VSTEvents Object (would have used VSTEvents before now, but it doesn't support the adding of events one by one)
	return Utils.convertToVSTEvents(outputEvents);
    }
    
   
    // process MIDI
    public int processEvents(VSTEvents ev) {

	// TODO: midi impl. here
	// for now, all incoming MIDI is echoed.  Also code has been added below to log various MIDI information if logging is enabled 


	VSTEvents newEvents = new VSTEvents();
	if (ev.getNumEvents() > 0)
	{
	    out("\n\n********************** Starting aftertouch/velocity logic");
	    out("BEFORE:");
	    try{ outputVstMidiEventsForDebug(ev);} catch (Exception e ){}
	    try
	    {
		newEvents = applyPolyAftertouchToNoteOnVelocities(ev, 2, 1, 127,0);
	    } catch (Exception e)
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }//11
	    out("\nAFTER (there are " + newEvents.getNumEvents() + " Events:");

	    try{ outputVstMidiEventsForDebug(newEvents);} catch (Exception e ){}

	    out("NOW SENDING EVENTS FROM Sonar AFTER LIST");
	    this.sendVstEventsToHost(newEvents); 
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

