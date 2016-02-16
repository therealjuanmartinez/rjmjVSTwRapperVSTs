package com.rjm.vst;

import java.util.List;

import jvst.wrapper.valueobjects.VSTEvent;
import jvst.wrapper.valueobjects.VSTEvents;

public class Utils {
    
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

}
