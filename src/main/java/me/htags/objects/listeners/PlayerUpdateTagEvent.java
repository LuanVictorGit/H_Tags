package me.htags.objects.listeners;

import org.bukkit.entity.Player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.htags.objects.ConfigTag;
import me.htags.utils.Listener;

@Getter
@Setter
@AllArgsConstructor
public class PlayerUpdateTagEvent extends Listener {

	private final Player target, viewer;
	private final ConfigTag config;
	private String prefix, suffix, color;
	
}
