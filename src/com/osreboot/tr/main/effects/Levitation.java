package com.osreboot.tr.main.effects;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.osreboot.tr.main.DataTable;
import com.osreboot.tr.main.Main;
import com.osreboot.tr.main.NodeEffects;
import com.osreboot.tr.main.Util;
import com.osreboot.tr.main.hooks.ProtectionManager;

public class Levitation extends NodeEffects{

	public Levitation(){}

	public static HashMap<String, Integer> buildup = new HashMap<String, Integer>();

	@Override
	public void onInteract(PlayerInteractEvent evt, DataTable d){
		Player p = evt.getPlayer();
		if(evt.getPlayer().getItemInHand().getType() == Material.AIR){
			if(p.isSneaking()){
				if(evt.getAction() == Action.RIGHT_CLICK_BLOCK){
					Control.crouching.add(p.getName());
					if(Util.getPlayerUUIDS(DataTable.floaters, d.getPlayer().getUniqueId()).size() <= d.nodes[2]*2){
						if(!buildup.containsKey(d.getPlayer().getName())) buildup.put(d.getPlayer().getName(), 0);
						if(buildup.get(d.getPlayer().getName()) > 30 - d.nodes[0]){
							buildup.put(d.getPlayer().getName(), 0);
							Material m = evt.getClickedBlock().getType();
							byte bte = evt.getClickedBlock().getData();
							if(!Main.blacklist.contains(m) && Main.canModify(d.getPlayer(), evt.getClickedBlock().getLocation())){
								d.ping(6);
								evt.getClickedBlock().setType(Material.AIR);
								FallingBlock b = evt.getPlayer().getWorld().spawnFallingBlock(evt.getClickedBlock().getLocation(), m, bte);
								DataTable.floaters.put(b.getUniqueId(), d.getPlayer().getUniqueId());
								b.setDropItem(false);
							}
						}else if(new Random().nextInt(1) == 0) buildup.put(d.getPlayer().getName(), buildup.get(d.getPlayer().getName()) + 1);
					}
				}
			}
			if((evt.getAction() == Action.LEFT_CLICK_AIR || evt.getAction() == Action.LEFT_CLICK_BLOCK) && (p.isSneaking() || p.isSprinting())){
				if(Util.getEntitysFromUUIDs(Util.getPlayerUUIDS(DataTable.floaters, d.getPlayer().getUniqueId())).size() > 0){ 
					d.ping(5);
					for(Entity e : Util.getEntitysFromUUIDs(Util.getPlayerUUIDS(DataTable.floaters, d.getPlayer().getUniqueId()))){
						DataTable.floaters.remove(e.getUniqueId());
						if(p.isSneaking()){
							Util.dropPerfectly(e);
						}
						ProtectionManager.abandoned.put(e.getUniqueId(), d.getPlayer().getUniqueId());
					}
				}
			}
		}
	}

	@Override
	public void tick(DataTable d){
		if(buildup.containsKey(d.getPlayer().getName()) && buildup.get(d.getPlayer().getName()) > 0) if(new Random().nextInt(10) == 0) buildup.put(d.getPlayer().getName(), buildup.get(d.getPlayer().getName()) - 1);

		for(int i = 0; i < Util.getEntitysFromUUIDs(Util.getPlayerUUIDS(DataTable.floaters, d.getPlayer().getUniqueId())).size(); i++){
			d.ping(100);
			if(new Random().nextInt(800 + (d.nodes[22]*50)) == 0 && d.nodes[22] != 30) d.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, new Random().nextInt(120), 1));
			if(new Random().nextInt(600 + (d.nodes[22]*50)) == 0 && d.nodes[22] != 30) d.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, new Random().nextInt(200), 1));
			if(new Random().nextInt(500 + (d.nodes[22]*50)) == 0 && d.nodes[22] != 30) d.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, new Random().nextInt(360), 1));
			if(new Random().nextInt(1000 + (d.nodes[22]*50)) == 0 && d.nodes[22] != 30) d.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, new Random().nextInt(240), 1));
			if(new Random().nextInt(100 + (d.nodes[4]*30)) == 0 && d.nodes[4] != 30) d.getPlayer().damage(new Random().nextInt(8));
		}
		for(Entity e : Util.getEntitysFromUUIDs(Util.getPlayerUUIDS(DataTable.floaters, d.getPlayer().getUniqueId()))){
			if(new Random().nextInt(100 + (d.nodes[3]*20)) == 0 && d.nodes[3] != 30){
				DataTable.floaters.remove(e.getUniqueId());
				Util.dropPerfectly(e);
				ProtectionManager.abandoned.put(e.getUniqueId(), d.getPlayer().getUniqueId());
			}
			if(e.getLocation().distance(d.getPlayer().getLocation()) > d.nodes[19] + 10){
				DataTable.floaters.remove(e.getUniqueId());
				Util.dropPerfectly(e);
				ProtectionManager.abandoned.put(e.getUniqueId(), d.getPlayer().getUniqueId());
			}
			e.setVelocity(new Vector(e.getVelocity().getX(), 0.1, e.getVelocity().getZ()));
			if(e.getTicksLived() > 300){
				DataTable.floaters.remove(e.getUniqueId());
				FallingBlock b = e.getWorld().spawnFallingBlock(e.getLocation(), ((FallingBlock)e).getBlockId(), ((FallingBlock)e).getBlockData());
				DataTable.floaters.put(b.getUniqueId(), d.getPlayer().getUniqueId());
				b.setDropItem(false);
				e.remove();
			}
			if(e.getLocation().getBlockY() > e.getLocation().getWorld().getMaxHeight() - 3){
				DataTable.floaters.remove(e.getUniqueId());
				Util.dropPerfectly(e);
				ProtectionManager.abandoned.put(e.getUniqueId(), d.getPlayer().getUniqueId());
			}
		}
	}
}
