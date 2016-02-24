package rjm.vst.midi.examples.gui.swing;


import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jvst.wrapper.*;
import jvst.wrapper.gui.VSTPluginGUIRunner;
import rjm.vst.midi.examples.gui.swing.JustEchoMidi;
import rjm.vst.tools.VstUtils;


public class JustEchoMidiGui extends VSTPluginGUIAdapter implements ChangeListener {

    JSlider VolumeSlider;
    JTextField VolumeText;
    JCheckBox CbThru;

    private VSTPluginAdapter pPlugin;

    public JustEchoMidiGui( VSTPluginGUIRunner r, VSTPluginAdapter plugin ) throws Exception {

	super( r, plugin );
	try
	{
	    this.setTitle( "Midi Echo VST" );
	    this.setSize(220,200);

	    this.setResizable(false);

	    pack();
	    this.pPlugin = plugin;
	    this.init();

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
		    this.pPlugin.setParameterAutomated(JustEchoMidi.PARAM_ID_VOLUME, (float)((float)sl.getValue() / 100F));

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



    public boolean checked;

    public void init()
    {
	((JustEchoMidi)plugin).gui=this; //tell the plug that it has a gui!

	VstUtils.out("PARAM_ID_VOLUME is " + this.pPlugin.getParameter(JustEchoMidi.PARAM_ID_VOLUME));
	VstUtils.out("PARAM_ID_VOLUME * 100F is " + this.pPlugin.getParameter(JustEchoMidi.PARAM_ID_VOLUME) * 100F);
	this.VolumeSlider = new JSlider(JSlider.VERTICAL, 0, 100, (int)(this.pPlugin.getParameter(JustEchoMidi.PARAM_ID_VOLUME) ));
	this.VolumeText = new JTextField("0");
	this.CbThru = new JCheckBox("Midi Thru");

	this.checked = CbThru.isSelected();

	this.VolumeSlider.addChangeListener(this);
	this.CbThru.addChangeListener(this);

	JLabel VolumeLabel = new JLabel(this.pPlugin.getParameterName(JustEchoMidi.PARAM_ID_VOLUME));

	GridLayout grids = new GridLayout(2, 2);
	this.getContentPane().setLayout(grids);

	Box VolumeBox = new Box(BoxLayout.Y_AXIS);

	VolumeBox.add(VolumeLabel);
	VolumeBox.add(this.VolumeSlider);
	VolumeBox.add(this.VolumeText);

	this.getContentPane().add(VolumeBox);
	this.getContentPane().add(CbThru);
    }



    public static void main(String[] args) throws Throwable 
    {
	JustEchoMidiGui gui = new JustEchoMidiGui(null, null);
	gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
