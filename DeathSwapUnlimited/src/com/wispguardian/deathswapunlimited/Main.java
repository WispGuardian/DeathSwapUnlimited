package com.wispguardian.deathswapunlimited;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Main extends JavaPlugin {
	
	BukkitScheduler scheduler = getServer().getScheduler();
	
	public static boolean deathswapActive, specOnDeath, barsActive;
	public static Main instance;
	long nextSwapTime = (long)(Math.random() * (150-30+1))+30;
	public static ArrayList<PlayerBar> bossBars = new ArrayList<PlayerBar>();
	
	public final Material[] disabledBlocks = {
		Material.WATER, Material.LILY_PAD, Material.KELP, Material.KELP_PLANT,
		Material.SEA_PICKLE, Material.SEAGRASS, Material.LAVA
	};
	
	public static String prefix = ChatColor.GOLD + "" + ChatColor.BOLD + "[" + ChatColor.AQUA + "Death" + ChatColor.GRAY + "Swap" + ChatColor.GOLD + "" + ChatColor.BOLD + "]" + ChatColor.DARK_AQUA + "" + ChatColor.BOLD;
	
	@Override
    public void onEnable() {
		Main.instance = this;
		getServer().getPluginManager().registerEvents(new EventListener(), this);
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
        		Main.disableGame();
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
				
				// randomize order of participants arraylist
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
    	ArrayList<Material> disabled_blocks = new ArrayList<>(Arrays.asList(disabledBlocks));
    	World world = players.get(0).getWorld();
    	for(int i = 0; i < players.size(); i++) {
    		Player p = players.get(i);
    		Location loc = null;
    		while(loc == null || disabled_blocks.contains(loc.getBlock().getRelative(BlockFace.DOWN).getType())) {
	    		loc = new Location(world, (int)(Math.random()*(10000-1000+1)+1000),
	    				0,
	    				(int)(Math.random()*(10000-1000+1)+1000));
	    		loc.setY(world.getHighestBlockAt(loc).getY());
    		}
    		loc.setY(loc.getY()+1);
    		p.setInvulnerable(true);
    		p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
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
    
    public static ArrayList<Player> getPlayersInSurvival() {
    	Player[] players = new Player[100];
    	Main.instance.getServer().getOnlinePlayers().toArray(players);			// array of online players
		ArrayList<Player> participants = new ArrayList<Player>();				// arraylist will hold all online players that are in survival
		for(int i = 0; i < players.length; i++) {
			if(players[i] == null) break;
			if(players[i].getGameMode().equals(GameMode.SURVIVAL)) {
				participants.add(players[i]); 									// put all survival mode players into arraylist
			}
		}
		return participants;
    }
    
    public static ArrayList<Player> getOnlinePlayers() {
    	Player[] players = new Player[100];
		Main.instance.getServer().getOnlinePlayers().toArray(players); 			// array of all online players
		ArrayList<Player> participants = new ArrayList<Player>();				// arraylist will hold all online players that are in survival
		for(int i = 0; i < players.length-1; i++) {
			if(players[i] == null) break;
			participants.add(players[i]);
		}
		return participants;
    }
    
    public static void disableGame() {
    	deathswapActive = false;
		specOnDeath = false;
		for(int i = 0; i < bossBars.size(); i++) {
			bossBars.get(i).bar.removeAll();
		}
		bossBars.clear();
		Main.instance.scheduler.cancelTasks(Main.instance);
    }
    
}
