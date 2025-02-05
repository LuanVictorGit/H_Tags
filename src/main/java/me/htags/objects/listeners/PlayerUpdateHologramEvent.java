package me.htags.objects.listeners;

import org.bukkit.entity.Player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.htags.utils.ListenerInterface;

@Getter
@Setter
@AllArgsConstructor
public class PlayerUpdateHologramEvent extends ListenerInterface {

	private final Player target, viewer;
	private String text;
	
}
