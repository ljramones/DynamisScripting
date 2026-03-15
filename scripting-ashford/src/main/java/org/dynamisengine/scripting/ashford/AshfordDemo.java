package org.dynamisengine.scripting.ashford;

import org.dynamisengine.scripting.runtime.RuntimeTickResult;
import org.dynamisengine.scripting.runtime.ScriptingRuntime;

public final class AshfordDemo {
    private AshfordDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        // Assemble runtime programmatically (YAML loading deferred)
        ScriptingRuntime runtime = AshfordRuntimeAssembler.assemble();
        runtime.start();

        System.out.println("Ashford simulation starting...");
        System.out.println("Press Ctrl+C to stop.");

        AshfordDebugOverlay overlay = new AshfordDebugOverlay(runtime);

        for (int tick = 0; tick < 20; tick++) {
            RuntimeTickResult result = runtime.tick();
            overlay.printTick(result);
            Thread.sleep(100); // wall time pacing for demo only
        }

        runtime.stop();
        System.out.println("Ashford simulation complete.");
    }
}
