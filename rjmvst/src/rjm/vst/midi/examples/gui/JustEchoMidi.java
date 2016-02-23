//Adapted from https://github.com/thbar/opaz-plugdk/blob/master/plugins/Stuff/MIDIVSTPluginSkeleton.java    

//This is an example "hello world" midi VST that only echoes incoming MIDI data

package rjm.vst.midi.examples.gui;



import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import jvst.wrapper.VSTPluginAdapter;
import jvst.wrapper.valueobjects.VSTEvents;
import rjm.vst.tools.VstUtils;

public class JustEchoMidi extends VSTPluginAdapter{

    public final static int PARAM_ID_VOLUME = 0;
    public final static int PARAM_ID_THRU = 1;

    public rjm.vst.midi.examples.gui.JustEchoMidiGui gui = null;

    private boolean volumeSet;

    public static int NUM_PARAMS = 1;

    public static String[] PARAM_NAMES = new String[] { "CH1 Output Volume", "Midi Thru"  };
    public static String[] PARAM_LABELS = new String[] { "VolumeLabel", "EnabledLbl" };
    public static float[] PARAM_PRINT_MUL = new float[] { 127, 1 };
    

    // Some default programs
    private float[][] programs = new float[][] { { 1.0f, 1 } };
    private int currentProgram = 0;

    public JustEchoMidi(long wrapper) {
	super(wrapper);
	
	volumeSet = false;
	
	currentProgram = 0;

	// for compatibility, we say that we have 2 ins and outs
	this.setNumInputs(2);
	this.setNumOutputs(2);

	this.canProcessReplacing(true);// mandatory for vst 2.4!
	this.setUniqueID('k' << 24 | 'R' << 16 | 'u' << 8 | 'b');// jRub

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
    
    
    protected void updateGUI() {
	//only access gui elemts if the gui was fully initialized
	//this is to prevent a threading issue on the mac that may cause a npe because the sliders 
	//arent there yet (the constructor of the plugin is called, when the gui is not initialized yet)
	//for thread savety on the mac, never call gui stuff in the constructor of the plugin
	//init the gui defaults always when the gui is loaded, not when the plug is loaded.
	
	if (	gui!=null && 
			gui.VolumeSlider!=null && 
			gui.VolumeText!=null) {
	    

	    gui.VolumeSlider.setValue((int)(this.getParameter(PARAM_ID_VOLUME) * 100F));
	    //out("Updating volume slider to " + (this.getParameter(PARAM_ID_VOLUME) * 100F));
	    gui.VolumeText.setText(this.getParameterDisplay(PARAM_ID_VOLUME)); 
	}
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
    
    
    //Obviously this one only works for standard midi items
    public int getParameterMulValue(int index)
    {
	if (index < programs[currentProgram].length) {
	    return (int)(PARAM_PRINT_MUL[index] * programs[currentProgram][index]);
	}
	return 0;
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
	if (index == PARAM_ID_VOLUME)
	{
	    VstUtils.out("Volume set = false");
	    volumeSet = false;
	}
	   
	updateGUI();
    }

    public void setProgram(int index) {
	currentProgram = index;
	updateGUI();
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
	
	if (!volumeSet)//Inject Volume CC message at the top of the VSTEvents set
	{
	    VstUtils.out("Setting volume to " + getParameterMulValue(JustEchoMidi.PARAM_ID_VOLUME));
                int channel = 1; //hard coded for demo simplicity
                int volumeCCNum = 07;  //This is standard MIDI
		try
		{
		    //Create new MIDI message with volume info
		    //Subtracting 1 from channel on next line since it does 0-15 instead of 1-16
		    ShortMessage s = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel - 1, volumeCCNum, getParameterMulValue(JustEchoMidi.PARAM_ID_VOLUME));
		    //Add new message to VSTEvents collection
		    ev = VstUtils.getVstEventsWithNewVstEventInserted(0, ev, VstUtils.createVstMidiEventFromShortMessage(s));
		    volumeSet = true;
		} catch (InvalidMidiDataException e1)
		{
		    // TODO Auto-generated catch block
		    VstUtils.out(e1.getMessage());
		    e1.printStackTrace();
		}
	}

	//Now get Param for whether MIDI THRU is turned on or not
	float param = this.getParameter(JustEchoMidi.PARAM_ID_THRU) ;
	//out("CB param is " + param);
	if (param > 0) //= 'true'
	{
	    this.sendVstEventsToHost(ev); //simply echo all incoming events
	}

	return 0;
    }



}

