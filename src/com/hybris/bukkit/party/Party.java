package com.hybris.bukkit.party;

import org.bukkit.plugin.java.JavaPlugin;
import  java.util.logging.Logger;

import com.hybris.bukkit.party.api.PartyPlugin;
import com.hybris.bukkit.party.api.GroupManager;

/**
* Party plugin for Bukkit
* @author Hybris95
*/
public class Party extends PartyPlugin
{
	
	private Logger log;
	
	public void onLoad(){}

	public void onEnable(){
		this.log = this.getServer().getLogger();
		this.log.info("[Party] enabling...");
		this.setManager(new PartyGroupManager(this));
		this.log.info("[Party] enabled!");
	}
	
	public void onDisable(){
		this.log.info("[Party] disabling...");
		GroupManager manager = this.getManager();
		if(manager instanceof PartyGroupManager){
			((PartyGroupManager)manager).deleteGroups();
		}
		super.setManager(null);
		this.log.info("[Party] disabled!");
		this.log = null;
	}
	
	/**
	 * Disallowed setManager access always return null and does nothing
	 */
	protected final GroupManager setManager(GroupManager manager){
		return null;
	}
	
}