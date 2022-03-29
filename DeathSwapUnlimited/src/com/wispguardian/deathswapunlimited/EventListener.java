package com.wispguardian.deathswapunlimited;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class EventListener implements Listener {

	@EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
    	// when player clicks respawn, they will remain in their location and be in spectator mode.
    	if(Main.specOnDeath) {
    		final Player player = e.getEntity();
    		player.setHealth(20);
        	player.spigot().respawn();
        	player.setGameMode(GameMode.SPECTATOR);
    	}
    	
    	// check how many players remaining (win detection)
    	ArrayList<Player> participants = Main.getPlayersInSurvival();
    	if(participants.size() == 1) {
    		Player winner = participants.get(0);
    		Bukkit.broadcastMessage(Main.prefix + " " + winner.getName() + " wins!");
    		Main.disableGame();
    		ArrayList<Player> online = Main.getOnlinePlayers();
    		for(int i = 0; i < online.size(); i++) {
    			online.get(i).teleport(winner);
    		}
    	}
    }
    
	@EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
		if(!Main.deathswapActive) return;
    	if(e.getEntity() instanceof Player) {
    		Player p = (Player)e.getEntity();
    		updateBossBar(p, p.getHealth() - e.getDamage());
    	}
    }
	
	@EventHandler
    public void onEntityHealthRegain(EntityRegainHealthEvent e) {
		if(!Main.deathswapActive) return;
    	if(e.getEntity() instanceof Player) {
    		Player p = (Player)e.getEntity();
    		double newhp = p.getHealth() + e.getAmount();
    		double maxhp = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue();
    		if(newhp > maxhp) newhp = maxhp;
    		updateBossBar(p, newhp);
    	}
    }
	
	public void updateBossBar(Player p, double hp) {
    	for(int i = 0; i < Main.bossBars.size(); i++) {
    		PlayerBar bar = Main.bossBars.get(i);
			if(p.getUniqueId() == bar.player.getUniqueId()) {
				bar.bar.setTitle(p.getName() + ": " + (int)hp + "\u2764");
				bar.bar.setProgress(hp / p.getHealthScale());
			}
		}
    }
	
}
