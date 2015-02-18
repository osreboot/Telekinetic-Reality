package com.osreboot.tr.main.hooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.osreboot.tr.main.DataTable;
import com.osreboot.tr.main.Main;

public class ProtectionManager {

	public static HashMap<UUID, UUID> abandoned = new HashMap<UUID, UUID>();
	public static ArrayList<UUID> toRemove = new ArrayList<UUID>();

	public static void tick(){
		for(UUID u : toRemove){
			abandoned.remove(u);
			DataTable.floaters.remove(u);
			DataTable.floatersSnd.remove(u);
		}
		toRemove.clear();
		for(World w : Bukkit.getWorlds()){
			for(Entity e : w.getEntities()){
				for(UUID u : abandoned.keySet()){
					if(e.getUniqueId() == u) if(!Main.canModify(Bukkit.getPlayer(abandoned.get(u)), e.getLocation())) dispose(Bukkit.getPlayer(abandoned.get(u)), e);
				}
				for(UUID u : DataTable.floaters.keySet()){
					if(e.getUniqueId() == u) if(!Main.canModify(Bukkit.getPlayer(DataTable.floaters.get(u)), e.getLocation())) dispose(Bukkit.getPlayer(DataTable.floaters.get(u)), e);
				}
				for(UUID u : DataTable.floatersSnd.keySet()){
					if(e.getUniqueId() == u) if(!Main.canModify(Bukkit.getPlayer(DataTable.floatersSnd.get(u)), e.getLocation())) dispose(Bukkit.getPlayer(DataTable.floatersSnd.get(u)), e);
				}
			}
		}
	}

	public static void dispose(Player p, Entity e){
		toRemove.add(e.getUniqueId());
		p.sendMessage(ChatColor.RED + "Your powers do not extend into this area.");
		e.getWorld().playEffect(e.getLocation(), Effect.MOBSPAWNER_FLAMES, 2000);
		e.remove();
	}

}
