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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
        
        HBox box = new HBox();
        //box.setLayoutX(100);  can't get these to work right...
        //box.setLayoutY(100);
        box.getChildren().add(text);

        text.setFont(new Font(25));
        text.setText("PolyTool");
        
        Button b = new Button();
        b.setText("Add row");
        b.setOnAction(e -> this.HandleNewRowButton()); 
        box.getChildren().add(b);

        root.getChildren().add(box);
        
        return (scene);
    } 
    
    public void HandleCheckbox(CheckBox cb)
    {
	//VstUtils.out("Checkbox set to " + jb.isSelected());

	Boolean checked = cb.selectedProperty().get();
	this.pPlugin.setParameterAutomated(JustEchoMidi.PARAM_ID_THRU, (checked) ? 1 : 0);

	UIUtils.showAlert("ID is " + cb.getId() + " and checked is " + checked.toString());
    }
    
    public void HandleNewRowButton()
    {
	float rows = this.pPlugin.getParameter(PolyTool.PARAM_ID_ROWS);
	this.pPlugin.setParameter(PolyTool.PARAM_ID_ROWS, rows + 1);
	addRow();
    }

    public PolyToolGui( VSTPluginGUIRunner r, VSTPluginAdapter plugin ) throws Exception {

	super( r, plugin );
	try
	{
	    this.setTitle( "PolyTool" );
	    this.setSize(300, 200);

	    this.rowCount = 0;
	    
	        final JFXPanel fxPanel = new JFXPanel();
	        fxPanel.setScene(createScene());
	        this.pPlugin = plugin;
	        this.getContentPane().add(fxPanel);
	 
	    if( RUNNING_MAC_X ) this.show();
	}
	catch (Exception e)
	{
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    e.printStackTrace(pw);
	}

    }
    
    
    int rowCount;
    private void addRow()
    {
	int y = currYPos + rowHeight;

	HBox b = new HBox();
	
	
	//TODO
	//Enabled, Input Chan, Input Note, Learn Button, Output Chan, CC#, CC Min, CC Max 

	int x = 0;
	while (x < 2)
	{
	    CheckBox cb = new CheckBox();
	    cb.setText("Test" + x);
	    cb.setLayoutX(50);
	    cb.setLayoutY(y);
	    cb.setSelected(true);
	    cb.setId("cb_" + rowCount);
	    x++;

	    //This style of event handling below wasn't supposedly available until Java 8, and it's quite nice
	    cb.setOnAction(e -> this.HandleCheckbox(cb)); 

	    b.getChildren().add(cb);

	}
	
	TextField chan = new TextField();
	chan.setText("1");
	b.getChildren().add(chan);
	
	Button learnBtn = new Button();
	learnBtn.setText("Learn Input Note");
	b.getChildren().add(learnBtn);



	Platform.runLater(new Runnable(){
	    @Override
	    public void run()
	    { root.getChildren().add(b); }
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
