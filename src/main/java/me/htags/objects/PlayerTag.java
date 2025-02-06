package me.htags.objects;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.htags.Core;
import me.htags.objects.listeners.PlayerUpdateTagEvent;
import me.htags.objects.manager.Manager;
import me.htags.utils.API;
import me.htags.utils.ConfigManager;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.Scoreboard;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;

@Getter
@Setter
public class PlayerTag {
	
	private String name; // Nome do jogador
	private Player player; // Objeto Player do Bukkit
	private String idCode = randomCode(10); // Código único para cada jogador
	private Scoreboard board; // Placar de pontuação
	private boolean updating = false; // Status de atualização
	private ScoreboardTeam team; // Time associado ao jogador
	private EntityArmorStand hologram;
	private ConfigTag tagSelected;
	private boolean creatingTag = false,
	settingPosition = false,
	settingPermission = false,
	settingPrefix = false,
	settingSuffix = false;
	private HashMap<PlayerTag, EntityArmorStand> hashHolograms = new HashMap<>();

	// Construtor: Inicializa o objeto PlayerTag
	public PlayerTag(String name) {
		this.name = name;
		this.board = new Scoreboard();
		this.player = Bukkit.getPlayerExact(name);
		this.update();
		if (hologram == null && ConfigManager.get().isHolograms()) {
			hologram = new EntityArmorStand(((CraftWorld)player.getWorld()).getHandle(),0,0,0);
			hologram.setCustomName(" ");
			hologram.setCustomNameVisible(true);
			hologram.setArms(false);
			hologram.setGravity(false);
			hologram.setInvisible(true);
			hologram.setBasePlate(false);
			NBTTagCompound tag = new NBTTagCompound();
			hologram.e(tag);
			tag.setBoolean("Marker", true);
			hologram.f(tag);
		}
		Manager.get().getPlayers().add(this);
	}
	
	public void resetSettings() {
		tagSelected = null;
		creatingTag = false;
		settingPosition = false;
		settingPermission = false;
		settingPrefix = false;
		settingSuffix = false;
	}

	// Reseta as tags e limpa o placar do jogador
	public void reset() {
		// Limpa todas as equipes do placar
		for (ScoreboardTeam team : board.getTeams()) {
			team.getPlayerNameSet().clear();
			for (Player all : Bukkit.getOnlinePlayers()) {
				// Envia o pacote de remoção de equipe para todos os jogadores online
				PacketPlayOutScoreboardTeam packetRemove = new PacketPlayOutScoreboardTeam(team, 1);
				sendPacket(all, packetRemove);
			}
		}
		// Limpa as equipes e define o time como nulo
		team = null;
		board.getTeams().clear();
	}

	// Atualiza a tag do jogador
	private byte attempts;
	public void update() {
		if (updating) return; // Evita múltiplas atualizações simultâneas
		updating = true;
		try {
			Player p = getPlayer();
			if (p == null || !p.isValid() || !p.isOnline() || attempts > 3) {
				updating = false;
				attempts = 0;
				return;
			}
			ConfigTag config = Manager.get().getTag(p);
			// Se a configuração não existir, utiliza a configuração padrão
			config = (config == null) ? Core.getInstance().getConfigtag() : config;
			reset(); // Reseta as configurações antigas

			// Cria uma nova equipe para o jogador
			team = board.createTeam(config.getPosition() + idCode);
			team.getPlayerNameSet().add(player.getName());
			
			// Envia o prefixo e sufixo para todos os jogadores online
			for (Player viewer : Bukkit.getOnlinePlayers()) {
				String prefix = config.getPrefix();
				String suffix = config.getSuffix();
				String color = ConfigManager.get().getColorDefault();
				
				// Dispara o evento de atualização de tag
				PlayerUpdateTagEvent event = new PlayerUpdateTagEvent(p, viewer, config, prefix, suffix, color);
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled()) continue;

				prefix = event.getPrefix();
				suffix = event.getSuffix();
				color = event.getColor();

				// Substitui placeholders no prefixo e sufixo
				prefix = (prefix != null)
						? PlaceholderAPI.setPlaceholders(player, prefix.replace("{cor}", color).replace("&", "§"))
						: null;
				suffix = (suffix != null)
						? PlaceholderAPI.setPlaceholders(player, suffix.replace("{cor}", color).replace("&", "§"))
						: null;

				// Garante que prefixo e sufixo não ultrapassem 16 caracteres
				if (prefix != null && prefix.length() > 16)
					prefix = prefix.substring(0, 16);
				if (suffix != null && suffix.length() > 16)
					suffix = suffix.substring(0, 16);

				if (prefix != null)
					team.setPrefix(prefix);
				if (suffix != null)
					team.setSuffix(suffix);

				// Cria o pacote de atualização da equipe para o jogador visualizador
				PacketPlayOutScoreboardTeam packetTeam = new PacketPlayOutScoreboardTeam(team, 0);
				// envia o pacote para os jogadores.
				sendPacket(viewer, packetTeam);
			}

			// Atualiza o cabeçalho e rodapé do tablist
			API.get().sendTablist(p, PlaceholderAPI.setPlaceholders(p, ConfigManager.get().getHeader()), PlaceholderAPI.setPlaceholders(p, ConfigManager.get().getFooter()));

			updating = false;
			attempts = 0;
		} catch (Exception e) {
			updating = false;
			attempts++;
			update();
		}
	}

	// Retorna o objeto Player, atualizando caso necessário
	public Player getPlayer() {
		if (player == null || !player.isOnline() || !player.isValid()) {
			player = Bukkit.getPlayerExact(name);
		}
		return player;
	}

	// Gera um código aleatório com o comprimento especificado
	private String randomCode(int length) {
		if (length <= 0)
			return "";
		StringBuilder code = new StringBuilder();
		List<Character> chars = new ArrayList<>();

		// Preenche a lista de caracteres com letras de 'a' a 'z'
		for (char i = 'a'; i <= 'z'; i++) {
			chars.add(i);
		}

		Random random = new Random();
		while (code.length() < length) {
			// Gera um caractere aleatório, alternando entre maiúsculo e minúsculo
			char randomChar = chars.get(random.nextInt(chars.size()));
			code.append(random.nextBoolean() ? Character.toLowerCase(randomChar) : Character.toUpperCase(randomChar));
		}
		return code.toString();
	}

	// Envia um pacote para o jogador
	public static void sendPacket(Player p, Packet<?> packet) {
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}

	// Remove o jogador da Manager
	public void remove() {
		player.removeMetadata("playertag", Core.getInstance());
		Manager.get().getPlayers().remove(this);
		if (hologram != null) {
			Bukkit.getOnlinePlayers().forEach(all -> {
				PacketPlayOutEntityDestroy destroyHologram = new PacketPlayOutEntityDestroy(hologram.getId());
				sendPacket(all, destroyHologram);
			});
		}
	}

	// Verifica se a instância de PlayerTag existe para o jogador, cria se não
	// existir
	public static PlayerTag check(Player p) {
		try {
			// Verifica se o jogador já possui uma tag armazenada
			if (p.hasMetadata("playertag")) {
				return (PlayerTag) p.getMetadata("playertag").get(0).value();
			}

			// Caso não tenha, cria uma nova instância
			PlayerTag pt = Manager.get().getPlayer(p.getName());
			if (pt == null) {
				pt = new PlayerTag(p.getName());
			}

			// Armazena a tag do jogador com a metadata
			p.setMetadata("playertag", new FixedMetadataValue(Core.getInstance(), pt));
			return pt;
		} catch (Exception e) {
			if (p != null) p.removeMetadata("playertag", Core.getInstance());
			return check(p);
		}
	}
}
