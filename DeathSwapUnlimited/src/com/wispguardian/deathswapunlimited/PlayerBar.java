package com.wispguardian.deathswapunlimited;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class PlayerBar {

	public BossBar bar;
	public Player player;
	
	public PlayerBar(Player player, BarColor colour) {
		this.player = player;
		bar = Bukkit.createBossBar(player.getName() + ": " + (int)(player.getHealth()) + "\u2764", colour, BarStyle.SOLID);
		bar.setVisible(true);
	}
	
}
