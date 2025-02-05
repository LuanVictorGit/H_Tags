package me.htags.utils;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.clip.placeholderapi.PlaceholderAPI;
import me.htags.Core;
import me.htags.objects.PlayerTag;
import me.htags.objects.listeners.PlayerUpdateHologramEvent;
import me.htags.objects.manager.Manager;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
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

	public void updateHologramOfPlayer(Player playerTarget) {
		PlayerTag target = PlayerTag.check(playerTarget);
		EntityArmorStand hologram = target.getHologram();
		Manager manager = Manager.get();
		ConfigManager configgeral = ConfigManager.get();
		for (int j = 0; j < manager.getPlayers().size(); j++) {
			PlayerTag viewer = manager.getPlayers().get(j);
			Player playerViewer = viewer.getPlayer();
			if (playerTarget.equals(playerViewer)) continue;
			Location loc1 = playerTarget.getLocation();
			Location loc2 = playerViewer.getLocation();
			PlayerUpdateHologramEvent event = new PlayerUpdateHologramEvent(playerTarget, playerViewer, PlaceholderAPI.setPlaceholders(playerTarget, configgeral.getHologram_text()));
			Bukkit.getPluginManager().callEvent(event);
			if (!loc1.getWorld().equals(loc2.getWorld()) || loc1.distance(loc2) > configgeral.getHologram_distance()
				|| !playerTarget.isOnline() 
				|| playerTarget.isDead() 
				|| !playerViewer.canSee(playerTarget)
				|| event.isCancelled()
				|| playerTarget.getActivePotionEffects().stream().filter(potion -> potion.getType() == PotionEffectType.INVISIBILITY).findFirst().orElse(null) != null) {
				if (target.getHashHolograms().containsKey(viewer)) {
					PacketPlayOutEntityDestroy destroyHologram = new PacketPlayOutEntityDestroy(target.getHologram().getId());
					PlayerTag.sendPacket(playerViewer, destroyHologram);
					target.getHashHolograms().remove(viewer);
				}
				continue;
			}
			Location loc = playerTarget.getLocation();
			hologram.world = ((CraftWorld)loc.getWorld()).getHandle();
			if (!playerTarget.isSneaking()) {
				hologram.setLocation(loc.getX(), (loc.getY()+1.585)+configgeral.getHologram_height(), loc.getZ(), 0, 0);
			} else {
				hologram.setLocation(loc.getX(), (loc.getY()+1.25)+configgeral.getHologram_height(), loc.getZ(), 0, 0);
			}
			
			PacketPlayOutEntityTeleport packetTeleportHologram = new PacketPlayOutEntityTeleport(hologram);
			if (target.getHashHolograms().containsKey(viewer)) {
				hologram.setCustomName(event.getText());
				PacketPlayOutEntityMetadata packetUpdate = new PacketPlayOutEntityMetadata(hologram.getId(), hologram.getDataWatcher(), true);
				PlayerTag.sendPacket(playerViewer, packetUpdate);
				PlayerTag.sendPacket(playerViewer, packetTeleportHologram);
				continue;
			}
			PacketPlayOutSpawnEntityLiving packetSpawnHologram = new PacketPlayOutSpawnEntityLiving(hologram);
			PlayerTag.sendPacket(playerViewer, packetSpawnHologram);
			PlayerTag.sendPacket(playerViewer, packetTeleportHologram);
			target.getHashHolograms().put(viewer, target.getHologram());
		}
	}
	
}
