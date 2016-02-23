package rjm.vst.midi.polytool;


import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.sound.midi.Synthesizer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jvst.wrapper.*;
import jvst.wrapper.gui.VSTPluginGUIRunner;
import rjm.vst.midi.examples.gui.JustEchoMidi;
import rjm.vst.tools.VstUtils;


public class PolyToolGui extends VSTPluginGUIAdapter implements ChangeListener {

    JSlider VolumeSlider;
    JTextField VolumeText;
    JCheckBox CbThru;

    //private Synthesizer synthesizer;
    private VSTPluginAdapter pPlugin;

    public PolyToolGui( VSTPluginGUIRunner r, VSTPluginAdapter plugin ) throws Exception {

	super( r, plugin );
	try
	{
	    //final MidiEchoVSTWithGui midiEchoVst = ( MidiEchoVSTWithGui ) plugin;

	    this.setTitle( "PolyTool" );
	    this.setSize(220,200);


	    //this.setResizable(false);
	    this.setResizable(true);

	    this.pPlugin = plugin;

	    this.init();
	    //this.pack();

	    if( RUNNING_MAC_X ) this.show();
	}
	catch (Exception e)
	{
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    e.printStackTrace(pw);
	    VstUtils.out("ERROR: " + sw.toString()); // stack trace as a string
	}

    }


    @Override
    public void stateChanged(ChangeEvent e)
    {
	try
	{
	    if (e.getSource().getClass() == JSlider.class)
	    {
		JSlider sl = (JSlider)e.getSource();
		if (sl == this.VolumeSlider) {

		    //Sets to somewhere between 0.0 to 1.0
		    VstUtils.out("Setting parameter to " + (float)((float)sl.getValue() / 100F));
		    this.pPlugin.setParameterAutomated(PolyTool.PARAM_ID_VOLUME, (float)((float)sl.getValue() / 100F));

		    //Sets to somewhere between 0-127
		    VstUtils.out("Setting volume text to " + this.pPlugin.getParameterDisplay(JustEchoMidi.PARAM_ID_VOLUME));
		    this.VolumeText.setText(this.pPlugin.getParameterDisplay(JustEchoMidi.PARAM_ID_VOLUME));
		}
	    }
	    if (e.getSource().getClass() == JCheckBox.class)
	    {
		//This seems to get triggered on hover-over also....
		JCheckBox jb = (JCheckBox)e.getSource();
		VstUtils.out("Checkbox set to " + jb.isSelected());

		if (jb.isSelected() != this.checked) //Make sure it's a true state change
		{
		    this.checked = jb.isSelected();
		    //this.testThingy();
		    this.addRowToGui();
		}
		this.pPlugin.setParameterAutomated(JustEchoMidi.PARAM_ID_THRU, (jb.isSelected()) ? 1 : 0);
	    }

	} catch (Exception ex)
	{
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    ex.printStackTrace(pw);
	    VstUtils.out("ERROR: " + sw.toString()); // stack trace as a string
	}

    }

    public void doTestSerializiation()
    {

	String string;
	try
	{
	    SomeClass b4 = new SomeClass();
	    b4.setCustom("This ia custom");
	    string = VstUtils.toString( b4 );
	    VstUtils.out(" Encoded serialized version " );
	    VstUtils.out( string );
	    SomeClass some = ( SomeClass ) VstUtils.fromString( string );
	    VstUtils.out( "\n\nReconstituted object");
	    VstUtils.out( some.toString() );
	    VstUtils.out( some.getCustom() + " <- that was the custom");
	} catch (IOException | ClassNotFoundException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public boolean checked;

    public void init()
    {
	((PolyTool)plugin).gui=this; //tell the plug that it has a gui!

	VstUtils.out("PARAM_ID_VOLUME is " + this.pPlugin.getParameter(JustEchoMidi.PARAM_ID_VOLUME));
	VstUtils.out("PARAM_ID_VOLUME * 100F is " + this.pPlugin.getParameter(JustEchoMidi.PARAM_ID_VOLUME) * 100F);
	//this.VolumeSlider = new JSlider(JSlider.VERTICAL, 1, 100, (int)(this.pPlugin.getParameter(MidiEchoVSTWithGui.PARAM_ID_VOLUME) * 100F));
	this.VolumeSlider = new JSlider(JSlider.VERTICAL, 0, 100, (int)(this.pPlugin.getParameter(JustEchoMidi.PARAM_ID_VOLUME) ));
	this.VolumeText = new JTextField("0");
	this.CbThru = new JCheckBox("Midi Thru");

	this.checked = CbThru.isSelected();

	this.VolumeSlider.addChangeListener(this);
	this.CbThru.addChangeListener(this);


	//JLabel VolumeLabel = new JLabel("The Volume");
	JLabel VolumeLabel = new JLabel(this.pPlugin.getParameterName(JustEchoMidi.PARAM_ID_VOLUME));

	//GridLayout grids = new GridLayout(1, 3);

	GridLayout grids = new GridLayout(2, 2);
	this.getContentPane().setLayout(grids);

	Box VolumeBox = new Box(BoxLayout.Y_AXIS);
	//Box FeedbackBox = new Box(BoxLayout.Y_AXIS);
	//Box DelayBox = new Box(BoxLayout.Y_AXIS);

	VolumeBox.add(VolumeLabel);
	VolumeBox.add(this.VolumeSlider);
	VolumeBox.add(this.VolumeText);

	this.getContentPane().add(VolumeBox);
	this.getContentPane().add(CbThru);
    }


    public void addRowToGui()
    {
	Box RowBox = new Box(BoxLayout.X_AXIS);


	//  JSlider VolumeSlider;
	//  JTextField VolumeText;
	//  JCheckBox CbThru;

	JCheckBox CbOnOff = new JCheckBox("Enabled");
	CbOnOff.setSelected(true);
	RowBox.add(CbOnOff);

	//NOT per row need: Input Channel

	//Need
	//"Learn Button"
	//On-Off checkbox
	//Input Note Value
	//Output CC #
	//Min/Max
	//Maybe a color box to show input

	this.getContentPane().add(RowBox);


    }

    public void testThingy()
    {
	this.getContentPane().getLayout();
	this.getContentPane().add(new JLabel("TeST"));

	doTestSerializiation();
    }

    public static void main(String[] args) throws Throwable {
	boolean DEBUG=true;

	//JayDLayGUI gui = new JayDLayGUI(null,null);
	PolyToolGui gui = new PolyToolGui(null, null);
	gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//gui.show();
    }



}


class SomeClass implements Serializable {

    private final static long serialVersionUID = 1; 

    int i    = Integer.MAX_VALUE;
    String s = "ABCDEFGHIJKLMNOP";
    Double d = new Double( -1.0 );


    String custom;

    public String toString(){
	return  "SomeClass instance says: Don't worry, " 
		+ "I'm healthy. Look, my data is i = " + i  
		+ ", s = " + s + ", d = " + d;
    }

    public void setCustom(String value)
    {
	this.custom = value;
    }

    public String getCustom()
    {
	return this.custom;
    }

}
