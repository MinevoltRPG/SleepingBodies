package sleepingbodies.sleepingbodies;



import com.google.common.collect.Sets;
import com.lishid.openinv.OpenInv;
import me.dpohvar.powernbt.api.NBTCompound;
import me.dpohvar.powernbt.api.NBTList;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;

import static sleepingbodies.sleepingbodies.SleepingBodies.db;
import static sleepingbodies.sleepingbodies.SleepingBodies.manager;

public class Bodies {
    public static HashMap<UUID, UUID> hbsToLoad = new HashMap<>();
    private static HashMap<UUID, List<UUID>> asSToLoad = new HashMap<>();
    private static HashMap<UUID, BukkitRunnable> cachedRunnables = new HashMap<>();
    static HashMap<UUID, PolarBear> hbs = new HashMap<>();
    static HashMap<UUID, List<ArmorStand>> asS = new HashMap<>();
    private static JavaPlugin plugin = SleepingBodies.getPlugin(SleepingBodies.class);

    public static String createBody(Player p) {
        List<UUID> as = createBodyTexture(p);
        return as.get(0).toString() + "/" + as.get(1).toString() + "/" + as.get(2).toString() + "/" + as.get(3).toString() + "/" + as.get(4).toString();
    }

    public static void delBody(UUID u) {
        if(hbs.get(u) == null)
            return;
        PolarBear sl = hbs.get(u);
        List<ArmorStand> as = asS.get(u);
        hbsToLoad.remove(u);
        asSToLoad.remove(u);
        cachedRunnables.get(u).cancel();
        cachedRunnables.remove(u);
        asS.remove(u);
        hbs.remove(u);
        sl.remove();
        for(ArmorStand a : as) {
            a.remove();
        }
    }

    public static void refreshBodies() {
        hbsToLoad.clear();
        asSToLoad.clear();
        HashMap<UUID, UUID> uuids = db.getAllHb();
        HashMap<UUID, String> asUuids = db.getAllAs();
        for (Map.Entry<UUID, UUID> entry : uuids.entrySet()) {
            hbsToLoad.put(entry.getKey(), entry.getValue());
        }
        for(Map.Entry<UUID, String> entry : asUuids.entrySet()) {
            String[] asD = entry.getValue().split("/");
            List<UUID> asL = new ArrayList<>();
            for(String as : asD) {
                asL.add(UUID.fromString(as));
            }
            asSToLoad.put(entry.getKey(), asL);

            String w = db.getWorld(entry.getKey());
            World world = Bukkit.getServer().getWorld(w);
            String xz = db.getXZ(entry.getKey());
            String xzD[] = xz.split("/");
            Chunk chunk = world.getChunkAt(Integer.valueOf(xzD[0]), Integer.valueOf(xzD[1]));
            if(chunk.isLoaded())
                loadBody(entry.getKey());

        }
    }

    public static void loadBody(UUID owner) {
        PolarBear hb = (PolarBear) Bukkit.getEntity(hbsToLoad.get(owner));
        if(hb == null)
            return;
        List<ArmorStand> asL = new ArrayList<>();
        for(UUID asU : asSToLoad.get(owner)) {
            asL.add((ArmorStand) Bukkit.getEntity(asU));
        }
        overrideBehavior(hb);

        hbs.put(owner, hb);
        asS.put(owner, asL);

        BukkitRunnable br = new BukkitRunnable() {
            @Override
            public void run() {
                Location loc1 = hb.getLocation().clone();
                loc1.setDirection(new Vector(0, 0, 0));
                loc1.setYaw(0);
                loc1.setY(loc1.getY() - 1.25);
                loc1.setZ(loc1.getZ() + 0.5);
                Location loc2 = hb.getLocation().clone();
                loc2.setDirection(new Vector(0, 0, 0));
                loc2.setYaw(0);
                loc2.setY(loc2.getY() - 0.65);

                Location text = hb.getLocation().clone();
                text.setZ(text.getZ()+0.75);
                text.setY(text.getY()+1.25);

                if(hb.hasPotionEffect(PotionEffectType.GLOWING))
                    hb.removePotionEffect(PotionEffectType.GLOWING);

                asL.get(0).teleport(loc1);
                asL.get(1).teleport(loc2);
                asL.get(2).teleport(text);
                text.setY(text.getY()-0.25);
                asL.get(3).teleport(text);
                text.setY(text.getY()-0.25);
                asL.get(4).teleport(text);
            }
        };
        br.runTaskTimer(SleepingBodies.getPlugin(SleepingBodies.class), 1, 1);
        cachedRunnables.put(owner, br);
    }

    public static void unloadBody(UUID owner) {
        hbs.remove(owner);
        asS.remove(owner);
        cachedRunnables.get(owner).cancel();
        cachedRunnables.remove(owner);
    }

    private static List<UUID> createBodyTexture(Player p) {
        Location newLoc = p.getLocation().clone();
        newLoc.setYaw(0);
        newLoc.setDirection(new Vector(0,0,0));
        newLoc.setY(p.getLocation().getY()-1.25);
        newLoc.setZ(p.getLocation().getZ()+0.5);
        ArmorStand body = createAs(p.getWorld(), newLoc, false);
        newLoc = p.getLocation().clone();
        newLoc.setYaw(0);
        newLoc.setDirection(new Vector(0,0,0));
        newLoc.setY(newLoc.getY()-0.65);
        ArmorStand legs = createAs(p.getWorld(), newLoc, false);

        newLoc = p.getLocation().clone();
        newLoc.setYaw(0);
        newLoc.setDirection(new Vector(0,0,0));
        newLoc.setY(newLoc.getY()+1.25);
        newLoc.setZ(newLoc.getZ()+0.75);
        ArmorStand uName = createAs(p.getWorld(), newLoc, true);
        if(!plugin.getConfig().getBoolean("nickHide")) {
//            if(plugin.getServer().getPluginManager().getPlugin("Hider") != null) {
//                if(plugin.getConfig().getBoolean("hider")) {
//                    uName.setCustomName(Hider.generateNick(plugin.getServer().getPluginManager().getPlugin("Hider").getConfig()));
//                } else {
//                    uName.setCustomName(p.getDisplayName());
//                }
//            } else {
//                uName.setCustomName(p.getDisplayName());
//            }
            uName.setCustomName(p.getDisplayName());
            uName.setCustomNameVisible(true);
        }

        newLoc = p.getLocation().clone();
        newLoc.setYaw(0);
        newLoc.setDirection(new Vector(0,0,0));
        newLoc.setY(newLoc.getY()+1.0);
        newLoc.setZ(newLoc.getZ()+0.75);
        ArmorStand uHp = createAs(p.getWorld(), newLoc, true);
        if(!plugin.getConfig().getBoolean("hpHide")) {
            uHp.setCustomName(drawHp(p.getHealth(), p.getMaxHealth()));
            uHp.setCustomNameVisible(true);
        }

        newLoc = p.getLocation().clone();
        newLoc.setYaw(0);
        newLoc.setDirection(new Vector(0,0,0));
        newLoc.setY(newLoc.getY()+0.75);
        newLoc.setZ(newLoc.getZ()+0.75);
        ArmorStand uLoot = createAs(p.getWorld(), newLoc, true);

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(p.getPlayerListName());
        skull.setItemMeta(meta);

        ItemStack curBody = p.getEquipment().getChestplate();
        ItemStack curLeg = p.getEquipment().getLeggings();
        ItemStack curBoots = p.getEquipment().getBoots();

        ItemStack l_body = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        LeatherArmorMeta l_bodyM = (LeatherArmorMeta) l_body.getItemMeta();
        l_bodyM.setColor(Color.BLACK);
        l_body.setItemMeta(l_bodyM);

        ItemStack l_leg = new ItemStack(Material.LEATHER_LEGGINGS, 1);
        LeatherArmorMeta l_legM = (LeatherArmorMeta) l_leg.getItemMeta();
        l_legM.setColor(Color.BLACK);
        l_leg.setItemMeta(l_legM);

        ItemStack l_boots = new ItemStack(Material.LEATHER_BOOTS, 1);
        LeatherArmorMeta l_bootsM = (LeatherArmorMeta) l_boots.getItemMeta();
        l_bootsM.setColor(Color.BLACK);
        l_boots.setItemMeta(l_bootsM);

        body.getEquipment().setHelmet(skull);

        if(curBody == null)
            body.getEquipment().setChestplate(l_body);
        else
            body.getEquipment().setChestplate(curBody);

        if(curLeg == null)
            legs.getEquipment().setLeggings(l_leg);
        else
            legs.getEquipment().setLeggings(curLeg);

        if(curBoots == null)
            legs.getEquipment().setBoots(l_boots);
        else
            legs.getEquipment().setBoots(curBoots);

        body.setHeadPose(new EulerAngle(1.65, 0, 3.15));
        body.setBodyPose(new EulerAngle(1.65, 0, 3.15));
        body.setLeftArmPose(new EulerAngle(1.65, 0, 3.15));
        body.setRightArmPose(new EulerAngle(1.65, 0, 3.15));

        legs.setLeftLegPose(new EulerAngle(1.55, 0, 3.15));
        legs.setRightLegPose(new EulerAngle(1.55, 0, 3.15));

        List<UUID> asU = new ArrayList<>();
        asU.add(body.getUniqueId());
        asU.add(legs.getUniqueId());
        asU.add(uName.getUniqueId());
        asU.add(uHp.getUniqueId());
        asU.add(uLoot.getUniqueId());
        asSToLoad.put(p.getUniqueId(), asU);

        return asU;
    }

    private static String drawHp(double hp, double maxHp) {
        DecimalFormat f = new DecimalFormat("#0.0");
        if(hp > (maxHp/100)*50) {
            return plugin.getConfig().getString("hpPrefix_1").replace("&", "§") + f.format(hp) + "/" + maxHp;
        } else if(hp < (maxHp/100)*25) {
            return plugin.getConfig().getString("hpPrefix_3").replace("&", "§") + f.format(hp) + "/" + maxHp;
        } else {
            return plugin.getConfig().getString("hpPrefix_2").replace("&", "§") + f.format(hp) + "/" + maxHp;
        }
    }

    public static PolarBear createHitbox(Player p, double health, double mHealth) {
        Location loc = p.getLocation();
        LivingEntity sl = (LivingEntity) p.getWorld().spawnEntity(loc, EntityType.POLAR_BEAR);

        sl.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 2147483647, 1, false, false));
        sl.setMaxHealth(mHealth);
        sl.setHealth(health);
        sl.setSilent(true);
        EntityEquipment ee = p.getEquipment();
        sl.getEquipment().setHelmet(ee.getHelmet());
        sl.getEquipment().setChestplate(ee.getChestplate());
        sl.getEquipment().setLeggings(ee.getLeggings());
        sl.getEquipment().setBoots(ee.getBoots());

        overrideBehavior(sl);

        hbsToLoad.put(p.getUniqueId(), sl.getUniqueId());

        return (PolarBear) sl;
    }

    private static void overrideBehavior(LivingEntity e) {
        EntityPolarBear c = (EntityPolarBear) ((CraftEntity)e).getHandle();
        //This gets the EntityCreature, we need it to change the values

        try {
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);
            bField.set(c.goalSelector, Sets.newLinkedHashSet());
            bField.set(c.targetSelector, Sets.newLinkedHashSet());
            cField.set(c.goalSelector, Sets.newLinkedHashSet());
            cField.set(c.targetSelector, Sets.newLinkedHashSet());
            //this code clears fields B, C. so right now the mob wont walk

        } catch (Exception exc) {exc.printStackTrace();}
    }

    private static ArmorStand createAs(World w, Location loc, boolean isMarker) {
        ArmorStand as = (ArmorStand) w.spawnEntity(loc, EntityType.ARMOR_STAND);
        as.setVisible(false);
        as.setGravity(false);
        as.setInvulnerable(true);
        if(isMarker)
            as.setMarker(true);

        return as;
    }

    public static class Listeners implements Listener {
        public static HashMap<Player, Inventory> openedBody = new HashMap<>();
        public static HashMap<Inventory, OfflinePlayer> invOwner = new HashMap<>();
        private static OpenInv openInv = (OpenInv) plugin.getServer().getPluginManager().getPlugin("OpenInv");

        @EventHandler
        public void onDamage(EntityDamageByEntityEvent e) {
            Entity entity = e.getDamager();
            if(entity instanceof PolarBear) {
                if (hbs.containsValue(entity)) {
                    e.setCancelled(true);
                }
            }
        }

        @EventHandler
        public void onDamage(EntityDamageEvent e) {
            Entity entity = e.getEntity();
            if(entity instanceof PolarBear) {
                if (hbs.containsValue(entity)) {
                    for (Map.Entry<UUID, PolarBear> entry : hbs.entrySet()) {
                        if (entry.getValue().equals(entity)) {
                            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);
                            OfflinePlayer opl = Bukkit.getOfflinePlayer(entry.getKey());
                            NBTCompound player = manager.readOfflinePlayer(opl);
                            player.put("Health", ((PolarBear) entity).getHealth()-e.getDamage());

                            for (Map.Entry<UUID, List<ArmorStand>> entry1 : asS.entrySet()) {
                                if (entry.getKey().equals(entry1.getKey())) {
                                    List<ArmorStand> armorStands = entry1.getValue();
                                    armorStands.get(3).setCustomName(drawHp(((PolarBear) entity).getHealth()-e.getDamage(), ((PolarBear) entity).getMaxHealth()));
                                }
                            }

                            manager.writeOfflinePlayer(opl, player);
                        }
                    }
                }
            }
        }

        @EventHandler
        public void onRegen(EntityRegainHealthEvent e) {
            Entity entity = e.getEntity();
            if(entity instanceof PolarBear) {
                if (hbs.containsValue(entity)) {
                    for (Map.Entry<UUID, PolarBear> entry : hbs.entrySet()) {
                        if (entry.getValue().equals(entity)) {
                            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);
                            OfflinePlayer opl = Bukkit.getOfflinePlayer(entry.getKey());
                            NBTCompound player = manager.readOfflinePlayer(opl);
                            player.put("Health", ((PolarBear) entity).getHealth()+e.getAmount());

                            for (Map.Entry<UUID, List<ArmorStand>> entry1 : asS.entrySet()) {
                                if (entry.getKey().equals(entry1.getKey())) {
                                    List<ArmorStand> armorStands = entry1.getValue();
                                    if(((PolarBear) entity).getHealth()+e.getAmount() >= ((PolarBear) entity).getMaxHealth())
                                        armorStands.get(3).setCustomName(drawHp(((PolarBear) entity).getMaxHealth(), ((PolarBear) entity).getMaxHealth()));
                                    else
                                        armorStands.get(3).setCustomName(drawHp(((PolarBear) entity).getHealth()+e.getAmount(), ((PolarBear) entity).getMaxHealth()));
                                }
                            }

                            manager.writeOfflinePlayer(opl, player);
                        }
                    }
                }
            }
        }

        @EventHandler
        public void onDeath(EntityDeathEvent e) {
            Entity entity = e.getEntity();
            Location loc = entity.getLocation();
            if(entity instanceof PolarBear) {
                if (hbs.containsValue(entity)) {
                    for (Map.Entry<UUID, PolarBear> entry : hbs.entrySet()) {
                        if (entry.getValue().equals(entity)) {
                            OfflinePlayer opl = Bukkit.getOfflinePlayer(entry.getKey());
                            Player lPl = openInv.loadPlayer(opl);
                            ItemStack[] isS = new ItemStack[0];
                            Inventory inv = null;
                            try {
                                inv = openInv.getSpecialInventory(lPl, false).getBukkitInventory();
                                isS = inv.getContents();
                            } catch (InstantiationException e1) {
                                e1.printStackTrace();
                            }

                            for(ItemStack it : isS) {
                                if(it != null)
                                    entity.getWorld().dropItemNaturally(loc, it);
                            }

                            inv.clear();

                            e.getDrops().clear();
                            entity.remove();
                            delBody(entry.getKey());
                            db.clear(opl.getUniqueId());
                        }
                    }
                }
            }
        }

        private ItemStack getItemStack(ItemStack item, NBTCompound itemMeta) {
            if(itemMeta != null) {
                ItemMeta iMeta = item.getItemMeta();
                NBTList ench = itemMeta.getList("ench");
                NBTList storedEnch = itemMeta.getList("StoredEnchantments");
                NBTCompound display = itemMeta.getCompound("display");
                Integer unbreakable = itemMeta.getInt("Unbreakable");
                NBTList attrMod = itemMeta.getList("AttributeModifiers");
                NBTList cPotionEff = itemMeta.getList("CustomPotionEffects");
                Integer hideFlags = itemMeta.getInt("HideFlags");
                String potion = itemMeta.getString("Potion");
                Integer generation = itemMeta.getInt("generation");
                String author = itemMeta.getString("author");
                String title = itemMeta.getString("title");
                NBTList pages = itemMeta.getList("pages");
                NBTCompound skullOwn = itemMeta.getCompound("SkullOwner");
                if(ench != null) {
                    for (Object anEnch : ench) {
                        NBTCompound enchant = (NBTCompound) anEnch;
                        iMeta.addEnchant(Enchantment.getById(enchant.getInt("id")), enchant.getInt("lvl"), true);
                    }
                }
                if(display != null) {
                    String name = display.getString("Name");
                    NBTList lore = display.getList("Lore");
                    if(name != null) {
                        iMeta.setDisplayName(name);
                    }
                    if(lore != null) {
                        List<String> lStr = new ArrayList<>();
                        for (Object aLore : lore) {
                            lStr.add((String) aLore);
                        }
                        iMeta.setLore(lStr);
                    }
                }
                if(unbreakable != 0) {
                    iMeta.setUnbreakable(true);
                }
                if(hideFlags != 0) {
                    iMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    iMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
                    iMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    iMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
                    iMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                    iMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                }
                item.setItemMeta(iMeta);
                net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
                NBTTagCompound compound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
                if(attrMod != null) {
                    NBTTagList modifiers = new NBTTagList();
                    for(Object attr : attrMod) {
                        NBTCompound at = (NBTCompound) attr;
                        Integer amount = at.getInt("Amount");
                        String slot = at.getString("Slot");
                        String attrName = at.getString("AttributeName");
                        String name = at.getString("Name");
                        Integer operation = at.getInt("Operation");
                        Integer uuidMost = at.getInt("UUIDMost");
                        Integer uuidLeast = at.getInt("UUIDLeast");
                        NBTTagCompound att = new NBTTagCompound();
                        att.set("AttributeName", new NBTTagString(attrName));
                        if(name != null) {
                            att.set("Name", new NBTTagString(name));
                        }
                        att.set("Operation", new NBTTagInt(operation));
                        att.set("UUIDLeast", new NBTTagInt(uuidLeast));
                        att.set("UUIDMost", new NBTTagInt(uuidMost));
                        if(slot != null) {
                            att.set("Slot", new NBTTagString(slot));
                        }
                        att.set("Amount", new NBTTagInt(amount));
                        modifiers.add(att);
                    }
                    if (compound != null) {
                        if(!modifiers.isEmpty())
                            compound.set("AttributeModifiers", modifiers);
                    }
                }
                if(!potion.equals("")) {
                    if (compound != null) {
                        compound.set("Potion", new NBTTagString(potion));
                    }
                }
                if(cPotionEff != null) {
                    NBTTagList modifiers = new NBTTagList();
                    for(Object pEff : cPotionEff) {
                        NBTCompound eff = (NBTCompound) pEff;
                        Integer id = eff.getInt("Id");
                        Integer ampl = eff.getInt("Amplifier");
                        Integer dur = eff.getInt("Duration");
                        NBTTagCompound att = new NBTTagCompound();
                        att.set("Id", new NBTTagInt(id));
                        att.set("Amplifier", new NBTTagInt(ampl));
                        att.set("Duration", new NBTTagInt(dur));
                        modifiers.add(att);
                    }
                    if (compound != null) {
                        if(!modifiers.isEmpty())
                        compound.set("CustomPotionEffects", modifiers);
                    }
                }
                if(storedEnch != null) {
                    NBTTagList modif = new NBTTagList();
                    for(Object en : storedEnch) {
                        NBTCompound enc = (NBTCompound) en;
                        Integer id = enc.getInt("id");
                        Integer lvl = enc.getInt("lvl");
                        NBTTagCompound att = new NBTTagCompound();
                        att.set("id", new NBTTagInt(id));
                        att.set("lvl", new NBTTagInt(lvl));
                        modif.add(att);
                    }
                    if (compound != null) {
                        if(!modif.isEmpty())
                        compound.set("StoredEnchantments", modif);
                    }
                }
                if(generation > 0) {
                    if (compound != null) {
                        compound.set("generation", new NBTTagInt(generation));
                    }
                }
                if(!title.equals("")) {
                    if (compound != null) {
                        compound.set("title", new NBTTagString(title));
                    }
                }
                if(!author.equals("")) {
                    if (compound != null) {
                        compound.set("author", new NBTTagString(author));
                    }
                }
                if(pages != null) {
                    NBTTagList modif = new NBTTagList();
                    for(Object en : pages) {
                        modif.add(new NBTTagString((String) en));
                    }
                    if (compound != null) {
                        if(!modif.isEmpty())
                            compound.set("pages", modif);
                    }
                }
                if(skullOwn != null) {
                    if(compound != null) {
                        NBTCompound props = skullOwn.getCompound("Properties");
                        NBTList textures = props.getList("textures");

                        NBTTagCompound skullOwner = new NBTTagCompound();
                        NBTTagCompound prop = new NBTTagCompound();
                        NBTTagList textrs = new NBTTagList();
                        for (Object texture : textures) {
                            NBTTagCompound list = new NBTTagCompound();
                            NBTCompound l = (NBTCompound) texture;
                            list.set("Signature", new NBTTagString(l.getString("Signature")));
                            list.set("Value", new NBTTagString(l.getString("Value")));
                            textrs.add(list);
                        }
                        prop.set("textures", textrs);

                        skullOwner.set("Properties", prop);
                        skullOwner.set("Id", new NBTTagString(skullOwn.getString("Id")));

                        skullOwner.set("Name", new NBTTagString(skullOwn.getString("Name")));
                        if(!skullOwn.getString("Name").equals(""))
                            compound.set("SkullOwner", skullOwner);
                    }
                }
                nmsStack.setTag(compound);
                item = CraftItemStack.asBukkitCopy(nmsStack);
            }
            return item;
        }

        @EventHandler
        private void onASMan(PlayerArmorStandManipulateEvent e) {
            Entity rc = e.getRightClicked();
            for(Map.Entry<UUID, List<ArmorStand>> entry : asS.entrySet()) {
                for(ArmorStand as : entry.getValue()) {
                    if(as.equals(rc)) {
                        e.setCancelled(true);
                    }
                }
            }
        }

        @EventHandler
        public void onRightClick(PlayerInteractEntityEvent e) {
            Player p = e.getPlayer();
            Entity rc = e.getRightClicked();

            if(rc instanceof PolarBear) {
                if (hbs.containsValue(rc)) {
                    for (Map.Entry<UUID, PolarBear> entry : hbs.entrySet()) {
                        if(entry.getValue() != null) {
                            if (entry.getValue().equals(rc)) {
                                String invName = null;
                                for (Map.Entry<UUID, List<ArmorStand>> entry1 : asS.entrySet()) {
                                    if (entry.getKey().equals(entry1.getKey())) {
                                        List<ArmorStand> armorStands = entry1.getValue();
                                        invName = armorStands.get(2).getCustomName();
                                        if (!plugin.getConfig().getBoolean("lootHide")) {
                                            armorStands.get(4).setCustomName(plugin.getConfig().getString("lootText").replace("&", "§"));
                                            armorStands.get(4).setCustomNameVisible(true);
                                        }
                                    }
                                }

                                final OfflinePlayer opl = Bukkit.getOfflinePlayer(entry.getKey());
                                Inventory inv = null;
                                if(invOwner.containsValue(opl)) {
                                    for(Map.Entry<Inventory, OfflinePlayer> entry1 : invOwner.entrySet()) {
                                        if(entry1.getValue() == opl) {
                                            inv = entry1.getKey();
                                        }
                                    }
                                } else {
                                    Player lPl = openInv.loadPlayer(opl);
                                    try {
                                        inv = openInv.getSpecialInventory(lPl, false).getBukkitInventory();
                                    } catch (InstantiationException e1) {
                                        e1.printStackTrace();
                                    }
                                }

                                if(inv != null) {
                                    if (!openedBody.containsValue(inv)) {
                                        invOwner.put(inv, opl);
                                        openedBody.put(p, inv);
                                        p.openInventory(inv);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        @EventHandler
        public void onCloseInv(InventoryCloseEvent e) {
            Player p = (Player) e.getPlayer();
            Inventory inv = e.getInventory();
            if(inv != null) {
                if(openedBody.containsValue(inv)) {
                    for (Map.Entry<UUID, List<ArmorStand>> entry1 : asS.entrySet()) {
                        if (invOwner.get(inv).getUniqueId().equals(entry1.getKey())) {
                            List<ArmorStand> armorStands = entry1.getValue();
                            armorStands.get(4).setCustomName("");
                            armorStands.get(4).setCustomNameVisible(false);
                        }
                    }

                    NBTCompound player;
                    Boolean online = invOwner.get(inv).isOnline();

                    if (!online) {
                        player = manager.readOfflinePlayer(invOwner.get(inv));
                    } else {
                        player = manager.read(Bukkit.getPlayer(invOwner.get(inv).getUniqueId()));
                    }

                    if (!invOwner.get(inv).isOnline()) {
                        manager.writeOfflinePlayer(invOwner.get(inv), player);
                    } else {
                        Player newP = Bukkit.getPlayer(invOwner.get(inv).getUniqueId());
                        newP.getInventory().clear();
                        ItemStack[] itemStacks = inv.getContents();
                        ItemStack[] newItemStacks = new ItemStack[36];

                        System.arraycopy(itemStacks, 0, newItemStacks, 0, 36);

                        newP.getInventory().setContents(newItemStacks);
                        newP.getEquipment().setBoots(itemStacks[36]);
                        newP.getEquipment().setLeggings(itemStacks[37]);
                        newP.getEquipment().setChestplate(itemStacks[38]);
                        newP.getEquipment().setHelmet(itemStacks[39]);
                    }
                }
            }
            invOwner.remove(inv);
            openedBody.remove(p);
        }

        @EventHandler
        public void ovInvInteract(InventoryClickEvent e) {
            Inventory inv = e.getClickedInventory();
            ItemStack it = e.getCurrentItem();

            if(inv != null) {
                if (it != null) {
                    if (openedBody.containsValue(inv)) {
                        if(e.getSlot() > 35) {
                            List<Material> helmets = new ArrayList<>();
                            List<Material> chestplates = new ArrayList<>();
                            List<Material> leggs = new ArrayList<>();
                            List<Material> boots = new ArrayList<>();

                            helmets.add(Material.AIR);
                            chestplates.add(Material.AIR);
                            leggs.add(Material.AIR);
                            boots.add(Material.AIR);

                            helmets.add(Material.LEATHER_HELMET);
                            chestplates.add(Material.LEATHER_CHESTPLATE);
                            leggs.add(Material.LEATHER_LEGGINGS);
                            boots.add(Material.LEATHER_BOOTS);

                            helmets.add(Material.CHAINMAIL_HELMET);
                            chestplates.add(Material.CHAINMAIL_CHESTPLATE);
                            leggs.add(Material.CHAINMAIL_LEGGINGS);
                            boots.add(Material.CHAINMAIL_BOOTS);

                            helmets.add(Material.IRON_HELMET);
                            chestplates.add(Material.IRON_CHESTPLATE);
                            leggs.add(Material.IRON_LEGGINGS);
                            boots.add(Material.IRON_BOOTS);

                            helmets.add(Material.GOLD_HELMET);
                            chestplates.add(Material.GOLD_CHESTPLATE);
                            leggs.add(Material.GOLD_LEGGINGS);
                            boots.add(Material.GOLD_BOOTS);

                            helmets.add(Material.DIAMOND_HELMET);
                            chestplates.add(Material.DIAMOND_CHESTPLATE);
                            leggs.add(Material.DIAMOND_LEGGINGS);
                            boots.add(Material.DIAMOND_BOOTS);
                            if (e.getSlot() == 36) {
                                if (!helmets.contains(e.getCursor().getType())) {
                                    e.setCancelled(true);
                                }
                            }
                            if (e.getSlot() == 37) {
                                if (!chestplates.contains(e.getCursor().getType())) {
                                    e.setCancelled(true);
                                }
                            }
                            if (e.getSlot() == 38) {
                                if (!leggs.contains(e.getCursor().getType())) {
                                    e.setCancelled(true);
                                }
                            }
                            if (e.getSlot() == 39) {
                                if (!boots.contains(e.getCursor().getType())) {
                                    e.setCancelled(true);
                                }
                            }
                        } else if(e.getSlot() > 40) {
                            e.setCancelled(true);
                        }
                    }
                }
            }

        }
    }
}
