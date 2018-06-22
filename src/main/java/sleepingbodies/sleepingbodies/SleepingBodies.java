package sleepingbodies.sleepingbodies;

import me.dpohvar.powernbt.PowerNBT;
import me.dpohvar.powernbt.api.NBTManager;
import org.bukkit.plugin.java.JavaPlugin;
import sleepingbodies.sleepingbodies.Data.Database;
import sleepingbodies.sleepingbodies.Data.SQLite;

import java.io.File;

public final class SleepingBodies extends JavaPlugin {
    static Database db;
    static NBTManager manager = PowerNBT.getApi();

    @Override
    public void onEnable() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                saveDefaultConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        db = new SQLite(this);
        db.load();

        Bodies.refreshBodies();
        getServer().getPluginManager().registerEvents(new Listeners(), this);
        getServer().getPluginManager().registerEvents(new Bodies.Listeners(), this);
    }

    @Override
    public void onDisable() {

    }
}
