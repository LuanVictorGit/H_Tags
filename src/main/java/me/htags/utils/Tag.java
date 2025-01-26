package me.htags.utils;

import org.bukkit.Bukkit;
import me.htags.Core;
import me.htags.objects.PlayerTag;
import me.htags.objects.manager.Manager;

public class Tag {

	public static PlayerTag getPlayer(String name) {
		return Manager.get().getPlayer(name);
	}
	
	public static void updateAllTag() {
		if (Manager.get().getPlayers().isEmpty()) return;
		Bukkit.getScheduler().runTaskAsynchronously(Core.getInstance(), ()->{
			Bukkit.getOnlinePlayers().forEach(p -> PlayerTag.check(p).update());
		});
	}
	
}
