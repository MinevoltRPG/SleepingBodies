package sleepingbodies.sleepingbodies;

import me.dpohvar.powernbt.PowerNBT;
import me.dpohvar.powernbt.api.NBTManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sleepingbodies.sleepingbodies.Data.Database;
import sleepingbodies.sleepingbodies.Data.SQLite;

import java.io.File;

public final class SleepingBodies extends JavaPlugin {
    static Database db;
    static NBTManager manager = PowerNBT.getApi();

    @Override
    public void onEnable() {
        if(this.getServer().getPluginManager().getPlugin("PowerNBT") == null) {
            getLogger().warning("Плагин PowerNBT не найден.");
            for(Player p : this.getServer().getOnlinePlayers()) {
                if(p.isOp()) {
                    p.spigot().sendMessage(new ComponentBuilder("[SleepingBodies]")
                    .append(" Плагин PowerNBT не найден").color(ChatColor.RED)
                    .create());
                }
            }
            return;
        }

        if(this.getServer().getPluginManager().getPlugin("OpenInv") == null) {
            getLogger().warning("Плагин OpenInv не найден.");
            for(Player p : this.getServer().getOnlinePlayers()) {
                if(p.isOp()) {
                    p.spigot().sendMessage(new ComponentBuilder("[SleepingBodies]").color(ChatColor.GRAY)
                            .append(" Плагин OpenInv не найден").color(ChatColor.RED)
                            .create());
                }
            }
            return;
        }

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
