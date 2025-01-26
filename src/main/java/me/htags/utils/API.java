package me.htags.utils;

import java.lang.reflect.Field;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import me.htags.Core;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

public class API {

	public static API get() {
		return Core.getInstance().getApi();
	}
	
	// enviando pacotes ao jogador para atualizar o header, e o footer.
	public void sendTablist(Player p, String Title, String subTitle) {
		if (Title == null) Title = "";
		if (subTitle == null) subTitle = "";

		IChatBaseComponent tabTitle = ChatSerializer.a("{\"text\":\"" + Title + "\"}");
		IChatBaseComponent tabSubTitle = ChatSerializer.a("{\"text\":\"" + subTitle + "\"}");

		PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter(tabTitle);

		try {
			Field field = packet.getClass().getDeclaredField("b");
			field.setAccessible(true);
			field.set(packet, tabSubTitle);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
		}
	}

}
