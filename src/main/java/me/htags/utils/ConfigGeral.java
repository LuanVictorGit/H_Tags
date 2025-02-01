package me.htags.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import lombok.Getter;
import me.htags.Core;

// Configuração geral cacheada para uma busca mais rapida na memoria sem precisar acessar a config constantemente.
@Getter
public class ConfigGeral {

	private String colorDefault,
	header, footer, hologram_text;
	
	private boolean holograms;
	private int hologram_distance;
	private double hologram_height;
	
	public ConfigGeral() {
		FileConfiguration config = Core.getInstance().getConfig();
		ConfigurationSection section = config.getConfigurationSection("Config");
		
		hologram_text = section.getString("hologram_text").replace("&", "§");
		hologram_height = section.contains("hologram_height") ? section.getDouble("hologram_height") : 0;
		holograms = section.getBoolean("holograms");
		hologram_distance = section.getInt("hologram_distance");
		colorDefault = section.getString("colorDefault").replace("&", "§"); // pegando a cor padrão do {cor}
		
		header = new String();
		for(String linha : section.getStringList("header")) {
			header+="\n"+linha.replace("&", "§");
		}
		header = header.replaceFirst("\n", new String());
		
		footer = new String();
		for(String linha : section.getStringList("footer")) {
			footer+="\n"+linha.replace("&", "§");
		}
		footer = footer.replaceFirst("\n", new String());
	}
	
	public static ConfigGeral get() {
		return Core.getInstance().getConfiggeral();
	}
	
}
