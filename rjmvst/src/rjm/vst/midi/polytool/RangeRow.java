package rjm.vst.midi.polytool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import com.sun.scenario.effect.light.Light.Type;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import jvst.wrapper.valueobjects.VSTEvent;
import jvst.wrapper.valueobjects.VSTEvents;
import jvst.wrapper.valueobjects.VSTMidiEvent;
import rjm.midi.tools.MidiUtils;
import rjm.midi.tools.Note;
import rjm.vst.javafx.UIUtils;
import rjm.vst.midi.polytool.PolyTool.TypedVSTEvent;
import rjm.vst.tools.VstUtils;

public class RangeRow implements Serializable, MidiRow {

    private static final long serialVersionUID = -5877708938899912589L;

    transient PolyTool p;
    transient Button learnBtn;

    private Note lowerNote;
    private Note upperNote;
    private Note lowerNoteMapped;
    private String name;
    private int inputChannel;
    private int outputChannel;

    private int id;
    private boolean enabled;
    private double rowHeight;

    public RangeRow(PolyTool p)
    {
	lowerNote = null;
	upperNote = null;
	lowerNoteMapped = null;
	inputChannel = 1;
	outputChannel = 1;
	enabled = true;

	this.p = p;
    }

    public void setPlugin(PolyTool p)
    {
	this.p = p;
    }

    public boolean getEnabled()
    {
	return this.enabled;
    }

    public String getDebugString()
    {
	StringBuilder sb = new StringBuilder();
	sb.append("\n");
	sb.append("Enabled " + enabled + "\n");
	sb.append("ID " + this.id + "\n");
	sb.append("Input Channel " + this.inputChannel + "\n");
	sb.append("Output Channel " + this.outputChannel + "\n");

	//sb.append("Note " + this. + "\n");
	return sb.toString();
    }

    public boolean isGoodForProcessing()
    {
	if (!enabled)
	{
	    return false;
	}

	/*
	if ((lowerNote == null) || (upperNote == null) || (lowerNoteMapped == null))
	{
	    return false;
	}
	*/


	if ((inputChannel < 0)||
		(outputChannel < 0)
		)
	{
	    VstUtils.out("one of the values was bad");
	    return false;
	}

	return true;
    }

    public String getName()
    { return name; }

    public void setName(String name)
    { this.name = name; }


    public int getId()
    { return id; }
    public void setId(int id)
    { this.id = id; }


    private boolean doLearnButton;
    // process MIDI


    public void processEvents(VSTEvents events)
    {
	if (doLearnButton) //"Learn" the notes from Learn button
	{ events = doLearnButton(events); }

	List<VSTEvent> origEvents = VstUtils.cloneVSTEventsToList(events);
	List<TypedVSTEvent> newEvents = new ArrayList<TypedVSTEvent>();

	VstUtils.out(getDebugString());

	if (this.isGoodForProcessing() && enabled) //Instantiated and has usable values
	{
	    try
	    {
		newEvents.addAll(getRemappedNotesAndTheKitchenSinkSet(events));
	    } catch (InvalidMidiDataException e)
	    {}
	}
	else //Pass everything through 
	{
	    for (int i = 0; i < origEvents.size(); i++)
	    {
		newEvents.add(new TypedVSTEvent(origEvents.get(i), TypedVSTEvent.PASSTHROUGH));
	    }
	}

	p.processMidiFromRow(newEvents, this); //Now shoot those MIDI events back out to parent plugin for further processing and/or chaining
    }

    private List<TypedVSTEvent> getRemappedNotesAndTheKitchenSinkSet(VSTEvents origEvents) throws InvalidMidiDataException
    {
	List<VSTEvent> events = VstUtils.cloneVSTEventsToList(origEvents);

	List<TypedVSTEvent> cleanedEvents = new ArrayList<TypedVSTEvent>();
	VstUtils.out("Considering " + events.size() + " to clean");

	for (int i = 0; i < events.size(); i++)
	{
	    VSTEvent e = events.get(i);

	    if( e.getType() == VSTEvent.VST_EVENT_MIDI_TYPE ) 
	    {
		//out("Considering midi event...");

		byte[] msg_data = ((VSTMidiEvent)e).getData();
		int ctrl_index, ctrl_value, msg_status, msg_channel;
		msg_status = MidiUtils.getStatusFromMidiByteArray(msg_data);
		msg_channel = MidiUtils.getChannelFromMidiByteArray(msg_data);
		int status = MidiUtils.getStatusWithoutChannelByteFromMidiByteArray(msg_data);
		ctrl_index = MidiUtils.getData1FromMidiByteArray(msg_data);
		ctrl_value = MidiUtils.getData2FromMidiByteArray(msg_data);
		int noteNum = ctrl_index;

		if (msg_channel != inputChannel)//Incoming message not on captured input channel
		{
		    //Let it through, since it's not on the channel we're capturing
		    cleanedEvents.add(new TypedVSTEvent(e, TypedVSTEvent.PASSTHROUGH)); 
		}
		else if ((lowerNote != null)&&(upperNote != null)&&(lowerNoteMapped != null)) //also matches input channel we're capturing
		{
		    if (
			   ((status == ShortMessage.POLY_PRESSURE) || (status == ShortMessage.NOTE_ON) || (status == ShortMessage.NOTE_OFF))
			    &&  (noteNum >= lowerNote.getMidiNoteNumber() && (noteNum <= upperNote.getMidiNoteNumber())))
		    {
			cleanedEvents.add(new TypedVSTEvent(e, TypedVSTEvent.REMOVAL));  //remove original message

			int delta = noteNum - lowerNote.getMidiNoteNumber();
			ctrl_index = lowerNoteMapped.getMidiNoteNumber() + delta;

			//UIUtils.showAlert("Notenum is " + noteNum + " and lowerNote is " + lowerNote.getMidiNoteNumber() + " and going to convert it to " + ctrl_index + " and BTW lowerNoteMapped is " + lowerNoteMapped.getMidiNoteNumber());

			//Create new message (poly/noteon/noteoff) mapped to the new place
			ShortMessage s = new ShortMessage(status,  outputChannel - 1, ctrl_index, ctrl_value);
			VSTMidiEvent newEvent = new VSTMidiEvent();
			newEvent.setData(s.getMessage());
			VSTEvent event = new VSTEvent();
			event = newEvent;
			event.setType(VSTEvent.VST_EVENT_MIDI_TYPE); //Apparently this is needed...
			
			cleanedEvents.add(new TypedVSTEvent(VstUtils.convertMidiChannel(event, inputChannel, outputChannel), TypedVSTEvent.NEW)); 
		    }
		    else //Passing through messages as is, since they are not relevant to this logic 
			//and we don't want to drop them (here) by default
		    {
			cleanedEvents.add(new TypedVSTEvent(e, TypedVSTEvent.PASSTHROUGH));  //passthrough event
		    }
		}
		else //Matched input channel, but no range defined, so change all input channel messages to output channel
		{
			cleanedEvents.add(new TypedVSTEvent(e, TypedVSTEvent.REMOVAL));  //remove original message
			cleanedEvents.add(new TypedVSTEvent(VstUtils.convertMidiChannel(e, inputChannel, outputChannel), TypedVSTEvent.NEW)); 
		}
	    }
	}
	VstUtils.out("returning " + cleanedEvents.size());
	return cleanedEvents;
    }




    public GridPane getGuiRow() //Create a row in the UI
    { 
	int index = 0;

	//This is meant to be a single 1 row grid
	GridPane rowGrid = new GridPane();
	rowGrid.setPadding(new Insets(4));
	rowGrid.setHgap(10);
	rowGrid.setVgap(5);
	rowGrid.setStyle("-fx-background-color: #C0C0C0;");

	//int numWidth = 50; //width of text fields that contain numbers

	TextField tfRowName = new TextField();
	if (getName() != null)
	{ tfRowName.setText(getName()); }
	else 
	{ tfRowName.setText("Row " + getId()); }
	tfRowName.setId("" + getId());  //The "" in front makes it cast the input as a string
	tfRowName.setMaxWidth(90);
	tfRowName.setStyle("-fx-background-color: #A0A0A0;");
	tfRowName.textProperty().addListener((observable, oldValue, newValue) -> {
	    HandleRowName(tfRowName);
	});
	//tfRowName.setOnAction(e -> this.HandleRowName(tfRowName));  //Apparently this is only if Return is pressed
	rowGrid.add(tfRowName,index,0);
	index++;

	CheckBox cb = new CheckBox();
	cb.setText("");
	if (enabled)
	{ cb.setSelected(true); }
	else
	{ cb.setSelected(false); }
	cb.setId("" + getId());
	//This style of event handling below wasn't supposedly available until Java 8, and it's quite nice
	cb.setOnAction(e -> HandleEnabledCheckbox(cb)); 
	rowGrid.add(cb,index,0);
	rowGrid.add(getRowLabel("Enabled"), index, 1);
	index++;

	
	String[] channels = new String[16];
	for (int i = 0; i < 16; i++){ channels[i] = Integer.toString(i + 1);}

	ComboBox<String> cbInputChannel = new ComboBox<String>();
	cbInputChannel.getItems().addAll(channels);
	if (inputChannel >= 0)
	{ //Here getting string instead of int so we pick the item based on the visible GUI value
	    cbInputChannel.getSelectionModel().select(Integer.toString(inputChannel)); }
	else
	{ cbInputChannel.getSelectionModel().select(0);}//Auto select channel 1 for input }
	cbInputChannel.setId("" + getId());
	cbInputChannel.setOnAction(e -> HandleInChannelCombo(cbInputChannel));
	rowGrid.add(cbInputChannel,index,0);
	rowGrid.add(getRowLabel("Input Channel"),index,1);
	HandleInChannelCombo(cbInputChannel);
	index++;

	ComboBox<String> cbOutputChannel = new ComboBox<String>();
	cbOutputChannel.getItems().addAll(channels);
	if (outputChannel >= 0)
	{ cbOutputChannel.getSelectionModel().select(Integer.toString(outputChannel)); }
	else
	{ cbOutputChannel.getSelectionModel().select(0);}//Auto select channel 1 for input }
	cbOutputChannel.setId("" + getId());
	cbOutputChannel.setOnAction(e -> HandleOutChannelCombo(cbOutputChannel));
	rowGrid.add(cbOutputChannel,index,0);
	rowGrid.add(getRowLabel("Output Channel"),index,1);
	HandleOutChannelCombo(cbOutputChannel);
	index++;

	learnBtn = new Button(); //Declaring this early so we can use it with the toggle switch 
	learnBtn.setText("Learn range");
	if ((lowerNote != null)&&(upperNote != null)&&(lowerNoteMapped != null))
	{ learnBtn.setText(lowerNote.getNoteNamePlusOctave() + "/" + upperNote.getNoteNamePlusOctave() + "-" + lowerNoteMapped.getNoteNamePlusOctave()); }
	learnBtn.setId("" + getId());
	learnBtn.setMinWidth(77);
	learnBtn.setOnAction(e -> HandleLearnButton(learnBtn)); 
	rowGrid.add(learnBtn,index,0);
	index++;


	Button delBtn = new Button();
	delBtn.setText("Delete");
	delBtn.setId("" + getId());
	delBtn.setOnAction(e ->  p.gui.removeRow(rowGrid, Integer.parseInt(delBtn.getId()))); 
	rowGrid.add(delBtn,index,0);
	index++;

	rowHeight = (int)rowGrid.heightProperty().doubleValue(); //so we know how high these things are


	return rowGrid;

	//VstUtils.out("poly collection now has " + ((PolyTool)plugin).getPolyCollection().size());
    }



    private Label getRowLabel(String text)
    {
	Label label = new Label();
	label.setFont(new Font("Arial", 9));
	label.setText(text);
	return label;
    }

    public int getRowHeight()
    {
	return (int) rowHeight;
    }

    private void HandleLearnButton(Button learnBtn)
    {
	doLearnButton = true;
	upperNote = null;
	lowerNote = null;
	lowerNoteMapped = null;
	Platform.runLater(new Runnable(){
	    @Override
	    public void run()
	    { learnBtn.setText("Play lower note");
	    }
	});
    }

    private void HandleEnabledCheckbox(CheckBox cb)
    {
	Boolean checked = cb.selectedProperty().get();
	enabled = checked;
    }


    private void HandleInChannelCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	inputChannel = (Integer.parseInt(value));
    }

    private void HandleOutChannelCombo(ComboBox cb)
    {
	String value = cb.getValue().toString();
	outputChannel = (Integer.parseInt(value));
    }



    private void HandleRowName(TextField cb)
    {
	String value = cb.getText();
	setName(value);
    }




    //This is called when a learn button has been pressed and the next midi events come in
    //Find the NOTE ON (if there is one), assign it to the Learned Button, and let all other messages be returned (to continue their flow)
    private VSTEvents doLearnButton(VSTEvents events)
    {
	List<VSTEvent> newEvents = new ArrayList<VSTEvent>();
	Boolean foundNoteOn = false;
	for (int i = 0; i < events.getNumEvents(); i++)
	{
	    VSTEvent event = events.getEvents()[i];
	    RangeRow row = this;
	    if (event.getType() == VSTEvent.VST_EVENT_MIDI_TYPE)
	    {
		byte[] msg_data = ((VSTMidiEvent)event).getData();
		int status = MidiUtils.getStatusWithoutChannelByteFromMidiByteArray(msg_data);

		if ((status == ShortMessage.NOTE_ON) && (foundNoteOn == false))
		{
		    //We have our note
		    try
		    {
			Platform.runLater(new Runnable(){
			    @Override
			    public void run()
			    { 
				try
				{
				    String btnText = "";

				    Note n = MidiUtils.getNote(MidiUtils.getShortMessage((VSTMidiEvent)event));

				    if (lowerNote == null)
				    { 
					lowerNote = n;
					btnText = "Play upper note";
				    }
				    else if (upperNote == null)
				    { 
					if (n.getMidiNoteNumber() < lowerNote.getMidiNoteNumber())
					{
					    UIUtils.showAlert("You can't play a note lower than the low note!");
					}
					else
					{
					    upperNote = n;

					    try
					    {
						btnText = "Play range low";
					    }
					    catch (Exception e)
					    {
					    }
					}
				    }
				    else 
				    {
					lowerNoteMapped = n;
					btnText = (lowerNote.getNoteNamePlusOctave() + "/" + upperNote.getNoteNamePlusOctave() + "-" + lowerNoteMapped.getNoteNamePlusOctave());
					doLearnButton = false;
					//learnBtn = null;
				    }

				    learnBtn.setText(btnText);
				} catch (Exception e)
				{ UIUtils.showAlert(VstUtils.getStackTrace(e));}
			    }
			});
			foundNoteOn = true; //So that we don't use other note ons

		    } catch (Exception e)
		    {
			VstUtils.out(e.getMessage()); 
		    }
		}
		else //Add to returned events this event since it is NOT the Note On
		{
		    newEvents.add(events.getEvents()[i]);
		}
	    }
	}
	return VstUtils.convertToVSTEvents(newEvents);
    }




}
