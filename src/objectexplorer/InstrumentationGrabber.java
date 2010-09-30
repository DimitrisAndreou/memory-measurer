package objectexplorer;

import com.google.common.base.Preconditions;
import java.lang.instrument.Instrumentation;

/**
 * Agent call-back that stores the {@link Instrumentation} provided by the JVM.
 * 
 * <p>Not to be used directly.
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
public class InstrumentationGrabber {
    private static volatile Instrumentation instrumentation;

    public static void premain(String agentArgs, Instrumentation inst) {
        if (InstrumentationGrabber.instrumentation != null) throw new AssertionError("Already initialized");
        InstrumentationGrabber.instrumentation = inst;
    }

    private static void checkSetup() {
        Preconditions.checkState(instrumentation != null, "Instrumentation is not setup properly. "
                + "You have to pass -javaagent:path/to/object-explorer.jar to the java interpreter");
    }

    static Instrumentation instrumentation() {
        checkSetup();
        return instrumentation;
    }
}
