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
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
    
 
  

    public PolyToolGui( VSTPluginGUIRunner r, VSTPluginAdapter plugin ) throws Exception {

	super( r, plugin );
	try
	{
	    this.setTitle( "PolyTool" );
	    this.setSize(685, 200);
	    
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
		lblEnabled.setFont(new Font("Arial", 7));
		Label lblInputChannel = new Label();
		lblInputChannel.setFont(new Font("Arial", 7));
		Label lblNote = new Label();
		lblNote.setFont(new Font("Arial", 7));
		Label lblMax = new Label();
		lblMax.setFont(new Font("Arial", 7));
		Label lblMin = new Label();
		lblMin.setFont(new Font("Arial", 7));
		Label lblCC = new Label();
		lblCC.setFont(new Font("Arial", 7));
		
		lblEnabled.setText("");
		lblEnabled.setMinWidth(70);
		lblInputChannel.setText("Input Chan");
		lblNote.setText("Input Note");
		lblMax.setText("Max Output Value");
		lblMin.setText("Min Output Value");
		lblCC.setText("CC Output");


	        rowGrid.add(lblEnabled,0,0);
	        rowGrid.add(lblInputChannel,1,0);
	        rowGrid.add(lblNote,2,0);
	        rowGrid.add(lblMin,3,0);
	        rowGrid.add(lblMax,4,0);
	        rowGrid.add(lblCC,5,0);

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
	rowGrid.setStyle("-fx-background-color: #C0C0C0;");
	//TODO
	//Enabled, Input Chan (app level instead?), Input Note, Learn Button, (Output Chan make app level?), CC#, CC Min, CC Max 

        CheckBox cb = new CheckBox();
        cb.setText("Enabled");
        cb.setSelected(true);
        cb.setId("" + row.getId());
        //This style of event handling below wasn't supposedly available until Java 8, and it's quite nice
        cb.setOnAction(e -> this.HandleEnabledCheckbox(cb)); 
        rowGrid.add(cb,0,0);

        int numWidth = 50; //width of text fields that contain numbers

        String[] channels = new String[16];
        for (int i = 0; i < 16; i++){ channels[i] = Integer.toString(i + 1);}
        
        ComboBox<String> cbInputChannel = new ComboBox<String>();
        cbInputChannel.getItems().addAll(channels);
        if (row.getInputChannel() >= 0)
        { //Here getting string instead of int so we pick the item based on the visible GUI value
            cbInputChannel.getSelectionModel().select(Integer.toString(row.getInputChannel())); }
        else
        { cbInputChannel.getSelectionModel().select(0);}//Auto select channel 1 for input }
        cbInputChannel.setId("" + row.getId());
        cbInputChannel.setOnAction(e -> HandleInChannelCombo(cbInputChannel));
        rowGrid.add(cbInputChannel,1,0);
        HandleInChannelCombo(cbInputChannel);
        
        ComboBox<String> cbOutputChannel = new ComboBox<String>();
        cbOutputChannel.getItems().addAll(channels);
        if (row.getOutputChannel() >= 0)
        { cbOutputChannel.getSelectionModel().select(Integer.toString(row.getOutputChannel())); }
        else
        { cbOutputChannel.getSelectionModel().select(0);}//Auto select channel 1 for input }
        cbOutputChannel.setId("" + row.getId());
        cbOutputChannel.setOnAction(e -> HandleOutChannelCombo(cbOutputChannel));
        rowGrid.add(cbOutputChannel,2,0);
        HandleOutChannelCombo(cbOutputChannel);

	Button learnBtn = new Button();
	learnBtn.setText("Learn Note");
	if (row.getNote() != null)
	{
	    learnBtn.setText(row.getNote().getNoteNamePlusOctave());
	}
	learnBtn.setId("" + row.getId());
	learnBtn.setMinWidth(77);
	learnBtn.setOnAction(e -> learnKey(learnBtn)); 
        rowGrid.add(learnBtn,3,0);

        String[] vals = new String[128];
        for (int i = 0; i < 128; i++){ vals[i] = Integer.toString(i);}

        ComboBox<String> minValue = new ComboBox<String>();
        minValue.getItems().addAll(vals);
        if (row.getMinOutputValue() >= 0)
        { minValue.getSelectionModel().select(Integer.toString(row.getMinOutputValue())); }
        else
        { minValue.getSelectionModel().select(0); }//Auto select channel 1 for input }
        minValue.setId("" + row.getId());
        minValue.setOnAction(e -> HandleMinOutValueCombo(minValue));
        rowGrid.add(minValue,4,0);
        HandleMinOutValueCombo(minValue);
        
        
        ComboBox<String> maxValue = new ComboBox<String>();
        maxValue.getItems().addAll(vals);
        if (row.getMaxOutputValue() >= 0)
        { maxValue.getSelectionModel().select(Integer.toString(row.getMaxOutputValue())); }
        else
        { maxValue.getSelectionModel().select(127); }//Auto select channel 1 for input }
        maxValue.setId("" + row.getId());
        maxValue.setOnAction(e -> HandleMaxOutValueCombo(maxValue));
        rowGrid.add(maxValue,5,0);
        HandleMaxOutValueCombo(maxValue);

        String[] ccVals = new String[64];
        for (int i = 0; i < 64; i++){ ccVals[i] = Integer.toString(i);}

        ComboBox<String> ccOut = new ComboBox<String>();
        ccOut.getItems().addAll(ccVals);
        if (row.getOutputCCNum() >= 0)
        { ccOut.getSelectionModel().select(Integer.toString(row.getOutputCCNum())); }
        else
        { ccOut.getSelectionModel().select(12); }//Auto select CC 11 for arbitrary reasons
        ccOut.setId("" + row.getId());
        ccOut.setOnAction(e -> HandleOutCCCombo(ccOut));
        rowGrid.add(ccOut,6,0);
        HandleOutCCCombo(ccOut);
        
        
        //This is the value we set CC to on note off 
        ComboBox<String> noteOffValue = new ComboBox<String>();
        noteOffValue.getItems().addAll(vals);
        if (row.getNoteOffCCValue() >= 0)
        { noteOffValue.getSelectionModel().select(Integer.toString(row.getMaxOutputValue())); }
        else
        { noteOffValue.getSelectionModel().select(0); }
        noteOffValue.setId("" + row.getId());
        noteOffValue.setOnAction(e -> HandleNoteOffValueCombo(noteOffValue));
        rowGrid.add(noteOffValue,7,0);
        HandleNoteOffValueCombo(noteOffValue);

        /*
        TextField ccOut = new TextField();
        ccOut.setText("11");
        ccOut.setMaxWidth(numWidth);
        rowGrid.add(ccOut,5,0);
        */

	Button delBtn = new Button();
	delBtn.setText("Delete");
	delBtn.setId("" + row.getId());
	delBtn.setOnAction(e -> removeRow(rowGrid, Integer.parseInt(delBtn.getId()))); 
        rowGrid.add(delBtn,8,0);
	
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
    
    
    public Button currentLearnButton;
    public void learnKey(Button learnBtn)
    {
	Platform.runLater(new Runnable(){
            @Override
            public void run()
            { learnBtn.setText("Play a key");
            }
        });
	this.currentLearnButton = learnBtn;
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
    
    public void HandleEnabledCheckbox(CheckBox cb)
    {
	VstUtils.out("Checkbox set to " + cb.isSelected());
	Boolean checked = cb.selectedProperty().get();
	
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	row.setEnabled(checked);
	getPolyCollection().updateRow(row);
	
	VstUtils.out("Made it this far...");
	row = getPolyCollection().getRowByRowId(cb.getId());
	VstUtils.out("row " + row.getId() + " enabled = " + row.getEnabled());
    }
    
    public void HandleInChannelCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	row.setInputChannel(Integer.parseInt(value));
	getPolyCollection().updateRow(row);
    }
    
    public void HandleOutChannelCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	row.setOutputChannel(Integer.parseInt(value));
	getPolyCollection().updateRow(row);
    }
    
    public void HandleMinOutValueCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	row.setMinOutputValue(Integer.parseInt(value));
	getPolyCollection().updateRow(row);
    }
    
    public void HandleMaxOutValueCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	row.setMaxOutputValue(Integer.parseInt(value));
	getPolyCollection().updateRow(row);
    }
    
    public void HandleOutCCCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	row.setOutputCCNum(Integer.parseInt(value));
	getPolyCollection().updateRow(row);
    }
    
    public void HandleNoteOffValueCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	PolyRow row = getPolyCollection().getRowByRowId(cb.getId());
	row.setNoteOffCCValue(Integer.parseInt(value));
	getPolyCollection().updateRow(row);
    }
    

    
    public PolyRowCollection getPolyCollection()
    {
	return ((PolyTool)plugin).getPolyCollection();
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
