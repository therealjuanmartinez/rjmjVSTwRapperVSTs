package rjm.vst.midi.polytool;

import jvst.wrapper.valueobjects.VSTEvents;
import rjm.midi.tools.Note;

public interface MidiRow {
    
    public int getAverageValue();
    public String getDebugString();
    public boolean getEnabled();
    public int getId();
    public int getInputChannel();
    public int getMaxOutputValue();
    public int getMinOutputValue();
    public String getName();
    public Note getNote();
    public int getNoteOffCCValue();
    public int getOutputCCNum();
    public int getOutputChannel();
    public boolean isDoingAveraging();
    public boolean isGoodForProcessing();
    public boolean isInverse();
    public boolean isPlayNotesActive();
    public boolean isUseAllKeys();
    public void setEnabled(boolean x);
    public void setId(int x);
    public void setInputChannel(int x);
    public void setInverse(boolean x);
    public void setIsDoingAveraging(boolean x);
    public void setIsPlayNotesActive(boolean x);
    public void setMaxOutputValue(int x);
    public void setMinOutputValue(int x);
    public void setName(String x);
    public void setNote(Note x);
    public void setNoteOffCCValue(int x);
    public void setOutputCCNum(int x);
    public void setOutputChannel(int x);
    public void setUseAllKeys(boolean x);
    public void submitRealtimeValue(int x);
    public void processEvents(VSTEvents e);
    

}
