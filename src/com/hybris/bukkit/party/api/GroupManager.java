package com.hybris.bukkit.party.api;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;

public abstract class GroupManager extends PlayerListener implements CommandExecutor{
	
	public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);
	
	public abstract Group getGroup(Player player);
	
	public abstract Group isLeader(Player player);
	
}