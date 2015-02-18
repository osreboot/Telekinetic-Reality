package com.osreboot.tr.main.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class WorldGuardHook extends Hook{
	
	public WorldGuardHook(){
		super("WorldGuard");
	}
	
	@Override
	public boolean canModify(Player p, Location l){
		return ((WorldGuardPlugin)this.getPlugin()).canBuild(p, l);
	}
	
}
