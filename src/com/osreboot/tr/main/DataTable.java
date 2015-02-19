package com.osreboot.tr.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.osreboot.tr.apis.StatsFile;

public class DataTable {

	public static ArrayList<DataTable> tables = new ArrayList<DataTable>();

	public static DataTable findPlayer(Player p){
		for(DataTable d : tables) if(d.p == p) return d;
		return null;
	}

	public Player p;
	public int[] nodes = new int[Node.nodes.size()];
	public Inventory i;
	public int skp;
	public int discovered = 0;
	public int total = 0;
	public int syntax = 0;
	public int scroll = 0;
	
	private Random random;

	public static HashMap<UUID, String> floaters = new HashMap<UUID, String>();
	public static HashMap<UUID, String> floatersSnd = new HashMap<UUID, String>();

	public DataTable(Player p){
		this.p = p;

		StatsFile f = new StatsFile(p.getName(), "tr_playerdata");
		if(!f.doesExist()){
			f.generate();
			f.load();
			for(Node n : Node.nodes) f.getConfiguration().set(n.getName(), "0");
			f.getConfiguration().set("skp", "0");
			f.getConfiguration().set("discovered", "0");
			f.getConfiguration().set("total", "0");
			f.getConfiguration().set("syntax", "0");
		}else f.load();

		for(Node n : Node.nodes) this.nodes[n.getIndex()] = verifyAndCache(f, n.getName());
		this.skp = verifyAndCache(f, "skp");
		this.discovered = verifyAndCache(f, "discovered");
		this.total = verifyAndCache(f, "total");
		this.syntax = verifyAndCache(f, "syntax");

		this.random = new Random();
		
		tables.add(this);
	}

	public Player getPlayer(){
		return this.p;
	}

	public int getSyntax(){
		return this.syntax;
	}

	public void setSyntax(int syntax){
		this.syntax = syntax;
	}

	private int verifyAndCache(StatsFile f, String path){
		if(!f.getConfiguration().contains(path)) f.getConfiguration().set(path, "0");
		return f.getConfiguration().getInt(path);
	}

	public void save(){
		Util.clearHash(floaters, this.p.getName());
		Util.clearHash(floatersSnd, this.p.getName());
		StatsFile f = new StatsFile(p.getName(), "tr_playerdata");
		f.load();
		f.getConfiguration().set("skp", this.skp);
		f.getConfiguration().set("discovered", this.discovered);
		f.getConfiguration().set("total", this.total);
		f.getConfiguration().set("syntax", this.syntax);
		for(Node n : Node.nodes) f.getConfiguration().set(n.getName(), this.nodes[n.index]);
		f.save();
	}

	public void open(){
		this.i = Bukkit.createInventory(null, 54, "Syntax Tree : " + this.skp + " Skill Points");
		this.updateInv();
		this.p.openInventory(this.i);
	}

	public void updateInv(){
		this.i.clear();

		Main.holidayify(this.i);
		
		for(Node n : Node.nodes){
			if((n.getPreReq() == null || this.nodes[n.getPreReq().getIndex()] >= n.getPreReqL()) && n.isCooperative(this) && n.getInvSpace() + (this.scroll*9) < 54) this.i.setItem(n.getInvSpace() + (this.scroll*9), n.getItem());
		}
		for(ItemStack item : this.i.getContents()){
			if(item != null  && !Main.decor.contains(item.getType())){
				ItemMeta m = item.getItemMeta();
				if(this.nodes[Node.findNode(item).getIndex()] == 0) m.setDisplayName(m.getDisplayName() + ChatColor.DARK_GRAY + " [Not Activated]");
				else if(this.nodes[Node.findNode(item).getIndex()] == Node.findNode(item).getMaxL()) m.setDisplayName(m.getDisplayName() + ChatColor.RED + " [Max Level]");
				else m.setDisplayName(m.getDisplayName() + ChatColor.GREEN + " [" + this.nodes[Node.findNode(item).getIndex()] + "/" + Node.findNode(item).getMaxL() + "]");
				if(this.nodes[Node.findNode(item).getIndex()] > 0 && Node.findNode(item).controls != null){
					ArrayList<String> lore = new ArrayList<String>();
					for(String s : Node.findNode(item).getData()) lore.add(s);
					for(String s : Node.findNode(item).controls) lore.add(s);
					m.setLore(lore);
				}else m.setLore(Node.findNode(item).getData());
				item.setItemMeta(m);
			}
		}
		
		ItemStack title = new ItemStack(Material.SKULL_ITEM, 1, (short)SkullType.PLAYER.ordinal());
		ItemMeta m = title.getItemMeta();
		
		String name = "";
		if(this.total >= 40) name += ChatColor.AQUA;
		if(this.total >= 80) name += ChatColor.DARK_AQUA;
		if(this.total >= 120) name += ChatColor.BLUE;
		if(this.total >= 160) name += ChatColor.DARK_BLUE;
		if(this.total >= 200) name += ChatColor.LIGHT_PURPLE;
		if(this.total >= 240) name += ChatColor.DARK_PURPLE;
		name += this.getPlayer().getName() + ", level " + this.total;
		name += " Adept ";
		if(this.syntax == 1) name += "Equilibrial";
		else if(this.syntax == 2) name += "Aqueous";
		else if(this.syntax == 3) name += "Detrimental";
		else if(this.syntax == 4) name += "Oculus";
		else name += "Neophyte";
		
		m.setDisplayName(name);
		title.setItemMeta(m);
		this.i.setItem(0, title);
		this.i.setItem(8, Main.scrollup);
		this.i.setItem(53, Main.scrolldown);
		this.i.setItem(45, Help.helpi);
		this.i.setItem(36, Info.infoi);
	}

	public void tick(){
		for(int i = 0; i < this.nodes.length; i++){
			if(Node.findNode(i).getEffects() != null && this.nodes[i] != 0)
				Node.findNode(i).getEffects().tick(this);
		}
	}

	public void onInteract(PlayerInteractEvent evt){
		boolean first = true;
		for(int i = 0; i < this.nodes.length; i++){
			if(this.nodes[i] != 0) first = false;
			if(Node.findNode(i).getEffects() != null && this.nodes[i] != 0)
				Node.findNode(i).getEffects().onInteract(evt, this);
		}
		if(first && evt.getPlayer().getItemInHand().getTypeId() == 0 && p.isSneaking() && evt.getAction() == Action.RIGHT_CLICK_BLOCK) this.ping(100);
	}

	public void onInteractEntity(PlayerInteractEntityEvent evt){
		for(int i = 0; i < this.nodes.length; i++){
			if(Node.findNode(i).getEffects() != null && this.nodes[i] != 0)
				Node.findNode(i).getEffects().onInteractEntity(evt, this);
		}
	}

	public void onMove(PlayerMoveEvent evt){
		for(int i = 0; i < this.nodes.length; i++){
			if(Node.findNode(i).getEffects() != null && this.nodes[i] != 0)
				Node.findNode(i).getEffects().onMove(evt, this);
		}
	}

	public void ping(int r){
		if(random.nextInt(r) == 0){
			if(this.discovered < 10 + (this.total/4)) this.discovered++; else{
				this.discovered = 0;
				this.total++;
				this.skp++;
				this.p.playSound(this.p.getLocation(), Sound.ORB_PICKUP, 10, 1);

				if(this.total >= 180) this.p.sendMessage(ChatColor.LIGHT_PURPLE + "The power has consumed you. The aura lives inside you. The world crumples at your will.");
				else if(this.total >= 160) this.p.sendMessage(ChatColor.LIGHT_PURPLE + "The aura is a second sense. You lift hills in your sleep. Energy buzzes at your fingertips.");
				else if(this.total >= 140) this.p.sendMessage(ChatColor.DARK_BLUE + "The aura brings another sight. You can feel world around you, without having to open your eyes.");
				else if(this.total >= 120) this.p.sendMessage(ChatColor.DARK_BLUE + "You can sense power. Power will make you immortal, invincible, unlimited. If you only can grasp it.");
				else if(this.total >= 100) this.p.sendMessage(ChatColor.BLUE + "The aura is your ally. There is strength just around the corner.");
				else if(this.total >= 80) this.p.sendMessage(ChatColor.BLUE + "You can lift things now. Objects move as you command, with some effort.");
				else if(this.total >= 60) this.p.sendMessage(ChatColor.AQUA + "The aura feels a little stronger. Is it that far out of your grasp?");
				else if(this.total >= 40) this.p.sendMessage(ChatColor.AQUA + "You can feel a faint aura of energy. Are those particles moving in sync with your thoughts?");
				else if(this.total >= 20) this.p.sendMessage(ChatColor.WHITE + "There is a pattern to how objects move around you. Nothing you do seems to influence it.");
				else this.p.sendMessage(ChatColor.WHITE + "Dust moves across the ground... but is it really the wind?");

				if(this.total >= 160) this.p.sendMessage(ChatColor.LIGHT_PURPLE + "A new skill point is available.");
				else if(this.total >= 120) this.p.sendMessage(ChatColor.DARK_BLUE + "A new skill point is available.");
				else if(this.total >= 80) this.p.sendMessage(ChatColor.BLUE + "A new skill point is available.");
				else if(this.total >= 40) this.p.sendMessage(ChatColor.AQUA + "A new skill point is available.");
				else this.p.sendMessage(ChatColor.WHITE + "A new skill point is available.");
			}
		}
	}
}
