package me.htags;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;
import me.htags.commands.TagCommand;
import me.htags.objects.ConfigTag;
import me.htags.objects.PlayerTag;
import me.htags.objects.manager.Manager;
import me.htags.utils.API;
import me.htags.utils.ConfigManager;
import me.htags.utils.Tag;
import me.htags.utils.listeners.ChatPlayerEvent;
@Getter
public class Core extends JavaPlugin {

	@Getter
	private static Core instance;
	
	private String tag, version = "§dv"+getDescription().getVersion();
	private Manager manager;
	private ConfigManager configgeral;
	private API api;
	private boolean hawkUtils;
	private FileConfiguration configTags;
	private File fileTags;
	private BukkitTask task;
	private ConfigTag configtag;
	
	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		reloadPlugin();
		new TagCommand();
		
		List<Listener> listeners = new ArrayList<>();
		if (hawkUtils) listeners.add(new ChatPlayerEvent());
		listeners.add(new Listener() {
			
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
			
		});
		listeners.forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
		
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
		if (task != null) task.cancel();
		HandlerList.unregisterAll(this);
		sendConsole(" ");
		sendConsole(tag + " &cH_Tags desligado com sucesso! &6[Author lHawk_] " + version);
		sendConsole(" ");
	}
	
	public void sendConsole(String msg) {Bukkit.getConsoleSender().sendMessage(msg.replace("&", "§"));}
	
	public void reloadPlugin() {
		reloadConfig();
		tag = getConfig().getString("Config.tag").replace("&", "§");
		hawkUtils = Bukkit.getPluginManager().getPlugin("HawkUtils") == null ? false : Bukkit.getPluginManager().getPlugin("HawkUtils").isEnabled(); 
		fileTags = new File(getDataFolder()+"/tags.yml");
		if (!fileTags.exists()) {
			try {
				fileTags.createNewFile();
				YamlConfiguration yaml = YamlConfiguration.loadConfiguration(fileTags);
				yaml.createSection("ceo");
				ConfigurationSection section = yaml.getConfigurationSection("ceo");
				section.set("permission", "*");
				section.set("position", "A");
				section.set("prefix", "&6[CEO] {cor}");
				section.set("suffix", " &c%player_health%&4❤");
				yaml.save(fileTags);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		configTags = YamlConfiguration.loadConfiguration(fileTags);
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
		for(String key : configTags.getKeys(false)) new ConfigTag(key);
		ConfigTag ct = new ConfigTag("defaultTag");
		char[] alfabet = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
		ct.setPosition(String.valueOf(alfabet[manager.getTags().size()]).toUpperCase());
		ct.setPrefix(getConfig().getString("Config.defaultPrefix").replace("&", "§"));
		ct.setSuffix(getConfig().getString("Config.defaultSuffix").replace("&", "§"));
		configtag = ct;
		configgeral = new ConfigManager();
		for(Player p : Bukkit.getOnlinePlayers()) PlayerTag.check(p);
		if (task!=null) task.cancel();
		task = new BukkitRunnable() {
			long currentTimeUpdate;
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
					api.updateHologramOfPlayer(playerTarget);
				}
			}
		}.runTaskTimerAsynchronously(this, 0, 1);
		sendConsole(tag + " &6todas as informações do H_Tags foram recarregadas em &e" + (System.currentTimeMillis()-time) + "ms&6!");
	}
	
}
