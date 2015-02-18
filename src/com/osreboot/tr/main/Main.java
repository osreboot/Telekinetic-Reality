package com.osreboot.tr.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.osreboot.tr.apis.FileAPI;
import com.osreboot.tr.main.hooks.Hook;
import com.osreboot.tr.main.hooks.PreciousStonesHook;
import com.osreboot.tr.main.hooks.ProtectionManager;
import com.osreboot.tr.main.hooks.WorldGuardHook;

public class Main extends JavaPlugin implements Listener{

	public final Logger logger = Logger.getLogger("Minecraft");
	public static Main plugin;

	public static ArrayList<Material> blacklist = new ArrayList<Material>();
	public static ArrayList<Material> liquids = new ArrayList<Material>();
	public static HashMap<Material, Material> breakables = new HashMap<Material, Material>();
	public static HashMap<Material, Material> lights = new HashMap<Material, Material>();
	public static ItemStack scrollup, scrolldown;

	@Override
	public void onEnable(){
		Bukkit.getPluginManager().registerEvents(this, this);
		plugin = this;

		FileAPI.generateFolder("tr_playerdata");

		blacklist.add(Material.CHEST);
		blacklist.add(Material.FURNACE);
		blacklist.add(Material.HOPPER);
		blacklist.add(Material.DISPENSER);
		blacklist.add(Material.ENDER_CHEST);
		blacklist.add(Material.BREWING_STAND);
		blacklist.add(Material.TRAPPED_CHEST);
		blacklist.add(Material.DROPPER);
		blacklist.add(Material.JUKEBOX);
		//as of 0.4.0.0
		blacklist.add(Material.BEACON);
		blacklist.add(Material.BED_BLOCK);
		blacklist.add(Material.COMMAND);
		blacklist.add(Material.IRON_DOOR_BLOCK);
		blacklist.add(Material.WOODEN_DOOR);
		blacklist.add(Material.SIGN_POST);
		blacklist.add(Material.WALL_SIGN);
		blacklist.add(Material.ITEM_FRAME);

		liquids.add(Material.WATER);
		liquids.add(Material.STATIONARY_WATER);
		liquids.add(Material.LAVA);
		liquids.add(Material.STATIONARY_LAVA);

		breakables.put(Material.DIRT, Material.DIRT);
		breakables.put(Material.GRASS, Material.DIRT);
		breakables.put(Material.SAND, Material.SAND);
		breakables.put(Material.STONE, Material.COBBLESTONE);
		breakables.put(Material.GRAVEL, Material.GRAVEL);
		breakables.put(Material.COBBLESTONE, Material.COBBLESTONE);

		lights.put(Material.BEACON, Material.BEACON);
		lights.put(Material.BREWING_STAND, Material.BREWING_STAND_ITEM);
		lights.put(Material.BROWN_MUSHROOM, Material.BROWN_MUSHROOM);
		lights.put(Material.DIODE_BLOCK_ON, Material.DIODE);
		lights.put(Material.DRAGON_EGG, Material.DRAGON_EGG);
		lights.put(Material.ENDER_CHEST, Material.ENDER_CHEST);
		lights.put(Material.GLOWING_REDSTONE_ORE, Material.REDSTONE);
		lights.put(Material.GLOWSTONE, Material.GLOWSTONE_DUST);
		lights.put(Material.JACK_O_LANTERN, Material.JACK_O_LANTERN);
		lights.put(Material.LAVA, Material.AIR);
		lights.put(Material.REDSTONE_COMPARATOR_ON, Material.REDSTONE_COMPARATOR);
		lights.put(Material.REDSTONE_LAMP_ON, Material.REDSTONE_LAMP_OFF);
		lights.put(Material.REDSTONE_TORCH_ON, Material.REDSTONE_TORCH_ON);
		lights.put(Material.STATIONARY_LAVA, Material.AIR);
		lights.put(Material.TORCH, Material.TORCH);
		lights.put(Material.BURNING_FURNACE, Material.FURNACE);
		lights.put(Material.FIRE, Material.AIR);

		NodeInits.init();
		Info.init();
		Help.init();
		holidaysInit();

		scrollup = new ItemStack(Material.EMERALD);
		scrolldown = new ItemStack(Material.EMERALD);

		ItemMeta um = scrollup.getItemMeta();
		um.setDisplayName(ChatColor.YELLOW + "Up");
		scrollup.setItemMeta(um);

		ItemMeta dm = scrolldown.getItemMeta();
		dm.setDisplayName(ChatColor.YELLOW + "Down");
		scrolldown.setItemMeta(dm);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				for(DataTable d : DataTable.tables) d.tick();
				ProtectionManager.tick();
			}
		}, 1, 1);

		for(Player p : Bukkit.getOnlinePlayers()) new DataTable(p);
		
		new WorldGuardHook();
		new PreciousStonesHook();
		
		this.logger.info("Telekinetic Reality : ENABLED");
	}

	@Override
	public void onDisable(){
		for(Player p : Bukkit.getOnlinePlayers()){
			if(p.getOpenInventory() != null && 
					(p.getOpenInventory().getTitle().contains("Syntax Tree :") || p.getOpenInventory().getTitle().contains("Getting to know Telekinesis") || p.getOpenInventory().getTitle().contains("TR Developer Information"))) p.closeInventory();
			DataTable.findPlayer(p).save();
		}
		this.logger.info("Telekinetic Reality : DISABLED");
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt){
		new DataTable(evt.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent evt){
		DataTable.findPlayer(evt.getPlayer()).save();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		Player p = (Player)sender;
		if(label.equalsIgnoreCase("syntax")) DataTable.findPlayer(p).open();
		if(label.equalsIgnoreCase("tr")){
			p.sendMessage(ChatColor.GOLD + "This server is running Telekinetic Reality v" + this.getDescription().getVersion() + " by Os_Reboot (i.g. os_reboot).");
		}
		if(label.equalsIgnoreCase("trlog")){
			for(int i = Changelog.getLog().length - 4; i < Changelog.getLog().length; i++)
				p.sendMessage(ChatColor.GOLD + Changelog.getLog()[i]);
		}
		return false;
	}

	//HOLIDAYS
	/////////////////////////////////////////////////////////////////////////////////////////TODO

	public static ArrayList<Material> decor = new ArrayList<Material>();

	public static void holidaysInit(){
		decor.add(Material.SNOW);
	}

	public static void holidayify(Inventory i){
		/*ItemStack snow = new ItemStack(Material.SNOW);
		nullifyName(snow);

		String layout = 
				"         " + 
				"         " + 
				"         " + 
				"         " + 
				"         " + 
				"SSSSSSSSS";

		for(int n = 0; n < layout.length(); n++){
			if(layout.charAt(n) == 'S') i.setItem(n, snow);
		}*/
	}

	public static ItemStack nullifyName(ItemStack i){
		ItemMeta m = i.getItemMeta();
		m.setDisplayName(" ");
		i.setItemMeta(m);
		return i;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////TODO
	//END HOLIDAYS

	@EventHandler
	public void onInventoryClick(InventoryClickEvent evt){
		if(evt.getInventory().getName().contains("Syntax Tree :")){
			if(evt.getAction() == InventoryAction.PICKUP_ALL){
				if(evt.getInventory().getItem(evt.getSlot()) != null){
					ItemStack i = evt.getInventory().getItem(evt.getSlot());
					Player p = (Player)evt.getWhoClicked();
					DataTable d = DataTable.findPlayer(p);
					if(evt.getInventory().getName() == d.i.getName()){
						if(i.getType() == Material.EMERALD || i.getType() == Material.BOOK || i.getType() == Material.SKULL_ITEM || decor.contains(i.getType())){
							evt.setCancelled(true);
							if(i.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Down")){
								if(d.scroll > 0){
									d.scroll--;
									d.updateInv();
								}
							}else if(i.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Up")){
								if(d.scroll < 9){
									d.scroll++;
									d.updateInv();
								}
							}else if(i.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Need help getting started?")){
								p.closeInventory();
								p.openInventory(Help.help);
							}else if(i.getItemMeta().getDisplayName().equals(ChatColor.BLUE + "Developer Information")){
								p.closeInventory();
								p.openInventory(Info.info);
							}
						}else if(i.getType() == Material.SKULL_ITEM){
							evt.setCancelled(true);
						}else{
							evt.setCancelled(true);
							Node n = Node.findNode(i);
							if(n.isCooperative(d)){
								if(d.skp > 0 && d.nodes[n.getIndex()] < n.getMaxL()){
									if(n.getPreReq() == null || d.nodes[n.getPreReq().getIndex()] >= n.getPreReqL()){
										d.skp--;
										d.nodes[n.getIndex()]++;
										p.sendMessage(ChatColor.GOLD + "Successfully upgraded " + n.getDisplayName() + ChatColor.RESET + "" + ChatColor.GOLD + " to level " + d.nodes[n.getIndex()] + "!");
										p.closeInventory();
										d.open();
									}else{
										p.sendMessage(ChatColor.RED + "You may not upgrade " + n.getDisplayName() + ChatColor.RESET + "" + ChatColor.RED + ", this node is locked!");
									}
								}else if(d.nodes[n.getIndex()] >= n.getMaxL()){
									p.sendMessage(ChatColor.RED + "You may not upgrade " + n.getDisplayName() + ChatColor.RESET + "" + ChatColor.RED + ", it is already maximum level!");
								}else if(d.skp <= 0){
									p.sendMessage(ChatColor.RED + "You may not upgrade " + n.getDisplayName() + ChatColor.RESET + "" + ChatColor.RED + ", you do not have the required skill points!");
								}
							}
						}
					}
				}
			}else{
				evt.setCancelled(true);
			}
		}else if(evt.getInventory().getName().contains("Getting to know Telekinesis")){//TODO compress this into a method?
			evt.setCancelled(true);
			if(evt.getAction() == InventoryAction.PICKUP_ALL && evt.getInventory().getItem(evt.getSlot()) != null){
				ItemStack i = evt.getInventory().getItem(evt.getSlot());
				Player p = (Player)evt.getWhoClicked();
				DataTable d = DataTable.findPlayer(p);

				if(i.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Back")){
					p.closeInventory();
					d.open();
				}
			}
		}else if(evt.getInventory().getName().contains("TR Developer Information")){
			evt.setCancelled(true);
			if(evt.getAction() == InventoryAction.PICKUP_ALL){
				if(evt.getInventory().getItem(evt.getSlot()) != null){
					ItemStack i = evt.getInventory().getItem(evt.getSlot());
					Player p = (Player)evt.getWhoClicked();
					DataTable d = DataTable.findPlayer(p);
					if(i.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Back")){
						p.closeInventory();
						d.open();
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent evt){
		DataTable.findPlayer(evt.getPlayer()).onInteract(evt);
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent evt){
		DataTable.findPlayer(evt.getPlayer()).onInteractEntity(evt);
	}

	@EventHandler
	public void onMove(PlayerMoveEvent evt){
		DataTable.findPlayer(evt.getPlayer()).onMove(evt);
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent evt){
		Util.clearHash(DataTable.floaters, evt.getEntity().getName());
		Util.clearHash(DataTable.floatersSnd, evt.getEntity().getName());
	}
	
	/**
	 * @return Checks all compatible Telekinetic Reality API hooks and returns whether or not the location is protected
	 */
	public static boolean canModify(Player p, Location l){
		boolean canModify = true;
		for(Hook h : Hook.hooks) if(h.isLoaded() && !h.canModify(p, l)) canModify = false;
		if(!canModify) p.sendMessage(ChatColor.RED + "Your powers do not extend into this area.");
		return canModify;
	}
}
