package gr.forth.ics.memorymeasurer;

import java.lang.instrument.Instrumentation;

/**
 * Agent call-back that stores the {@link Instrumentation} provided by the JVM.
 * 
 * <p>Not to be used directly.
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class InstrumentationGrabber {
    public static void premain(String agentArgs, Instrumentation inst) {
        MemoryMeasurer.init(inst);
    }
}
