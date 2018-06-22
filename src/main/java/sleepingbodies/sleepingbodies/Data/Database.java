package sleepingbodies.sleepingbodies.Data;

import sleepingbodies.sleepingbodies.SleepingBodies;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public abstract class Database {
    SleepingBodies plugin;
    Connection connection;
    private String table = "players";
    Database(SleepingBodies instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public String getHb(UUID uuid) {
        Connection conn = null;
        String string = uuid.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE uuid = '"+string+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getString("uuid").equalsIgnoreCase(string.toLowerCase())){
                    return rs.getString("hb");
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            endConn(conn, ps);
        }
        return null;
    }

    public String getWorld(UUID uuid) {
        Connection conn = null;
        String string = uuid.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE uuid = '"+string+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getString("uuid").equalsIgnoreCase(string.toLowerCase())){
                    return rs.getString("world");
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            endConn(conn, ps);
        }
        return null;
    }

    public String getXZ(UUID uuid) {
        Connection conn = null;
        String string = uuid.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE uuid = '"+string+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getString("uuid").equalsIgnoreCase(string.toLowerCase())){
                    return rs.getString("xz");
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            endConn(conn, ps);
        }
        return null;
    }

    public void clear(UUID u) {
        Connection conn = null;
        String string = u.toString();
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM " + table + " WHERE uuid ='" + string + "';");

            ps.execute();
            return;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            endConn(conn, ps);
        }
    }



    public HashMap<UUID, UUID> getAllHb() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + ";");

            rs = ps.executeQuery();
            HashMap<UUID, UUID> uuids = new HashMap<>();
            while(rs.next()){
                uuids.put(UUID.fromString(rs.getString("uuid")), UUID.fromString(rs.getString("hb")));
            }
            return uuids;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            endConn(conn, ps);
        }
        return null;
    }

    public HashMap<UUID, String> getAllAs() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + ";");

            rs = ps.executeQuery();
            HashMap<UUID, String> uuids = new HashMap<>();
            while(rs.next()){
                uuids.put(UUID.fromString(rs.getString("uuid")), rs.getString("asS"));
            }
            return uuids;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            endConn(conn, ps);
        }
        return null;
    }

    public void setAs(UUID u, String as, UUID hb, String world, int x, int z) {
        String xz = x + "/" + z;
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + table + " (uuid,asS,hb,world,xz,die) VALUES(?,?,?,?,?,?)");
            ps.setString(1, u.toString());

            ps.setString(2, as);

            ps.setString(3, hb.toString());

            ps.setString(4, world);

            ps.setString(5, xz);

            ps.setInt(6, 0);

            ps.executeUpdate();
            return;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            endConn(conn, ps);
        }
    }


    private void endConn(Connection conn, PreparedStatement ps) {
        try {
            if (ps != null)
                ps.close();
            if (conn != null)
                conn.close();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
        }
    }

    public void close(PreparedStatement ps,ResultSet rs){
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }
}
