package com.hybris.bukkit.party.api;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.Plugin;
import java.util.logging.Logger;

import com.hybris.bukkit.party.Party;

/**
 * If extended, do not forget to super in the onLoad, onEnable and onDisable
 */
public abstract class RequiredPartyPlugin extends PartyPlugin{
	
	protected Logger log;
	
	/**
	 * Disallowed setManager access always return null and does nothing
	 */
	protected final GroupManager setManager(GroupManager manager){
		return null;
	}
	
	public void onLoad(){
		this.log = this.getServer().getLogger();
	}
	
	public void onEnable(){
		PluginManager pluginManager = this.getServer().getPluginManager();
		Plugin party = pluginManager.getPlugin("Party");
		if(party == null){
			this.log.severe(this.getDescription().getName() + " did not found plugin Party");
			this.getPluginLoader().disablePlugin(this);
			return;
		}
		pluginManager.enablePlugin(party);
		
		Party partyPlugin = (Party)party; // Unchecked cast
		super.setManager(partyPlugin.getManager());
	}
	
	public void onDisable(){
		super.setManager(null);
	}
	
}