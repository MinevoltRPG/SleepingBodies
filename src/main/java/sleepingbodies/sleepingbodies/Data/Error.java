package sleepingbodies.sleepingbodies.Data;

import sleepingbodies.sleepingbodies.SleepingBodies;

import java.util.logging.Level;

public class Error {
    public static void execute(SleepingBodies plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    public static void close(SleepingBodies plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}
