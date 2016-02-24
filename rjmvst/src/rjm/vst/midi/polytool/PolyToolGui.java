package rjm.vst.midi.polytool;


import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jvst.wrapper.*;
import jvst.wrapper.gui.VSTPluginGUIRunner;
import rjm.vst.midi.examples.gui.swing.JustEchoMidi;
import rjm.vst.tools.VstUtils;
import rjm.vst.javafx.SceneToJComponent;
import rjm.vst.javafx.UIUtils;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class PolyToolGui extends VSTPluginGUIAdapter implements ChangeListener {

    JSlider VolumeSlider;
    JTextField VolumeText;
    JCheckBox CbThru;
    
    CheckBox cb1;
    int currYPos = 0;
    final int rowHeight = 20;

    private VSTPluginAdapter pPlugin;
    
    VBox root;
    
    //THIS IS THE HOT AREA RIGHT NOW
    private Scene createScene() {
        //Group  root  =  new  Group();
        this.root  =  new  VBox();
        Scene  scene  =  new  Scene(root, Color.DARKRED);
        Text  text  =  new  Text();
        
        text.setX(40);
        text.setY(100);
        text.setFont(new Font(25));
        text.setText("Welcome JavaFX!");

        root.getChildren().add(text);
        
      //A checkbox without a caption
        cb1 = new CheckBox();
        cb1.setText("Enable Midi Thru");
        cb1.setLayoutX(50);
        cb1.setLayoutY(115);
        cb1.setSelected(true);
        cb1.setId("testcb");
        
        //This style of event handling below wasn't supposedly available until Java 8, and it's quite nice
        cb1.setOnAction(e -> this.HandleCheckbox(cb1));
        
        root.getChildren().add(cb1);

        return (scene);
    } 
    
    public void HandleCheckbox(CheckBox cb)
    {
	//VstUtils.out("Checkbox set to " + jb.isSelected());

	Boolean checked = cb.selectedProperty().get();
	this.pPlugin.setParameterAutomated(JustEchoMidi.PARAM_ID_THRU, (checked) ? 1 : 0);

	UIUtils.showAlert("ID is " + cb.getId() + " and checked is " + checked.toString());
	
	addRow();
    }

    public PolyToolGui( VSTPluginGUIRunner r, VSTPluginAdapter plugin ) throws Exception {

	super( r, plugin );
	try
	{
	    //add
	    //Jpanel declaration (?)

	    this.setTitle( "Midi Echo VST" );
	    this.setSize(300, 200);

	    this.rowCount = 0;
	    
	    //JFrame frame = new JFrame("Swing and JavaFX");
	        final JFXPanel fxPanel = new JFXPanel();
	        fxPanel.setScene(createScene());
	        this.pPlugin = plugin;
	        
	        this.getContentPane().add(fxPanel);
	        
	        cb1 = new CheckBox();
	        cb1.setText("Enable Midi Thru2");
	        cb1.setLayoutX(50);
	        cb1.setLayoutY(135);
	        this.currYPos = 135;
	        
	        cb1.setSelected(true);
	        cb1.setId("testcb");
	        
	        //This style of event handling below wasn't supposedly available until Java 8, and it's quite nice
	        cb1.setOnAction(e -> this.HandleCheckbox(cb1)); 
	        
                Platform.runLater(new Runnable(){
		    @Override
		    public void run()
		    { root.getChildren().add(cb1); }
	        });
	        
	        //UIUtils.showAlert("Hey so i guess this worked??");
	        
	        
	    if( RUNNING_MAC_X ) this.show();
	}
	catch (Exception e)
	{
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    e.printStackTrace(pw);
	    VstUtils.out("ERROR: " + sw.toString()); // stack trace as a string
	        UIUtils.showAlert("Hey so i guess this NOT worked?? " + sw.toString() );
	}

    }
    
    
    int rowCount;
    private void addRow()
    {
	int y = currYPos + rowHeight;

	CheckBox cb = new CheckBox();
	cb.setText("Enable Midi Thru2");
	cb.setLayoutX(50);
	cb.setLayoutY(y);
	cb.setSelected(true);
	cb.setId("cb_" + rowCount);

	//This style of event handling below wasn't supposedly available until Java 8, and it's quite nice
	cb.setOnAction(e -> this.HandleCheckbox(cb)); 

	Platform.runLater(new Runnable(){
	    @Override
	    public void run()
	    { root.getChildren().add(cb); }
	});

	rowCount++;
	currYPos = y;
    }


    @Override
    public void stateChanged(ChangeEvent e)
    {
	//Since we're using JavaFX, we maybe don't ever need to use this function...
		//but it is still required as it is an inherited abstract method	
    }






    public static void main(String[] args) throws Throwable 
    {
	PolyToolGui gui = new PolyToolGui(new VSTPluginGUIRunner(), null);
	gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//gui.show();
    }

}
