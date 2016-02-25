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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
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
        double rowHeight;

    private VSTPluginAdapter pPlugin;
    
    VBox root;
    
    VBox rowBox;
    
    //THIS IS THE HOT AREA RIGHT NOW
    private Scene createScene() {
        //Group  root  =  new  Group();
        this.root  =  new  VBox();
        Scene  scene  =  new  Scene(root, Color.DARKRED);
        Text  text  =  new  Text();
        
        VBox parentBox = new VBox();
        rowBox = new VBox();
        parentBox.getChildren().add(text);

        text.setFont(new Font(25));
        text.setText("PolyTool");
        
        Button btnNewRow = new Button();
        btnNewRow.setText("Add row");
        btnNewRow.setOnAction(e -> this.HandleNewRowButton()); 
        parentBox.getChildren().add(btnNewRow);

        parentBox.getChildren().add(rowBox);

        root.getChildren().add(parentBox);
        
        return (scene);
    } 
    
 
    
    public void HandleNewRowButton()
    {

	VstUtils.out("Attempting to add row to poly collection");
	int rowId = -1;
	try
	{
            PolyRow row = new PolyRow();
            VstUtils.out("Row has id of " + row.getId());
	    rowId = ((PolyTool)plugin).getPolyCollection().add(row);
            VstUtils.out("Row NOW has id of " + rowId);
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
	    this.setSize(600, 200);
	    
	    this.addComponentListener(new ComponentAdapter() {
		    @Override
		    public void componentResized(ComponentEvent e)
		    {
		        //this. doesn't work
		    }
		});
	    
	    ((PolyTool)plugin).gui=this; //tell the plug that it has a gui!

	    
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
   
  

    public void addGuiRowHeader()
    {
	VstUtils.out("Adding row header");
                //This is meant to be a single 1 row grid
		GridPane rowGrid = new GridPane();
		rowGrid.setPadding(new Insets(5));
		rowGrid.setHgap(10);
		rowGrid.setVgap(10);
		//TODO
		//Enabled, Input Chan, Input Note, Learn Button, Output Chan, CC#, CC Min, CC Max 
		
		Label lblEnabled = new Label();
		Label lblInputChannel = new Label();
		
		lblEnabled.setText("Enabled");
		lblInputChannel.setText("    Input Chan");

	        rowGrid.add(lblEnabled,0,0);
	        rowGrid.add(lblInputChannel,1,0);

		Platform.runLater(new Runnable(){
		    @Override
		    public void run()
		    { rowBox.getChildren().add(rowGrid);
		    }
		});
    }
    
    
    public void addGuiRow(PolyRow row)
    {
	if (((PolyTool)plugin).getPolyCollection().size() == 1) //First row just added to collection, so first row in GUI
	{
	    addGuiRowHeader();
	}

	//This is meant to be a single 1 row grid
	GridPane rowGrid = new GridPane();
	rowGrid.setPadding(new Insets(5));
	rowGrid.setHgap(10);
	rowGrid.setVgap(10);
	//TODO
	//Enabled, Input Chan (app level instead?), Input Note, Learn Button, (Output Chan make app level?), CC#, CC Min, CC Max 

        CheckBox cb = new CheckBox();
        cb.setText("Enabled");
        cb.setSelected(true);
        cb.setId("" + row.getId());

        //This style of event handling below wasn't supposedly available until Java 8, and it's quite nice
        cb.setOnAction(e -> this.HandleEnabledCheckbox(cb)); 
        rowGrid.add(cb,0,0);
	
	TextField chan = new TextField();
	chan.setText("1");
        rowGrid.add(chan,1,0);
	
	Button learnBtn = new Button();
	learnBtn.setText("Learn Note");
	learnBtn.setId("" + row.getId());
	learnBtn.setOnAction(e -> VstUtils.out("This is Row ID " + learnBtn.getId())); 
        rowGrid.add(learnBtn,2,0);

        int numWidth = 50;
	Button delBtn = new Button();
	delBtn.setText("Delete");
	delBtn.setId("" + row.getId());
	delBtn.setOnAction(e -> removeRow(rowGrid, Integer.parseInt(delBtn.getId()))); 
        rowGrid.add(delBtn,3,0);

        TextField ccOut = new TextField();
        ccOut.setText("11");
        ccOut.setMaxWidth(numWidth);
        rowGrid.add(ccOut,6,0);
        
        TextField minValue = new TextField();
        minValue.setText("0");
        minValue.setMaxWidth(numWidth);
        rowGrid.add(minValue,4,0);
        
        TextField maxValue = new TextField();
        maxValue.setText("127");
        maxValue.setMaxWidth(numWidth);
        rowGrid.add(maxValue,5,0);
	
        this.rowHeight = rowGrid.heightProperty().doubleValue(); //so we know how high these things are

        if (((PolyTool)plugin).getPolyCollection().size() == 2) //First row just added to collection, so first row in GUI
        {
            this.setSize(this.getSize().width, (int)this.getSize().height + (int)rowHeight);
        }

        Platform.runLater(new Runnable(){
            @Override
            public void run()
            { rowBox.getChildren().add(rowGrid);
            }
        });

	VstUtils.out("poly collection now has " + ((PolyTool)plugin).getPolyCollection().size());
    }
    
    
    public void HandleEnabledCheckbox(CheckBox cb)
    {
	VstUtils.out("Checkbox set to " + cb.isSelected());
	Boolean checked = cb.selectedProperty().get();
	
	PolyRow row = ((PolyTool)plugin).getPolyCollection().getRowByRowId(cb.getId());
	
	row.setEnabled(checked);
	
	((PolyTool)plugin).getPolyCollection().updateRow(row);
	
	VstUtils.out("Made it this far...");
	row = ((PolyTool)plugin).getPolyCollection().getRowByRowId(cb.getId());
	VstUtils.out("row " + row.getId() + " enabled = " + row.getEnabled());
    }
    
    public void removeRow(GridPane b, int rowId)
    {
	((PolyTool)plugin).getPolyCollection().removeRow(rowId);
	Platform.runLater(new Runnable(){
	    @Override
	    public void run()
	    { rowBox.getChildren().remove(b); }
	});
	VstUtils.out("poly collection now has " + ((PolyTool)plugin).getPolyCollection().size());
	
	if (((PolyTool)plugin).getPolyCollection().size() == 0)
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
	gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//gui.show();
    }

}
