package rjm.vst.midi.polytool;

import java.io.Serializable;

import javafx.scene.layout.GridPane;
import jvst.wrapper.valueobjects.VSTEvents;
import rjm.midi.tools.Note;

public interface MidiRow extends Serializable {
    
    public String getDebugString();
    public boolean getEnabled();
    public int getId();
    public boolean isGoodForProcessing();
    public void setEnabled(boolean x);
    public void setId(int x);
    public void submitRealtimeValue(int x);
    public void processEvents(VSTEvents e);
    public void setPlugin(PolyTool p); 
    public GridPane getGuiRow();
}
