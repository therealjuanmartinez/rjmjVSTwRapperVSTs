package rjm.vst.tools;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import jvst.wrapper.valueobjects.VSTEvent;
import jvst.wrapper.valueobjects.VSTEvents;
import jvst.wrapper.valueobjects.VSTMidiEvent;

public class VstUtils {
    
    public static VSTEvents convertToVSTEvents(List<VSTEvent> events)
    {
	VSTEvent[] newVstEvents = new VSTEvent[events.size()];
	for (int i = 0; i < newVstEvents.length; i++)
	{ newVstEvents[i] = events.get(i); }
	VSTEvents eventsOut = new VSTEvents();
	eventsOut.setEvents(newVstEvents);
	eventsOut.setNumEvents(newVstEvents.length); //Seems this needed.  For now I'd rather not modify base jvstwrapper code to correct this
	return eventsOut;
    }
    
    public static VSTEvent createVstMidiEventFromShortMessage(ShortMessage s)
    {
        VSTMidiEvent newEvent = new VSTMidiEvent();
	newEvent.setData(s.getMessage());
        VSTEvent v = new VSTEvent();
        v = newEvent;
        v.setType(VSTEvent.VST_EVENT_MIDI_TYPE); //Apparently this is needed...
        return newEvent;
    }
    

    //Insert a new event into an existing VSTEvents collection at the specified index
    public static VSTEvents getVstEventsWithNewVstEventInserted(int index, VSTEvents events, VSTEvent newEvent)
    {
	VSTEvent[] eventsOut = new VSTEvent[events.getNumEvents() + 1];
	
	for (int i = 0; i < eventsOut.length; i++)
	{
	    out("i is " + i);
	    if (i < index)
	    { eventsOut[i] = events.getEvents()[i]; }
	    if (i == index)
	    { eventsOut[i] = newEvent; }
	    if (i > index)
	    { eventsOut[i] = events.getEvents()[i - 1]; }
	}

	VSTEvents out = new VSTEvents();
	//out("out has " + out.getNumEvents() + " items");
	out.setEvents(eventsOut);
	out.setNumEvents(out.getEvents().length); //OK eventually I will start editing the wrapper code... MAN....
	//out("out now yo has " + out.getNumEvents() + " items");
	return out;
    }
    
    
    
    /** Read the object from Base64 string. */
    public static Object fromString( String base64string ) throws IOException , ClassNotFoundException {
         byte [] data = Base64.getDecoder().decode( base64string );
         ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
         Object o  = ois.readObject();
         ois.close();
         return o;
    }

     /** Write a serializable object to a Base64 string. */
     public static String toString( Serializable o ) throws IOException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream( baos );
         oos.writeObject( o ); oos.close();
         return Base64.getEncoder().encodeToString(baos.toByteArray()); 
     }
     
     
     
     public static VSTEvents convertMidiChannel(VSTEvents inputEvents, int inputChannel, int outputChannel)
     {

 	List<VSTEvent> outputEvents = new ArrayList<VSTEvent>();

 	for (int i = 0; i < inputEvents.getEvents().length; i++)
 	{
 	    VSTEvent e = inputEvents.getEvents()[i];

 	    if( e.getType() == VSTEvent.VST_EVENT_MIDI_TYPE ) 
 	    {
 		byte[] msg_data = ((VSTMidiEvent)e).getData();

 		int ctrl_index, ctrl_value, msg_status, msg_channel;
 		msg_status = ( msg_data[ 0 ] & 0xF0 ) >> 4;
 		if( msg_status == 0xF ) {
 		    /* Ignore system messages.*/
 		    //return;
 		}
 		msg_channel = ( msg_data[ 0 ] & 0xF ) + 1;
 		ctrl_index = msg_data[ 1 ] & 0x7F;
 		ctrl_value = msg_data[ 2 ] & 0x7F;

 		int status = (int) (e.getData()[0] & 0xFF) - msg_channel + 1; //different but related to msg_status
 		//out("channel from message is " + msg_channel + " and inputChannel is " + inputChannel);

 		if ((msg_channel) == inputChannel) //This is the channel we want to remap
 		{
 		    msg_channel = outputChannel - 1;
 		    ShortMessage s;
 		    try //Build new MIDI message based on old one but with new channel
 		    {
 			s = new ShortMessage(status, msg_channel, ctrl_index, ctrl_value);
 			VSTMidiEvent newMidiEvent = new VSTMidiEvent();
 			newMidiEvent.setData(s.getMessage());
 			VSTEvent v = new VSTEvent();
 			v = newMidiEvent;
 			v.setType(VSTEvent.VST_EVENT_MIDI_TYPE); //Apparently this is needed...
 			outputEvents.add(v);
 			//out("Adding event to outputEvents.. ");
 		    } catch (InvalidMidiDataException e1)
 		    {
 			// TODO Auto-generated catch block
                         //out(e1.getStackTrace().toString());
 		    }
 		}

 	    }
 	    else //Ignore i.e. simply pass through the message without modification
 	    { outputEvents.add(e); }
 	    
 	}
 	    return convertToVSTEvents(outputEvents);
    
     }

    public static void out(String message)
    {

	//This function as of right now is a total hack and should only be used for debugging purposes and is known to slightly degrade plugin performance

	if (!true) //This prevents this hack of a logging function from running unless manually enabled
	{ return; }

	try
	{
	    //HACK JMM
	    //TODO remove this thing
	    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(

		    new FileOutputStream("c:\\temp\\jwrapper_log.txt", true), "UTF-8"));

	    try
	    {
		writer.write(message + "\n");
		writer.close();
	    } catch (IOException e)
	    {
		try
		{
		    writer.write(message + "\n");
		    writer.close();
		} catch (IOException e1)
		{
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		}
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	} catch (UnsupportedEncodingException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (FileNotFoundException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    

    public static void outputVstMidiEventsForDebugPurposes(VSTEvents ev)
    {
	for (int i = 0; i < ev.getEvents().length; i++)
	{
	    VSTEvent e = ev.getEvents()[i];

	    if( e.getType() == VSTEvent.VST_EVENT_MIDI_TYPE ) 
	    {
		byte[] msg_data = ((VSTMidiEvent)e).getData();

		int ctrl_index, ctrl_value, msg_status, msg_channel;
		msg_status = ( msg_data[ 0 ] & 0xF0 ) >> 4;
		if( msg_status == 0xF ) {
		    /* Ignore system messages.*/
		    //return;
		}
		msg_channel = ( msg_data[ 0 ] & 0xF ) + 1;

		ctrl_index = msg_data[ 1 ] & 0x7F;
		ctrl_value = msg_data[ 2 ] & 0x7F;

		/*
	                        case 0x8: /* Note off.*/
		//case 0x9: /* Note on.*/
		//case 0xB: /* Control change.*/
		//case 0xC: /* Program change.*/
		//case 0xE: /* Pitch wheel.*/

		int status = (int) (e.getData()[0] & 0xFF) - msg_channel + 1; //different but related to msg_status

		out("Status for incoming message is " + status);

		ShortMessage s = new ShortMessage();

		out("\n");
		Class<ShortMessage> c = ShortMessage.class;
		for (java.lang.reflect.Field f : c.getDeclaredFields()) {
		    int mod = f.getModifiers();
		    if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod)) {
			try {
			    //System.out.printf("%s = %d%n", f.getName(), f.get(null));
			    Integer code = (Integer)f.get(null);
			    if (status == code.intValue())
			    {
				//Print the type of message we've received
				out(String.format("%s = %d", f.getName(), f.get(null)));
			    }
			} catch (IllegalAccessException e2) {
			    out("ERROR doing the comparison thing");
			    e2.printStackTrace();
			}
		    }
		}
		out("Channel: " + msg_channel);
		out("Status: " + msg_status);
		out("Value: " + ctrl_value);
		out("Index: " + ctrl_index);
	    }
	    else {
		out("Not midi?");}
	    }
	}	
    }

