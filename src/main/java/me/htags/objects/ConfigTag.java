package me.htags.objects;


import org.bukkit.configuration.ConfigurationSection;

import org.bukkit.configuration.file.FileConfiguration;

import lombok.Getter;
import lombok.Setter;
import me.htags.Core;
import me.htags.objects.manager.Manager;

// classe para a configuração da tag do jogador, com a mesma função do ConfigGeral.
@Getter
@Setter
public class ConfigTag {

	private String name, permissionGroup = new String(), position = new String(),
	prefix = new String(), suffix = new String();
	
	public ConfigTag(String name) {
		this.name = name;
		FileConfiguration config = Core.getInstance().getConfig();
		if (config.getConfigurationSection("tags." + name) == null) return;
		ConfigurationSection section = config.getConfigurationSection("tags." + name);
		this.permissionGroup = section.getString("permission");
		this.position = section.getString("position").toUpperCase();
		this.prefix = section.getString("prefix").replace("&", "§");
		this.suffix = section.getString("suffix").replace("&", "§");
		Manager.get().getTags().add(this);
	}
	
}
