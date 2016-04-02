//Adapted from https://github.com/thbar/opaz-plugdk/blob/master/plugins/Stuff/MIDIVSTPluginSkeleton.java    

//This is an example "hello world" midi VST that only echoes incoming MIDI data


//

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

public class PolyTool extends VSTPluginAdapter implements Serializable {

    public final static int PARAM_ID_ROWS = 0;
    // public final static int PARAM_ID_THRU = 1;

    PolyToolGui gui = null;
    
    MidiRowCollection midiRows = null;
    
    public static int PITCH_BEND = 70;
    public static int NUM_PARAMS = 1;

    public static String[] PARAM_NAMES = new String[] { "Rows", "Midi Thru" };
    public static String[] PARAM_LABELS = new String[] { "VolumeLabel", "EnabledLbl" };
    public static float[] PARAM_PRINT_MUL = new float[] { 0, 1 };
    
    //Chaining works and is coded in, but not TOO tested yet... probably it's fine.
    //Other thing about Chaining is that it sucks more than I thought it would, but 
    //now that the logic's here I'm inclined to leave it in for now
    public static boolean DO_CHAINING = false; 

    // Some default programs
    private float[][] programs = new float[][] { { 0.0f, 1 } };
    private int currentProgram = 0;

    public PolyTool(long wrapper)
    {
	super(wrapper);

	// setProgramHasChunks(true);
	currentProgram = 0;
	
        midiRows = new MidiRowCollection();

	// Apparently this line is instrumental in letting DAW know that Chunks
	// are its way of doing business
	// (otherwise I've seen DAWs not ask for Chunks)
	this.programsAreChunks(true);

	// for compatibility, we say that we have 2 ins and outs
	this.setNumInputs(2);
	this.setNumOutputs(2);

	this.canProcessReplacing(true);// mandatory for vst 2.4!
	this.setUniqueID('k' << 24 | 'R' << 16 | 'u' << 8 | 'b');// jRub
	
	this.isSynth(true);

	VstUtils.out("LOADING");
	VstUtils.out("Host can receive vst midi?: " + this.canHostDo(CANDO_HOST_RECEIVE_VST_MIDI_EVENT));
	eventsToDeduplicate = new ArrayList<VSTEvent>();
    }

    public void resume()
    {
	// Need to call this so the host knows we want MIDI events.
	wantEvents(1);
    }
    
    public MidiRowCollection getMidiRowCollection()
    {
	return this.midiRows;
    }
    
    
    public int getPlugCategory(){
//      return PLUG_CATEG_UNKNOWN;
//      return PLUG_CATEG_EFFECT;
//      return PLUG_CATEG_GENERATOR;
     return PLUG_CATEG_SYNTH;
 }

 public int canDo(String feature){
     //log("harms synth cando: "+feature+".");
     if(CANDO_PLUG_RECEIVE_VST_EVENTS.equals(feature))
         return CANDO_YES;
     if(CANDO_PLUG_RECEIVE_VST_MIDI_EVENT.equals(feature))
         return CANDO_YES;
      if(CANDO_PLUG_RECEIVE_VST_TIME_INFO.equals(feature))
          return CANDO_YES;
   //   if(CANDO_PLUG_MIDI_PROGRAM_NAMES.equals(feature))
    //      return CANDO_YES;
      if(CANDO_PLUG_SEND_VST_EVENTS.equals(feature))
          return CANDO_YES;
      if(CANDO_PLUG_SEND_VST_MIDI_EVENT.equals(feature))
          return CANDO_YES;
     return CANDO_NO;
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
	    String polysSerializedString = VstUtils.toString(this.midiRows);
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
	    String serializedString = new String(data).trim();
	    this.midiRows = (MidiRowCollection)VstUtils.fromString(serializedString);
	    for (int i = 0; i < midiRows.size(); i++)
	    {
		MidiRow row = this.midiRows.getRow(i);
		row.setPlugin(this);
		//TODO is next line really needed?
		getMidiRowCollection().updateRow(row);
	    }
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
	    for (int i = 0; i < midiRows.size(); i++)
	    {
		gui.addGuiRow(midiRows.getRow(i));
	    }
	}

    }
    
   
  

    private List<VSTEvent> eventsToDeduplicate;
    public void processMidiFromRow(VSTEvents events, MidiRow row)
    {
	if (!DO_CHAINING)
	{
	    eventsToDeduplicate.addAll(VstUtils.convertVSTEventsToList(events));

	    //Send data right out to host
            //this.sendVstEventsToHost(events);
	}
	else
	{
	    //Send data to next item in the chain (if exists)
	    MidiRow successorRow = midiRows.getSuccessorToThisMidiRow(row);
	    if (successorRow != null)
	    {
		if (successorRow.getEnabled())
		{
                    //Send to next row
                    successorRow.processEvents(events);
		}
		else
		{
		    processMidiFromRow(events, successorRow); //Recurse to next MidiRow
		}
	    }
	    else
	    {
		//Finally, send MIDI to host
		this.sendVstEventsToHost(events);
	    }
	}
    }
 

    // process MIDI
    public int processEvents(VSTEvents events)
    {
	//TODO process this through "MidiRow" interface objects 
	
	List<VSTEvent> origEvents = VstUtils.cloneVSTEventsToList(events);
	if (gui != null) 
	{
	  
	    VstUtils.out("midiRows size is " + this.midiRows.size());
	    
	    if ((midiRows.size() == 0)||(midiRows.areAllRowsDisabled()))
	    {
		//Default to MIDI THRU on when no rows present...
		this.sendVstEventsToHost(events);
	    }

	    if (!DO_CHAINING) //Go through all rows one by one and process them manually
	    {
		this.eventsToDeduplicate.clear();
		for (int i = 0; i < this.midiRows.size(); i++)
		{
		    MidiRow row = this.midiRows.getRow(i);
		    if (row.getEnabled())
		    {
                            row.processEvents(events);
		    }
		}

		//DEDUPE NOW
		Set<VSTEvent> uniqueSet = new LinkedHashSet<>(eventsToDeduplicate); //This should do the dedupe
		List<VSTEvent> uniqueEvents = new ArrayList<VSTEvent>();
		uniqueEvents.addAll(uniqueSet);
		this.sendVstEventsToHost(VstUtils.convertToVSTEvents(uniqueEvents));
	    }
	    else //Doing chaining...
	    {
		//Just get first row to process, chaining will take care of any additional rows
		if (midiRows.size() > 0)
		{
		    midiRows.getRow(0).processEvents(events);
		}
	    }
	}

	return 0;
    }

   

  


}
