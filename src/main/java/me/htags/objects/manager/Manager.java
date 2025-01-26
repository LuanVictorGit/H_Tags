package me.htags.objects.manager;

import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Player;

import lombok.Getter;
import me.htags.Core;
import me.htags.objects.ConfigTag;
import me.htags.objects.PlayerTag;

// classe Manager para manusear os jogadores e as tags.
@Getter
public class Manager {

	private List<PlayerTag> players = new ArrayList<>();
	private List<ConfigTag> tags = new ArrayList<>();
	
	public PlayerTag getPlayer(String name) {
		Iterator<PlayerTag> it = players.iterator();
		while(it.hasNext()) {
			PlayerTag pt = it.next();
			if (pt.getName().equalsIgnoreCase(name)) return pt;
		}
		return null;
	}
	
	public ConfigTag getTag(Player p) {
		Iterator<ConfigTag> it = tags.iterator();
		while(it.hasNext()) {
			ConfigTag tag = it.next();
			if (!p.hasPermission(tag.getPermissionGroup())) continue;
			return tag;
		}
		return null;
	}
	
	public ConfigTag getTag(String name) {
		Iterator<ConfigTag> it = tags.iterator();
		while(it.hasNext()) {
			ConfigTag ct = it.next();
			if (ct.getName().equalsIgnoreCase(name)) return ct;
		}
		return null;
	}
	
	public static Manager get() {
		return Core.getInstance().getManager();
	}
	
}
