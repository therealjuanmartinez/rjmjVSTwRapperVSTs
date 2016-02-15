# rjmjVSTwRapperVSTs

The main goal behind this project is to build VSTs that primarily focus on MIDI input/output logic instead of audio.  A driving goal of this was originally to transform polyphonic aftertouch and channel pressure into CC mappings.

As of this writing there is NO GUI for any VST that I have authored but I hope to begin some work in this area as well.

This project uses the available java-based jVSTwRapper libraries (jvstwrapper.sourceforge.net).  As of this writing I have included in this repository only the JAVA-BASED jVSTwRapper projects required to run the sample midi VSTs in rjmvst.  


In order for a VST from rjmvst to be actually hosted in a DAW, a directory will be needed containing the following:

1. JVST Wrapper DLL (or MAC/Linux library).  This can be sourced from https://sourceforge.net/projects/jvstwrapper/files/ or built from the native C++ project hosted here: http://jvstwrapper.cvs.sourceforge.net/viewvc/jvstwrapper/jvst_native/  
   - This is the VST DLL loaded by the DAW
   - This works great with jBridge also (definitely in Windows for MIDI at least) if a 64bit DLL is needed (https://jstuff.wordpress.com/jbridge)
   


2. INI file (must match DLL name except for extension) and a sample of this is in this repository under /misc

3. JVST Wrapper Jar File (build from jvst_wrapper project)

4. JVST System Jar File (build from jvst_wrapper project)

5. rjmvst.jar (or your own custom .jar file containing your VST logic)


In addition you'll need to make sure you have a Java JVM setup.   More info available at http://jvstwrapper.sourceforge.net/
  



