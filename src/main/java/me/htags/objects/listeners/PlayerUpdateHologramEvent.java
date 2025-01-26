package me.htags.objects.listeners;

import org.bukkit.entity.Player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.htags.utils.Listener;

@Getter
@Setter
@AllArgsConstructor
public class PlayerUpdateHologramEvent extends Listener {

	private final Player target, viewer;
	private String text;
	
}
