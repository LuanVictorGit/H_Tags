package me.htags.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.htags.Core;
import me.htags.objects.menus.MenuTags;

// comando geral do plugin.
public class TagCommand implements CommandExecutor {

	public TagCommand() {
		Core.getInstance().getCommand("tag").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender s, Command c, String lb, String[] args) {
		if (!s.hasPermission("h_tags.adm")) return false;
		String tag = Core.getInstance().getTag();
		if (args.length == 1) {
			switch (args[0].toLowerCase()) {
			case "reload":
				Core.getInstance().reloadPlugin();
				s.sendMessage(tag + " §aconfiguração recarregada com sucesso!");
				return false;
			case "menu":
				if (Core.getInstance().isHawkUtils() && s instanceof Player) {
					new MenuTags().open((Player) s);
					return false;
				}
				break;
			default:
				break;
			}
		}
		s.sendMessage(" ");
		s.sendMessage(tag + " §aComandos disponíveis:");
		s.sendMessage("§e/Tag [Reload] §7- Recarregar a configuração.");
		if (Core.getInstance().isHawkUtils() && s instanceof Player) s.sendMessage("§e/Tag [Menu] §7- Abrir o menu de configuração.");
		s.sendMessage(" ");
		return false;
	}
	
}
