package sleepingbodies.sleepingbodies.Data;

import sleepingbodies.sleepingbodies.SleepingBodies;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLite extends Database {
    private String dbname;

    public SQLite(SleepingBodies instance){
        super(instance);
        dbname = "slbodies";
    }

    public String SQLiteCreateTokensTable = "CREATE TABLE IF NOT EXISTS players (" +
            "`uuid` varchar(64) NOT NULL," +
            "`asS` varchar(128) NOT NULL," +
            "`hb` varchar(64) NOT NULL," +
            "`world` varchar(64) NOT NULL," +
            "`xz` varchar(64) NOT NULL," +
            "`die` int(1) NOT NULL," +
            "PRIMARY KEY (`uuid`)" +
            ");";


    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), dbname+".db");
        if (!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: "+dbname+".db");
            }
        }
        try {
            if(connection!=null&&!connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateTokensTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}