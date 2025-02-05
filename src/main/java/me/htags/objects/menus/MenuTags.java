package me.htags.objects.menus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.hawkutils.utils.API;
import me.hawkutils.utils.Glass;
import me.hawkutils.utils.Item;
import me.hawkutils.utils.RowCreator;
import me.hawkutils.utils.menus.Menu;
import me.htags.Core;
import me.htags.objects.ConfigTag;
import me.htags.objects.PlayerTag;
import me.htags.objects.manager.Manager;

@Getter
public class MenuTags extends Menu {

	private Inventory inv;
	private RowCreator page;
	private int[] slots = {11,12,13,14,15};
	private Item backPage, nextPage, itemCreateTag;
	private Glass glass;
	
	public MenuTags() {
		super("§0H_TagsV"+Core.getInstance().getDescription().getVersion() + " - {page}/{totalPage}", 3);
		setCancelClick(true);
		setRemoveOnClose(true);
		inv = getInventory();
		List<String> list = new ArrayList<>();
		list.add("160>15:all");
		list.add("160>7:11,17");
		glass = new Glass(list, getSize());
		
		nextPage = new Item(API.getItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2M2MDJkYmE5Zjk3MDFhZTAwMzllNzQ4MWNlYmY2MWM1OGZlOGQzOWQyOWM5MjdiNDg4YmVlNDIyZDlhNjJkNCJ9fX0="));
		nextPage.setDisplayName("§bPassar Página");
		nextPage.setSlot(16);
		
		backPage = new Item(API.getItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjEzNGJhMjQxZjg3M2Q1ZWY0YzUyNmViMjkxYjVjMTZkNTA3ZDVhMGM2ZjFhMmU2NTAzZWM1OWIzNjNhMzY3NSJ9fX0="));
		backPage.setDisplayName("§bVoltar Página");
		backPage.setSlot(10);
		
		itemCreateTag = new Item(API.getItemStack("386>0"));
		itemCreateTag.setDisplayName("§bCriar Tag");
		List<String> lore = new ArrayList<>();
		lore.add("§7Clique para criar uma tag.");
		itemCreateTag.setLore(lore);
		itemCreateTag.setSlot(22);
		
	}
	
	@Override
	public Menu open(Player p) {
		List<Object> items = new ArrayList<>();
		Manager.get().getTags().forEach(configtag -> items.add(new ItemTag(p, configtag)));
		items.sort((o1, o2) -> {
		    String pos1 = ((ItemTag) o1).getTag().getPosition();
		    String pos2 = ((ItemTag) o2).getTag().getPosition();
		    return pos1.compareToIgnoreCase(pos2);
		});
		page = new RowCreator(items, slots.length);
		setItems(p);
		return super.open(p);
	}
	
	public void setItems(Player p) {
		inv.setContents(new ItemStack[getSize()*9]);
		for(Item it : glass.getGlassItems()) {
			inv.setItem(it.getSlot(), it.build().clone());
		}
		if (page.containsPage(page.getPaginaAtual()+1)) inv.setItem(nextPage.getSlot(), nextPage.build().clone());
		if (page.containsPage(page.getPaginaAtual()-1)) inv.setItem(backPage.getSlot(), backPage.build().clone());
		inv.setItem(itemCreateTag.getSlot(), itemCreateTag.build().clone());
		for (int i = 0; i < slots.length; i++) {
			int slot = slots[i];
			try {
				ItemTag tag = (ItemTag) page.getPage(page.getPaginaAtual()).get(i);
				inv.setItem(slot, tag.getItem().build().clone());
			} catch (Exception e) {
				Item item = new Item(Material.BARRIER);
				item.setDisplayName("§cslot vazio");
				inv.setItem(slot, item.build().clone());
			}
		}
		setTitleInventory(p, getTitle().replace("{page}", String.valueOf(page.getPaginaAtual())).replace("{totalPage}", String.valueOf(page.getTotalPages())));
		p.updateInventory();
	}
	
	@Override
	public void onClick(InventoryClickEvent e) {
		super.onClick(e);
		if (!e.getInventory().equals(inv)) return;
		if (e.getClickedInventory() != null && !e.getClickedInventory().equals(inv)) return;
		if (e.getCurrentItem() == null) return;
		Player p = (Player) e.getWhoClicked();
		PlayerTag pt = PlayerTag.check(p);
		if (e.getCurrentItem().isSimilar(backPage.build())) {
			page.setPaginaAtual(page.getPaginaAtual()-1);
			setItems(p);
			return;
		}
		if (e.getCurrentItem().isSimilar(nextPage.build())) {
			page.setPaginaAtual(page.getPaginaAtual()+1);
			setItems(p);
			return;
		}
		if (e.getCurrentItem().getType() != Material.BARRIER && containsSlot(e.getSlot())) {
			ConfigTag tag = getTagOfSlot(e.getSlot());
			if (e.getClick() == ClickType.RIGHT) {
				pt.setTagSelected(tag);
				pt.setSettingPosition(true);
				p.closeInventory();
				for (int i = 0; i < 250; i++) {
					p.sendMessage(new String());
				}
				p.playSound(p.getLocation(), Sound.LEVEL_UP, 0.5f, 10);
				p.sendMessage(Core.getInstance().getTag() + " §fDigite no chat a posição que você vai querer para a tag, ou digite §ccancelar §fpara cancelar!");
				return;
			}
			if (e.getClick() == ClickType.LEFT) {
				pt.setTagSelected(tag);
				pt.setSettingPermission(true);
				p.closeInventory();
				for (int i = 0; i < 250; i++) {
					p.sendMessage(new String());
				}
				p.playSound(p.getLocation(), Sound.LEVEL_UP, 0.5f, 10);
				p.sendMessage(Core.getInstance().getTag() + " §fDigite no chat a permissão que você vai querer para a tag, ou digite §ccancelar §fpara cancelar!");
				return;
			}
			if (e.getClick() == ClickType.SHIFT_RIGHT) {
				pt.setTagSelected(tag);
				pt.setSettingSuffix(true);
				p.closeInventory();
				for (int i = 0; i < 250; i++) {
					p.sendMessage(new String());
				}
				p.playSound(p.getLocation(), Sound.LEVEL_UP, 0.5f, 10);
				p.sendMessage(Core.getInstance().getTag() + " §fDigite no chat a suffix que você vai querer para a tag, ou digite §ccancelar §fpara cancelar!");
				return;
			}
			if (e.getClick() == ClickType.SHIFT_LEFT) {
				pt.setTagSelected(tag);
				pt.setSettingPrefix(true);
				p.closeInventory();
				for (int i = 0; i < 250; i++) {
					p.sendMessage(new String());
				}
				p.playSound(p.getLocation(), Sound.LEVEL_UP, 0.5f, 10);
				p.sendMessage(Core.getInstance().getTag() + " §fDigite no chat a prefix que você vai querer para a tag, ou digite §ccancelar §fpara cancelar!");
				return;
			}
			if (e.getClick() == ClickType.NUMBER_KEY) {
				try {
					YamlConfiguration yaml = YamlConfiguration.loadConfiguration(Core.getInstance().getFileTags());
					yaml.set(tag.getName(), null);
					yaml.save(Core.getInstance().getFileTags());
					Core.getInstance().reloadPlugin();
					p.sendMessage(Core.getInstance().getTag() + " §aVocê deletou a tag §f" + tag.getName() + " §acom sucesso!");
					p.closeInventory();
					new MenuTags().open(p);
				} catch (Exception e2) {
					e2.printStackTrace();
					p.sendMessage(Core.getInstance().getTag() + " §cNão foi possivel deletar a tag!");
					p.closeInventory();
				}
				return;
			}
		}
		if (e.getCurrentItem().isSimilar(itemCreateTag.build())) {
			p.closeInventory();
			pt.setCreatingTag(true);
			for (int i = 0; i < 250; i++) {
				p.sendMessage(new String());
			}
			p.playSound(p.getLocation(), Sound.LEVEL_UP, 0.5f, 10);
			p.sendMessage(Core.getInstance().getTag() + " §fDigite no chat o nome que você vai querer para a sua tag, ou digite §ccancelar §fpara cancelar!");
			return;
		}
	}
	
	private ConfigTag getTagOfSlot(int slot) {
		for (int i = 0; i < slots.length; i++) {
			int slt = slots[i];
			try {
				if (slt == slot) {
					return ((ItemTag)page.getPage(page.getPaginaAtual()).get(i)).getTag();
				}
			} catch (Exception e) {
				continue;
			}
		}
		return null;
	}
	
	private boolean containsSlot(int number) {
		for(int slot : slots) {
			if (slot == number) return true;
		}
		return false;
	}
	
	@Getter
	private class ItemTag {
		
		private ConfigTag tag;
		private Item item;
		
		public ItemTag(Player p, ConfigTag tag) {
			this.tag = tag;
			this.item = new Item(API.getItemStack("421>0"));
			item.setDisplayName("§7Nome: §e"+tag.getName().toUpperCase());
			List<String> lore = new ArrayList<>();
			lore.add("§9§m----------------------------");
			lore.add("§9# §7Permissão: §6" + tag.getPermissionGroup());
			lore.add("§9# §7Posição: §e" + tag.getPosition().toUpperCase());
			lore.add("§9# §7Prefix: §f" + tag.getPrefix().replace("&", "§"));
			lore.add("§9# §7Suffix: §f" + tag.getSuffix().replace("&", "§"));
			lore.add("§9§m----------------------------");
			lore.add(" ");
			lore.add("§aRIGHT-CLICK");
			lore.add("§7Setar a posição da tag.");
			lore.add(" ");
			lore.add("§aLEFT-CLICK");
			lore.add("§7Setar a permissão da tag.");
			lore.add(" ");
			lore.add("§aSHIFT+RIGHT-CLICK");
			lore.add("§7Setar a suffix da tag.");
			lore.add(" ");
			lore.add("§aSHIFT+LEFT-CLICK");
			lore.add("§7Setar a prefix da tag.");
			lore.add(" ");
			lore.add("§cNUMBER-KEY");
			lore.add("§7Deletar a tag.");
			lore.add(" ");
			lore.replaceAll(l -> PlaceholderAPI.setPlaceholders(p, l.replace("&", "§")));
			item.setLore(lore);
		}
		
	}

}
