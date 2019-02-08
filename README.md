# rjmjVSTwRapperVSTs

The main goal behind this project is to build VSTs that primarily focus on MIDI input/output logic instead of audio.  A driving goal of this was to transform polyphonic aftertouch and channel pressure into CC mappings.

This project uses the available java-based jVSTwRapper libraries (http://jvstwrapper.sourceforge.net).  As of this writing I have included in this repository only the JAVA-BASED jVSTwRapper projects required to run the sample midi VSTs that are unique to this repository.  


In order to host a VST from rjmvst in a DAW, first create a directory containing the following:

1. JVST Wrapper DLL (or MAC/Linux library).  This can be sourced from https://sourceforge.net/projects/jvstwrapper/files/ or built from the native C++ project hosted here: http://jvstwrapper.cvs.sourceforge.net/viewvc/jvstwrapper/jvst_native/  
   - This is the VST DLL loaded by the DAW
   - This works fairly well with jBridge also (definitely in Windows for MIDI at least) if a 64bit DLL is needed (https://jstuff.wordpress.com/jbridge)
   


2. INI file (must match DLL name except for extension) and a sample of this is in this repository under /misc

3. JVST Wrapper Jar File (build from jvst_wrapper project)

4. JVST System Jar File (build from jvst_system project)

5. rjmvst.jar (or your own custom .jar file containing your VST logic)


You'll also need to have a Java JVM installed if you don't already.   More info available at http://jvstwrapper.sourceforge.net/
Official Location of jVSTwRapper source: http://jvstwrapper.cvs.sourceforge.net/viewvc/jvstwrapper/
  

On a side note, I'd like to make a huge shout-out to the creator of VstBoard (http://vstboard.blogspot.com/) as it has proved to be a fantastc VST Host chainer that works great with these VSTs.  More info on that journey here: https://github.com/JesperGoor/LaunchSequencer/issues/1



