//Adapted from https://github.com/thbar/opaz-plugdk/blob/master/plugins/Stuff/MIDIVSTPluginSkeleton.java    

//This is an example "hello world" midi VST that only echoes incoming MIDI data

package rjm.vst.midi.polytool;



import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import jvst.wrapper.VSTPluginAdapter;
import jvst.wrapper.valueobjects.VSTEvents;
import rjm.vst.javafx.UIUtils;
import rjm.vst.tools.VstUtils;

public class PolyTool extends VSTPluginAdapter{

    public final static int PARAM_ID_ROWS = 0;
    //public final static int PARAM_ID_THRU = 1;

    public rjm.vst.midi.examples.gui.swing.JustEchoMidiGui gui = null;


    public static int NUM_PARAMS = 1;

    public static String[] PARAM_NAMES = new String[] { "Rows", "Midi Thru"  };
    public static String[] PARAM_LABELS = new String[] { "VolumeLabel", "EnabledLbl" };
    public static float[] PARAM_PRINT_MUL = new float[] { 0, 1 };
    

    // Some default programs
    private float[][] programs = new float[][] { { 1.0f, 1 } };
    private int currentProgram = 0;

    public PolyTool(long wrapper) {
	super(wrapper);
	
	//setProgramHasChunks(true);
	currentProgram = 0;
	this.programsAreChunks(true);

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

	VstUtils.out("Host asked canDo: " + feature + " we replied: " + ret);
	return ret;
    }
    
    
  

    public String getProductString() {
	return "PolyTool";
    }

    public String getEffectName() {
	return "RJM PolyTool";
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
	VstUtils.out("CALLING " + NUM_PARAMS + Thread.currentThread().getStackTrace()[1].getMethodName());
	return NUM_PARAMS;
    }

    public int getNumPrograms() {
	VstUtils.out("CALLING " + programs.length + " " + Thread.currentThread().getStackTrace()[1].getMethodName());
	return programs.length;
    }

    public float getParameter(int index) {
	if (index < programs[currentProgram].length)
	{
            VstUtils.out("CALLING " + programs[currentProgram][index] + Thread.currentThread().getStackTrace()[1].getMethodName());
	    //UIUtils.showAlert("Returning " + programs[currentProgram][index]);
	    return programs[currentProgram][index];
	}
        VstUtils.out("CALLING 0.0f" + Thread.currentThread().getStackTrace()[1].getMethodName());
        //UIUtils.showAlert("Returning 0.0f");
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
	try
	{
	VstUtils.out("CALLING " + Thread.currentThread().getStackTrace()[1].getMethodName());
	if (index < programs[currentProgram].length) {
	    return "" + (int) (PARAM_PRINT_MUL[index] * programs[currentProgram][index]);
	}
	}
	catch (Exception e)
	{
	    VstUtils.out(e.getMessage());
	}
	return "0";
    }

    public String getParameterLabel(int index) {
	try{
	VstUtils.out("CALLING " + Thread.currentThread().getStackTrace()[1].getMethodName());
	if (index < PARAM_LABELS.length) return PARAM_LABELS[index];
	}
	catch (Exception e)
	{
	    VstUtils.out(e.getMessage());
	}
	return "";
    }

    public String getParameterName(int index) {
	try{
	    if (index < PARAM_NAMES.length) 
	    {
		VstUtils.out("CALLING " + PARAM_NAMES[index] + Thread.currentThread().getStackTrace()[1].getMethodName());
		return PARAM_NAMES[index];
	    }
	}
	catch (Exception e)
	{
	    VstUtils.out(e.getMessage());
	}
	VstUtils.out("CALLING param: " + index + " " +  Thread.currentThread().getStackTrace()[1].getMethodName());
	return "param: " + index;
    }

    public int getProgram() {
	VstUtils.out("CALLING " + currentProgram + " " + Thread.currentThread().getStackTrace()[1].getMethodName());
	return currentProgram;
    }

    public String getProgramName() {
	VstUtils.out("CALLING returning " + "program " + currentProgram + " " + Thread.currentThread().getStackTrace()[1].getMethodName());
	return "program " + currentProgram;
    }

    public void setParameter(int index, float value) {
	try
	{

	    VstUtils.out("CALLING index,value " + index + "," + value + " " + Thread.currentThread().getStackTrace()[1].getMethodName());
	    //UIUtils.showAlert("Someone wants to set parameter index " + index + " to " + value);
	    programs[currentProgram][index] = value;
	    if (index == PARAM_ID_ROWS)
	    {
		//VstUtils.out("Volume set = false");
	    }
	}
	catch (Exception e)
	{
	    VstUtils.out(e.getMessage());
	}

    }

    public void setProgram(int index) {
	currentProgram = index;
	VstUtils.out("CALLING with " + index + " " + Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    public void setProgramName(String name) {
	// TODO: ignored
	VstUtils.out("CALLING " + Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    public int getPlugCategory() {
	VstUtils.out("CALLING " + Thread.currentThread().getStackTrace()[1].getMethodName());
	log("getPlugCategory");
	//return PLUG_CATEG_EFFECT;  //TODO: maybe return categ synth here ???
	return PLUG_CATEG_SYNTH;
    }


    // Generate / Process the sound!
    public void processReplacing(float[][] inputs, float[][] outputs, int sampleFrames) {
	//DO NOTHING HERE
    }
    
    
    public int getChunk(byte[][] data, boolean isPreset) {
	VstUtils.out("CALLING " + Thread.currentThread().getStackTrace()[1].getMethodName());
        String stringEnsemble = "what ever you want to save in the host";
        data[0] = stringEnsemble.getBytes();
        return data[0].length;
  }

    /**
     * Recrée un Ensemble sur base d'un byte[] passé par le host
     * @param data byte[]
     * @param byteSize int
     * @param isPreset boolean
     * @return int
     */
    public final int setChunk(byte[] data, int byteSize, boolean isPreset) {
	VstUtils.out("CALLING " + Thread.currentThread().getStackTrace()[1].getMethodName());
	VstUtils.out("setChunk received " + new String(data));
	return 0;
    }


    // process MIDI
    public int processEvents(VSTEvents ev) {
	
	//Now get Param for whether MIDI THRU is turned on or not
	//float param = this.getParameter(PolyTool.PARAM_ID_THRU) ;
	//out("CB param is " + param);
	//if (param > 0) //= 'true'
	//{
        this.sendVstEventsToHost(ev); //simply echo all incoming events
	//}

	return 0;
    }



}

