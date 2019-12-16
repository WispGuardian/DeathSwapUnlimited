package com.wispguardian.deathswapunlimited;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Main extends JavaPlugin implements Listener {
	
	BukkitScheduler scheduler = getServer().getScheduler();
	
	boolean deathswapActive, specOnDeath, barsActive;
	long nextSwapTime = (long)(Math.random() * (150-30+1))+30;
	ArrayList<PlayerBar> bossBars = new ArrayList<PlayerBar>();
	
	String prefix = ChatColor.GOLD + "" + ChatColor.BOLD + "[" + ChatColor.AQUA + "Death" + ChatColor.GRAY + "Swap" + ChatColor.GOLD + "" + ChatColor.BOLD + "]" + ChatColor.DARK_AQUA + "" + ChatColor.BOLD;
	
	@Override
    public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
    }
	
    @Override
    public void onDisable() {
    	for(int i = 0; i < bossBars.size(); i++) {
			bossBars.get(i).bar.removeAll();
		}
    	bossBars.clear();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        
        // deathswap command
        if(command.getName().equalsIgnoreCase("deathswap")) {
        	
        	if(!deathswapActive) {
        		getServer().broadcastMessage(prefix + " DEATHSWAP GAME ACTIVATED.");
        		deathswapActive = true;
        		barsActive = false;
        		specOnDeath = true;
        		randomTP();
	        	nextSwap();
        	}else {
        		deathswapActive = false;
        		specOnDeath = false;
        		for(int i = 0; i < bossBars.size(); i++) {
        			bossBars.get(i).bar.removeAll();
        		}
        		bossBars.clear();
//        		scheduler.cancelTasks(this); // don't need because there's a check preswap that will stop everything from happening including queuing the next swap
        		scheduler.cancelTasks(this);
        		getServer().broadcastMessage(prefix + " DEATHSWAP GAME DEACTIVATED.");
        	}
			return true;
        }
        
        if(command.getName().equalsIgnoreCase("spectateondeath")) {
        	specOnDeath = !specOnDeath;
        	if(specOnDeath ) {
        		getServer().broadcastMessage(prefix + " SPECTATE ON DEATH ACTIVATED.");
        	}else {
        		getServer().broadcastMessage(prefix + " SPECTATE ON DEATH DEACTIVATED.");
        	}
        	return true;
        }
        
        return false;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
    	// when player clicks respawn, they will remain in their location and be in spectator mode.
    	if(specOnDeath) {
    		final Player player = e.getEntity();
    		player.setHealth(20);
        	player.spigot().respawn();
        	player.setGameMode(GameMode.SPECTATOR);
    	}
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
    	if(e.getEntity() instanceof Player) {
    		Player p = (Player)e.getEntity();
    		updateBossBar(p, p.getHealth() - e.getDamage());
    	}
    }
    @EventHandler
    public void onEntityHealthRegain(EntityRegainHealthEvent e) {
    	if(e.getEntity() instanceof Player) {
    		Player p = (Player)e.getEntity();
    		updateBossBar(p, p.getHealth() + e.getAmount());
    	}
    }
    
    // for deathswap mode
    public void nextSwap() {
    	if(!barsActive && getPlayersInSurvival().size() == 2) {
    		barsActive = true;
			ArrayList<Player> participants = getPlayersInSurvival();
			ArrayList<Player> online = getOnlinePlayers();
			int num = participants.size();
			
			// bossbar stuff
			if(num == 2) {
				for(int i = 0; i < num; i++) {
					// create bar for each participant
					Player p = participants.get(i);
					bossBars.add(new PlayerBar(p, (i%2==0)?BarColor.BLUE:BarColor.RED));
					// show bar to all players online
					for(int j = 0; j < online.size(); j++) {
						bossBars.get(i).bar.addPlayer(online.get(j));
					}
				}
			}
    	}
		
    	scheduler.scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				if(!deathswapActive) return;
				ArrayList<Player> participants = getPlayersInSurvival();
				
				int num = participants.size();
				ArrayList<Player> swappers = new ArrayList<Player>();
				for(int i = 0; i < num; i++) {
					int randomIndex = (int)(Math.random()*participants.size());
					if(participants.size() == 1) randomIndex = 0;
					swappers.add(participants.get(randomIndex));
					participants.remove(randomIndex);
				}
				// swap all participants (players in survival mode)
				Location l1 = swappers.get(0).getLocation();
				int lastPlayerIndex = swappers.size()-1;
				getServer().broadcastMessage(prefix + ChatColor.RED + ChatColor.ITALIC + " SWAP COMMENCING!");
				for(int i = 0; i < swappers.size(); i++) {
					Player p1 = swappers.get(i);
					if(i == lastPlayerIndex) p1.teleport(l1);
					else p1.teleport(swappers.get(i+1));
				}
				
				nextSwapTime = (long)(Math.random() * (150-30+1))+30;
				nextSwap();
			}
		}, (20*nextSwapTime));
    }
    
    public void randomTP() {
    	ArrayList<Player> players = getPlayersInSurvival();
    	for(int i = 0; i < players.size(); i++) {
    		Player p = players.get(i);
    		Location loc = p.getLocation();
    		loc.setX((int)(Math.random()*(10000-1000+1)+1000));
    		loc.setZ((int)(Math.random()*(10000-1000+1)+1000));
    		loc.setY(loc.getWorld().getHighestBlockAt(loc).getY()+2);
    		p.setInvulnerable(true);
    		p.teleport(loc);
    		p.getInventory().clear();
    		p.sendMessage(prefix + " You have been teleported to a random location.");
    		scheduler.scheduleSyncDelayedTask(this, new Runnable() {
    			@Override
    			public void run() {
    				p.setHealth(p.getHealthScale());
    				p.setFoodLevel(20);
    				p.setInvulnerable(false);
    				p.sendMessage(prefix + " All players healed!");
    			}
    		}, (20*10));
    	}
    }
    
    public ArrayList<Player> getPlayersInSurvival() {
    	Player[] players = new Player[100];
		getServer().getOnlinePlayers().toArray(players); // array of all online players
		ArrayList<Player> participants = new ArrayList<Player>();				// arraylist will hold all online players that are in survival
		for(int i = 0; i < players.length-1; i++) {
			if(players[i] == null) break;
			if(players[i].getGameMode().equals(GameMode.SURVIVAL)) participants.add(players[i]); // put all survival mode players into arraylist
		}
		return participants;
    }
    
    public ArrayList<Player> getOnlinePlayers() {
    	Player[] players = new Player[100];
		getServer().getOnlinePlayers().toArray(players); // array of all online players
		ArrayList<Player> participants = new ArrayList<Player>();				// arraylist will hold all online players that are in survival
		for(int i = 0; i < players.length-1; i++) {
			if(players[i] == null) break;
			participants.add(players[i]); // put all survival mode players into arraylist
		}
		return participants;
    }
    
    public void updateBossBar(Player p, double hp) {
    	for(int i = 0; i < bossBars.size(); i++) {
			if(p.getUniqueId() == bossBars.get(i).player.getUniqueId()) {
				bossBars.get(i).bar.setTitle(p.getName() + ": " + (int)hp + "\u2764");
				bossBars.get(i).bar.setProgress(hp / p.getHealthScale());
			}
		}
    }
    
}
