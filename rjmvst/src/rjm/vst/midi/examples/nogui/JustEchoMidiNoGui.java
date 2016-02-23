//Adapted from https://github.com/thbar/opaz-plugdk/blob/master/plugins/Stuff/MIDIVSTPluginSkeleton.java    

//This is an example "hello world" midi VST that only echoes incoming MIDI data

package rjm.vst.midi.examples.nogui;

import java.lang.reflect.Modifier;
import javax.sound.midi.ShortMessage;

import jvst.wrapper.VSTPluginAdapter;
import jvst.wrapper.valueobjects.VSTEvent;
import jvst.wrapper.valueobjects.VSTEvents;
import jvst.wrapper.valueobjects.VSTMidiEvent;
import rjm.midi.tools.MidiUtils;
import rjm.vst.tools.VstUtils;

public class JustEchoMidiNoGui extends VSTPluginAdapter{


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

    public JustEchoMidiNoGui(long wrapper) {
	super(wrapper);

	currentProgram = 0;

	// for compatibility, we say that we have 2 ins and outs
	this.setNumInputs(2);
	this.setNumOutputs(2);

	this.canProcessReplacing(true);// mandatory for vst 2.4!
	this.setUniqueID('k' << 24 | 'R' << 16 | 'u' << 8 | 'b');// jRub

	log("Construktor INVOKED!");

	VstUtils.out("LOADING");
	VstUtils.out("Host can receive vst midi?: " + this.canHostDo(CANDO_HOST_RECEIVE_VST_MIDI_EVENT));
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


    // process MIDI
    public int processEvents(VSTEvents ev) {

	// for now, all incoming MIDI is echoed using the line below.  
	this.sendVstEventsToHost(ev); //simply echo all incoming events


	//Now just doing logging for debug purposes mainly (this logging can degrade performance)
	//Logging via the "out()" function, which itself is a hack, is enabled/disabled in the out() function code itself.

	for (int i = 0; i < ev.getEvents().length; i++)
	{
	    VSTEvent e = ev.getEvents()[i];

	    if( e.getType() == VSTEvent.VST_EVENT_MIDI_TYPE ) 
	    {

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

                ShortMessage s = new ShortMessage();

                VstUtils.out("\n");
                Class<ShortMessage> c = ShortMessage.class;
                for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                    int mod = f.getModifiers();
                    if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod)) {
                        try {
                            Integer code = (Integer)f.get(null);
                            if (status == code.intValue())
                            {
                                //Print the type of MIDI message we've received along with the status value for it
                                VstUtils.out(String.format("%s = %d", f.getName(), f.get(null)));
                            }
                        } catch (IllegalAccessException e2) {
                            VstUtils.out("ERROR doing the comparison thing");
                            e2.printStackTrace();
                        }
                    }
                }
                VstUtils.out("Channel: " + msg_channel);
                VstUtils.out("Status: " + msg_status);
                VstUtils.out("Value: " + ctrl_value);
                VstUtils.out("Index: " + ctrl_index);
	    }
	}
	return 0;
    }




}

