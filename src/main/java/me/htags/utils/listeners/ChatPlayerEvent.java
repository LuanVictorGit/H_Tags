package me.htags.utils.listeners;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.htags.Core;
import me.htags.objects.ConfigTag;
import me.htags.objects.PlayerTag;
import me.htags.objects.manager.Manager;
import me.htags.objects.menus.MenuTags;
import net.md_5.bungee.api.ChatColor;

public class ChatPlayerEvent implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void chat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		PlayerTag pt = PlayerTag.check(p);
		String message = ChatColor.stripColor(e.getMessage().replace("&", "§"));
		if (pt.getTagSelected() != null) {
			e.setCancelled(true);
			if (message.equalsIgnoreCase("cancelar")) {
				pt.resetSettings();
				new MenuTags().open(p);
				return;
			}
			if (pt.isSettingPosition()) {
				if (message.length() <= 2) {
					try {
						YamlConfiguration yaml = YamlConfiguration.loadConfiguration(Core.getInstance().getFileTags());
						yaml.set(pt.getTagSelected().getName()+".position", message.toUpperCase());
						yaml.save(Core.getInstance().getFileTags());
						Core.getInstance().reloadPlugin();
						p.sendMessage("§aVocê setou a posição da tag §f" + pt.getTagSelected().getName() + " §apara §e" + message.toUpperCase()+"§a!");
						p.playSound(p.getLocation(), Sound.NOTE_PLING, 0.5f, 10f);
						new MenuTags().open(p);
					} catch (Exception e2) {
						e2.printStackTrace();
						pt.resetSettings();
						p.sendMessage(Core.getInstance().getTag() + " §cOcorreu um erro ao tentar setar a posição");
					}
				} else {
					p.sendMessage(Core.getInstance().getTag() + " §cVocê so pode colocar até 2 caracteres de posição, A até Z");
					p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1, 1);
					return;
				}
				return;
			}
			if (pt.isSettingPermission()) {
				if (message.length() < 30) {
					try {
						YamlConfiguration yaml = YamlConfiguration.loadConfiguration(Core.getInstance().getFileTags());
						yaml.set(pt.getTagSelected().getName()+".permission", message.toLowerCase());
						yaml.save(Core.getInstance().getFileTags());
						Core.getInstance().reloadPlugin();
						p.sendMessage("§aVocê setou a permissão da tag §f" + pt.getTagSelected().getName() + " §apara §e" + message.toLowerCase()+"§a!");
						p.playSound(p.getLocation(), Sound.NOTE_PLING, 0.5f, 10f);
						new MenuTags().open(p);
					} catch (Exception e2) {
						e2.printStackTrace();
						pt.resetSettings();
						p.sendMessage(Core.getInstance().getTag() + " §cOcorreu um erro ao tentar setar a permissão da tag.");
					}
				} else {
					p.sendMessage(Core.getInstance().getTag() + " §cA permissão deve conter apenas 30 caracteres!");
					p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1, 1);
					return;
				}
				return;
			}
			if (pt.isSettingPrefix()) {
				message = e.getMessage();
				if (message.length() < 80) {
					try {
						YamlConfiguration yaml = YamlConfiguration.loadConfiguration(Core.getInstance().getFileTags());
						yaml.set(pt.getTagSelected().getName()+".prefix", message);
						yaml.save(Core.getInstance().getFileTags());
						Core.getInstance().reloadPlugin();
						p.sendMessage("§aVocê setou a prefix da tag §f" + pt.getTagSelected().getName() + " §apara §e" + message.replace("&", "§") +"§a!");
						p.playSound(p.getLocation(), Sound.NOTE_PLING, 0.5f, 10f);
						new MenuTags().open(p);
					} catch (Exception e2) {
						e2.printStackTrace();
						pt.resetSettings();
						p.sendMessage(Core.getInstance().getTag() + " §cOcorreu um erro ao tentar setar o prefixo da tag.");
					}
				} else {
					p.sendMessage(Core.getInstance().getTag() + " §cA permissão deve conter apenas 80 caracteres!");
					p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1, 1);
					return;
				}
				return;
			}
			if (pt.isSettingSuffix()) {
				message = e.getMessage();
				if (message.length() < 80) {
					try {
						YamlConfiguration yaml = YamlConfiguration.loadConfiguration(Core.getInstance().getFileTags());
						yaml.set(pt.getTagSelected().getName()+".suffix", message);
						yaml.save(Core.getInstance().getFileTags());
						Core.getInstance().reloadPlugin();
						p.sendMessage("§aVocê setou a suffix da tag §f" + pt.getTagSelected().getName() + " §apara §e" + message.replace("&", "§") +"§a!");
						p.playSound(p.getLocation(), Sound.NOTE_PLING, 0.5f, 10f);
						new MenuTags().open(p);
					} catch (Exception e2) {
						e2.printStackTrace();
						pt.resetSettings();
						p.sendMessage(Core.getInstance().getTag() + " §cOcorreu um erro ao tentar setar o suffixo da tag.");
					}
				} else {
					p.sendMessage(Core.getInstance().getTag() + " §cA permissão deve conter apenas 80 caracteres!");
					p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1, 1);
					return;
				}
				return;
			}
		}
		if (pt.isCreatingTag()) {
			e.setCancelled(true);
			if (message.equalsIgnoreCase("cancelar")) {
				pt.setCreatingTag(false);
				new MenuTags().open(p);
				return;
			}
			if (message.contains(" ") || message.length() > 16) {
				p.sendMessage(Core.getInstance().getTag() + " §cO nome da tag que você escreveu ou é muito grande, ou contem espaço!");
				p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1, 1);
				return;
			}
			ConfigTag tag = Manager.get().getTag(message);
			if (tag == null) {
				try {
					YamlConfiguration yaml = YamlConfiguration.loadConfiguration(Core.getInstance().getFileTags());
					yaml.createSection(message);
					ConfigurationSection section = yaml.getConfigurationSection(message);
					section.set("permission", "h_tags."+message);
					section.set("position", "A");
					section.set("prefix", "&7["+message.toUpperCase()+"&7] {cor}");
					section.set("suffix", " &c%player_health%&4❤");
					yaml.save(Core.getInstance().getFileTags());
					Core.getInstance().reloadPlugin();
					p.sendMessage("§aVocê criou a tag §f" + message + " §acom sucesso!");
					p.playSound(p.getLocation(), Sound.NOTE_PLING, 0.5f, 10f);
					new MenuTags().open(p);
					return;
				} catch (Exception e2) {
					e2.printStackTrace();
					pt.setCreatingTag(false);
					p.sendMessage(Core.getInstance().getTag() + " §cOcorreu um erro ao tentar criar a tag.");
				}
				return;
			} else {
				p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1, 1);
				p.sendMessage(Core.getInstance().getTag() + "§cEsta tag ja existe!, coloque outro nome");
				return;
			}
		}
	}
	
}
