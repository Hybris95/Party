package com.hybris.bukkit.party.api;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class PartyPlugin extends JavaPlugin{
	
	private GroupManager manager;
	
	public final GroupManager getManager(){
		return this.manager;
	}
	
	protected GroupManager setManager(GroupManager manager){
		GroupManager toReturn = this.manager;
		this.manager = manager;
		return toReturn;
	}
	
}