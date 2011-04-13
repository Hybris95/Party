package com.hybris.bukkit.party;

import org.bukkit.plugin.java.JavaPlugin;
import  java.util.logging.Logger;

/**
* Party plugin for Bukkit
* @version 0.4a
* @author Hybris95
*/
public class Party extends JavaPlugin
{
	
	private PartyGroupManager manager;
	private Logger log;
	
	public void onLoad(){}

	public void onEnable(){
		this.log = this.getServer().getLogger();
		this.log.info("[Party] enabling...");
		this.manager = new PartyGroupManager(this);
		this.log.info("[Party] enabled!");
	}
	
	public void onDisable(){
		this.log.info("[Party] disabling...");
		this.manager.deleteGroups();
		this.manager = null;
		this.log.info("[Party] disabled!");
		this.log = null;
	}
	
	public PartyGroupManager getManager(){
		return this.manager;
	}
	
}