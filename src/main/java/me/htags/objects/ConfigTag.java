package me.htags.objects;


import org.bukkit.configuration.ConfigurationSection;

import org.bukkit.configuration.file.FileConfiguration;

import lombok.Getter;
import lombok.Setter;
import me.htags.Core;
import me.htags.objects.manager.Manager;

@Getter
@Setter
public class ConfigTag {

	private String name, 
	permissionGroup = new String(), 
	position = new String(),
	prefix = new String(),
	suffix = new String();
	
	public ConfigTag(String name) {
		this.name = name;
		FileConfiguration config = Core.getInstance().getConfigTags();
		if (config.getConfigurationSection(name) == null) return;
		ConfigurationSection section = config.getConfigurationSection(name);
		this.permissionGroup = section.getString("permission");
		this.position = section.getString("position").toUpperCase();
		this.prefix = section.getString("prefix").replace("&", "§");
		this.suffix = section.getString("suffix").replace("&", "§");
		Manager.get().getTags().add(this);
	}
	
}
