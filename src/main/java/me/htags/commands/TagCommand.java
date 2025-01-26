package me.htags.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.htags.Core;

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
			if (args[0].equalsIgnoreCase("reload")) {
				Core.getInstance().reloadPlugin();
				s.sendMessage(tag + " §aconfiguração recarregada com sucesso!");
				return false;
			}
		}
		s.sendMessage(" ");
		s.sendMessage(tag + " §aComandos disponíveis:");
		s.sendMessage("§e/Tag [Reload] §7- Recarregar a configuração.");
		s.sendMessage(" ");
		return false;
	}
	
}
