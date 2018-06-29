package sleepingbodies.sleepingbodies;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;
import static sleepingbodies.sleepingbodies.Bodies.Listeners.invOwner;
import static sleepingbodies.sleepingbodies.Bodies.hbs;
import static sleepingbodies.sleepingbodies.Bodies.hbsToLoad;
import static sleepingbodies.sleepingbodies.Bodies.loadBody;
import static sleepingbodies.sleepingbodies.SleepingBodies.db;

public class Listeners implements Listener {
    private HashMap<Player, UUID> bodyToRemove = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        UUID u = p.getUniqueId();


        String hb = db.getHb(u);

        if(hb != null) {
            if(p.getHealth() == 0) {
                p.getInventory().clear();
                p.setHealth(0);
            } else {
                if(hbs.get(u) != null) {
                    p.teleport(hbs.get(u));
                    Bodies.delBody(u);
                } else {
                    refreshBodiesByChunk(p.getLocation().getChunk().getEntities());
                    p.teleport(hbs.get(u));
                    Bodies.delBody(u);
                }
            }
            db.clear(u);
            if(invOwner.containsValue(Bukkit.getOfflinePlayer(u))) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for(Map.Entry<Inventory, OfflinePlayer> entry : invOwner.entrySet()) {
                            if(entry.getValue().equals(Bukkit.getOfflinePlayer(u))) {
                                Inventory inv = entry.getKey();
                                List<HumanEntity> views = inv.getViewers();
                                for (HumanEntity view : views) {
                                    Player p2 = Bukkit.getPlayer(view.getUniqueId());
                                    p2.closeInventory();
                                }
                            }
                        }
                    }
                }.runTaskAsynchronously(SleepingBodies.getPlugin(SleepingBodies.class));
            }
        }
    }

    private List<Chunk> chunkToUnload = new ArrayList<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        UUID u = p.getUniqueId();

        Inventory inv = p.getInventory();
        ItemStack[] items = inv.getContents();

        Boolean notClear = false;
        for(ItemStack item : items) {
            if(item != null)
                notClear = true;
        }

        if(notClear) {
            chunkToUnload.add(p.getLocation().getChunk());
            PolarBear sl = Bodies.createHitbox(p, p.getHealth(), p.getMaxHealth());
            String as = Bodies.createBody(p);
            loadBody(u);
            db.setAs(u, as, sl.getUniqueId(), p.getWorld().getName(), (int) Math.round(p.getLocation().getX()), (int) Math.round(p.getLocation().getZ()));
        }
    }

    @EventHandler
    public void onLoadChunk(ChunkLoadEvent e) {
        Entity[] entities = e.getChunk().getEntities();

        refreshBodiesByChunk(entities);
    }

    private void refreshBodiesByChunk(Entity[] entities) {
        for(Entity entity : entities) {
            if(entity instanceof PolarBear) {
                if(Bodies.hbsToLoad.containsValue(entity.getUniqueId())) {
                    for (Map.Entry<UUID, UUID> hb : Bodies.hbsToLoad.entrySet()) {
                        if (hb.getValue().equals(entity.getUniqueId())) {
                            Bodies.loadBody(hb.getKey());
                        }
                    }
                }
            }
        }
        for(Entity entity : entities) {
            if(entity instanceof Player) {
                if(bodyToRemove.containsKey(((Player) entity).getPlayer())) {
                    entity.teleport(hbs.get(entity.getUniqueId()).getLocation());
                    Bodies.delBody(entity.getUniqueId());
                }
            }
        }
    }

    @EventHandler
    public void onUnloadChunk(ChunkUnloadEvent e) {
        Chunk c = e.getChunk();
        Entity[] entities = e.getChunk().getEntities();

        for(int i = 0; i < chunkToUnload.size(); i++) {
            if(c.equals(chunkToUnload.get(i))) {
                e.setCancelled(true);
                chunkToUnload.remove(c);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for(Entity entity : entities) {
                            if(entity instanceof PolarBear) {
                                if(Bodies.hbsToLoad.containsValue(entity.getUniqueId())) {
                                    for(Map.Entry<UUID, UUID> hb : Bodies.hbsToLoad.entrySet()) {
                                        if(hb.getValue().equals(entity.getUniqueId())) {
                                            Bodies.unloadBody(hb.getKey());
                                        }
                                    }
                                }
                            }
                        }
                        e.setCancelled(false);
                        e.setSaveChunk(true);
                    }
                }.runTaskLater(getPlugin(SleepingBodies.class), 10);
            }
        }
    }
}
