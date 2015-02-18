package com.osreboot.tr.main.hooks;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PreciousStonesHook extends Hook{
	
	public PreciousStonesHook(){
		super("PreciousStones");
	}
	
	@Override
	public boolean canModify(Player p, Location l){
		return PreciousStones.API().canBreak(p, l) && PreciousStones.API().canPlace(p, l);
	}
	
}
