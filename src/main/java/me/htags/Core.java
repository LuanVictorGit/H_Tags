package me.htags;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.htags.commands.TagCommand;
import me.htags.objects.ConfigTag;
import me.htags.objects.PlayerTag;
import me.htags.objects.listeners.PlayerUpdateHologramEvent;
import me.htags.objects.manager.Manager;
import me.htags.utils.API;
import me.htags.utils.ConfigGeral;
import me.htags.utils.Tag;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;

@Getter
public class Core extends JavaPlugin {

	@Getter
	private static Core instance;
	
	private String tag, version = "§dv"+getDescription().getVersion();
	private Manager manager;
	private ConfigGeral configgeral;
	private API api;
	private BukkitTask task;
	private ConfigTag configtag;
	
	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		reloadPlugin();
		new TagCommand();
		Bukkit.getPluginManager().registerEvents(new Listener() {
			
			@EventHandler(priority = EventPriority.HIGHEST)
			public void join(PlayerJoinEvent e) {
				PlayerTag.check(e.getPlayer());
				Tag.updateAllTag();
			}
			
			@EventHandler(priority = EventPriority.HIGHEST)
			public void quit(PlayerQuitEvent e) {
				PlayerTag.check(e.getPlayer()).remove();
				Tag.updateAllTag();
			}
			
			@EventHandler(priority = EventPriority.HIGHEST)
			public void death(PlayerDeathEvent e) {
				Bukkit.getScheduler().runTask(Core.getInstance(), ()->Tag.updateAllTag());
			}
			
		}, this);
		
		sendConsole(" ");
		sendConsole(tag + " &aH_Tags iniciado com sucesso! &6[Author lHawk_] " + version);
		sendConsole(" ");
	}
	
	@Override
	public void onDisable() {
		while(!manager.getPlayers().isEmpty()) {
			PlayerTag pt = manager.getPlayers().get(0);
			pt.reset();
			pt.remove();
		}
		sendConsole(" ");
		sendConsole(tag + " &cH_Tags desligado com sucesso! &6[Author lHawk_] " + version);
		sendConsole(" ");
	}
	
	public void sendConsole(String msg) {Bukkit.getConsoleSender().sendMessage(msg.replace("&", "§"));}
	
	public void reloadPlugin() {
		reloadConfig();
		tag = getConfig().getString("Config.tag").replace("&", "§");
		sendConsole(tag + " &6recarregando informações do H_Tags...");
		long time = System.currentTimeMillis();
		if (manager!=null) {
			while(!manager.getPlayers().isEmpty()) {
				PlayerTag pt = manager.getPlayers().get(0);
				pt.reset();
				pt.remove();
			}
		}
		api = new API();
		manager = new Manager();
		for(String key : getConfig().getConfigurationSection("tags").getKeys(false)) new ConfigTag(key);
		ConfigTag ct = new ConfigTag("defaultTag");
		char[] alfabet = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
		ct.setPosition(String.valueOf(alfabet[manager.getTags().size()]).toUpperCase());
		ct.setPrefix(getConfig().getString("Config.defaultPrefix").replace("&", "§"));
		ct.setSuffix(getConfig().getString("Config.defaultSuffix").replace("&", "§"));
		configtag = ct;
		configgeral = new ConfigGeral();
		for(Player p : Bukkit.getOnlinePlayers()) PlayerTag.check(p);
		if (task!=null) task.cancel();
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			long currentTimeUpdate = 0;
			
			@Override
			public void run() {
				if (System.currentTimeMillis() - currentTimeUpdate >= (1000*getConfig().getInt("Config.updateTime"))) {
					Tag.updateAllTag();
					currentTimeUpdate = System.currentTimeMillis();
				}
				
				if (!configgeral.isHolograms()) return;
				if (manager.getPlayers().isEmpty()) return;
				for (int i = 0; i < manager.getPlayers().size(); i++) {
					PlayerTag target = manager.getPlayers().get(i);
					Player playerTarget = target.getPlayer();
					EntityArmorStand hologram = target.getHologram();
					loop: for (int j = 0; j < manager.getPlayers().size(); j++) {
						PlayerTag viewer = manager.getPlayers().get(j);
						Player playerViewer = viewer.getPlayer();
						if (playerTarget.equals(playerViewer)) continue loop;
						Location loc1 = playerTarget.getLocation();
						Location loc2 = playerViewer.getLocation();
						if (!loc1.getWorld().equals(loc2.getWorld()) || loc1.distance(loc2) > configgeral.getHologram_distance()
							|| !playerTarget.isOnline() || playerTarget.isDead()) {
							if (target.getHashHolograms().containsKey(viewer)) {
								PacketPlayOutEntityDestroy destroyHologram = new PacketPlayOutEntityDestroy(target.getHologram().getId());
								PlayerTag.sendPacket(playerViewer, destroyHologram);
								target.getHashHolograms().remove(viewer);
							}
							continue loop;
						}
						Location loc = playerTarget.getLocation();
						hologram.world = ((CraftWorld)loc.getWorld()).getHandle();
						if (!playerTarget.isSneaking()) {
							hologram.setLocation(loc.getX(), loc.getY()+1.585, loc.getZ(), 0, 0);
						} else {
							hologram.setLocation(loc.getX(), loc.getY()+1.25, loc.getZ(), 0, 0);
						}
						
						PacketPlayOutEntityTeleport packetTeleportHologram = new PacketPlayOutEntityTeleport(hologram);
						if (target.getHashHolograms().containsKey(viewer)) {
							PlayerUpdateHologramEvent event = new PlayerUpdateHologramEvent(playerTarget, playerViewer, PlaceholderAPI.setPlaceholders(playerTarget, configgeral.getHologram_text()));
							Bukkit.getPluginManager().callEvent(event);
							hologram.setCustomName(event.getText());
							PacketPlayOutEntityMetadata packetUpdate = new PacketPlayOutEntityMetadata(hologram.getId(), hologram.getDataWatcher(), true);
							PlayerTag.sendPacket(playerViewer, packetUpdate);
							PlayerTag.sendPacket(playerViewer, packetTeleportHologram);
							continue loop;
						}
						PacketPlayOutSpawnEntityLiving packetSpawnHologram = new PacketPlayOutSpawnEntityLiving(hologram);
						PlayerTag.sendPacket(playerViewer, packetSpawnHologram);
						PlayerTag.sendPacket(playerViewer, packetTeleportHologram);
						target.getHashHolograms().put(viewer, target.getHologram());
					}
				}
			}
			
		}, 0, 1);
		sendConsole(tag + " &6todas as informações do H_Tags foram recarregadas em &e" + (System.currentTimeMillis()-time) + "ms&6!");
	}
	
}
