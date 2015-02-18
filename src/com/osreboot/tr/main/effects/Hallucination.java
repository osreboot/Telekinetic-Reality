package com.osreboot.tr.main.effects;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.osreboot.tr.main.DataTable;
import com.osreboot.tr.main.NodeEffects;
import com.osreboot.tr.main.Util;

public class Hallucination extends NodeEffects{

	public Hallucination(){}

	public static HashMap<String, Integer> active = new HashMap<String, Integer>();
	public static HashMap<String, Integer> buildup = new HashMap<String, Integer>();
	public static HashMap<String, Integer> extinguish = new HashMap<String, Integer>();

	@Override
	public void tick(DataTable d){
		if(!active.containsKey(d.getPlayer().getName())) active.put(d.getPlayer().getName(), 0);
		if(!buildup.containsKey(d.getPlayer().getName())) buildup.put(d.getPlayer().getName(), 0);
		if(!extinguish.containsKey(d.getPlayer().getName())) extinguish.put(d.getPlayer().getName(), 0);
		if(buildup.get(d.getPlayer().getName()) > 0) buildup.put(d.getPlayer().getName(), buildup.get(d.getPlayer().getName()) - 1);
		if(active.get(d.getPlayer().getName()) > 0){
			active.put(d.getPlayer().getName(), active.get(d.getPlayer().getName()) - 1);

			if(active.get(d.getPlayer().getName()) == 1) d.getPlayer().playSound(d.getPlayer().getLocation(), Sound.FIZZ, 10, 1);

			d.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20, 1), true);

			if(d.getPlayer().isSneaking()) extinguish.put(d.getPlayer().getName(), extinguish.get(d.getPlayer().getName()) + 1);
		}
		if(extinguish.get(d.getPlayer().getName()) > 40){
			extinguish.put(d.getPlayer().getName(), 0);
			buildup.put(d.getPlayer().getName(), 0);
			active.put(d.getPlayer().getName(), 0);
			d.getPlayer().playSound(d.getPlayer().getLocation(), Sound.FIZZ, 10, 1);
			d.ping(30);
		}
	}

	@Override
	public void onMove(PlayerMoveEvent evt, DataTable d){
		Player p = evt.getPlayer();
		if(d.nodes[16] > 0){
			if(evt.getFrom().getX() == evt.getTo().getX() && evt.getFrom().getY() == evt.getTo().getY() && evt.getFrom().getZ() == evt.getTo().getZ()){
				if(d.getPlayer().isSneaking() && active.containsKey(d.getPlayer().getName()) && active.get(d.getPlayer().getName()) == 0 && Util.getPlayerUUIDS(DataTable.floaters, d.getPlayer().getName()).size() == 0){
					if(buildup.get(d.getPlayer().getName()) < (40 - d.nodes[16])*4){
						buildup.put(d.getPlayer().getName(), buildup.get(d.getPlayer().getName()) + 4);
					}else{
						active.put(d.getPlayer().getName(), 100 + (d.nodes[16]*10) + new Random().nextInt(d.nodes[16]*100));
						d.getPlayer().playSound(d.getPlayer().getLocation(), Sound.NOTE_PIANO, 10, 1);
						d.ping(20);
					}
				}	
			}
			extinguish.put(p.getName(), 0);
		}
	}

}
