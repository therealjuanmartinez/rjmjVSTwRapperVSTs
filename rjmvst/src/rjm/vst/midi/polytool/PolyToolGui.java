package rjm.vst.midi.polytool;


import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public class PolyToolGui extends VSTPluginGUIAdapter implements ChangeListener {

    JSlider VolumeSlider;
    JTextField VolumeText;
    JCheckBox CbThru;
    
    CheckBox cb1;
    int currYPos = 0;
    
    public Button currentLearnButton;
    private VSTPluginAdapter pPlugin;
    
    VBox root;
    VBox rowBox;
    
    private Scene createScene() {
        //Group  root  =  new  Group();
        this.root  =  new  VBox();
        Scene scene  =  new  Scene(root, Color.DARKRED);
        Text text  =  new  Text();
        
        VBox parentBox = new VBox();
        rowBox = new VBox();
        parentBox.getChildren().add(text);

        text.setFont(new Font(25));
        text.setText("PolyTool");

	GridPane rowGrid = new GridPane();
        Button btnNewRow = new Button();
        btnNewRow.setText("Add row");
        btnNewRow.setOnAction(e -> this.HandleNewRowButton()); 
        int idx = 0;
        rowGrid.add(btnNewRow,idx,0); idx++;

        /*
        Button b = new Button();
        b.setText("Add row");
        b.setOnAction(e -> this.HandleNewRowButton()); 
        rowGrid.add(b,idx,0); idx++;
        */

        parentBox.getChildren().add(rowGrid);
        //parentBox.getChildren().add(btnNewRow);
        parentBox.getChildren().add(rowBox);

        root.getChildren().add(parentBox);
        
        
	//https://sourceforge.net/p/jvstwrapper/discussion/318265/ - Go to THIS ONE so the view count doesn't increase
        //https://sourceforge.net/p/jvstwrapper/discussion/318265/thread/23e17e9b/ 6 views 3/1 3:08PM
	//									11 views 3/8 11:13AM - but 4 were mine, so 1 public view has happened
       
        return (scene);
    } 
    
    
    
    private void HandleNewRowButton()
    {
	VstUtils.out("Attempting to add row to collection");
	int rowId = -1;
	try
	{
            MidiRow row = new PolyRow((PolyTool)plugin);
	    rowId = ((PolyTool)plugin).getMidiRowCollection().add(row);

            VstUtils.out("row ID is " + rowId);
            addGuiRow(row);
	}
	catch (Exception e)
	{
	    VstUtils.outStackTrace(e);
	}
	//float rows = this.pPlugin.getParameter(PolyTool.PARAM_ID_ROWS);
	//this.pPlugin.setParameter(PolyTool.PARAM_ID_ROWS, rows + 1);
    }
    
 

    public PolyToolGui( VSTPluginGUIRunner r, VSTPluginAdapter plugin ) throws Exception {

	super( r, plugin );
	try
	{
	    this.setTitle( "PolyTool" );
	    this.setSize(1000, 200);
	    //Consider using setPreferredSize if this ever gives any issues

	    this.addComponentListener(new ComponentAdapter() {
		@Override
		public void componentResized(ComponentEvent e)
		{
		    //this. doesn't work
		}
	    });


	    //TODO (close immediately if it's first opened, and then somehow know if it's launched again?)

	    ((PolyTool)plugin).gui=this; //tell the plug that it has a gui!


	    final JFXPanel fxPanel = new JFXPanel();
	    Scene scene = createScene();
	    fxPanel.setScene(scene);
	    this.pPlugin = plugin;
	    this.getContentPane().add(fxPanel);


	    if( RUNNING_MAC_X ) this.show();
	}
	catch (Exception e)
	{
	    VstUtils.outStackTrace(e);
	}




    }


    public void addGuiRow(MidiRow row) //Create a row in the UI
    {
	/*
	   if (((PolyTool)plugin).getMidiRowCollection().size() == 2) //First row just added to collection, so first row in GUI
	        {
	            this.setSize(this.getSize().width, (int)this.getSize().height + row.getRowHeight());
	        }
	 */
	try
	{
	    GridPane pane = row.getGuiRow();

	    Platform.runLater(new Runnable(){
		@Override
		public void run()
		{ 
		    rowBox.getChildren().add(pane);
		}
	    });
	}
	catch (Exception e)
	{
	    VstUtils.outStackTrace(e);
	}
    }



    
    public MidiRowCollection getMidiRowCollection()
    {
	return ((PolyTool)plugin).getMidiRowCollection();
    }
    
    public void removeRow(GridPane b, int rowId)
    {
	((PolyTool)plugin).getMidiRowCollection().removeRow(rowId);
	Platform.runLater(new Runnable(){
	    @Override
	    public void run()
	    { rowBox.getChildren().remove(b); }
	});
	VstUtils.out("poly collection now has " + ((PolyTool)plugin).getMidiRowCollection().size());
	
	if (((PolyTool)plugin).getMidiRowCollection().size() == 0)
        {
	    clearGuiRows(); //will flush the header too
        }
    }
    
    public void clearGuiRows()
    {
	Platform.runLater(new Runnable(){
	    @Override
	    public void run()
	    { rowBox.getChildren().clear();}
	});

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
	gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //Apparently EXIT_ON_CLOSE can cause problems with jvstwrapper
	//gui.show();
    }

}
