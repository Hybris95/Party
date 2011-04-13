package com.hybris.bukkit.party.api;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.Plugin;
import java.util.logging.Logger;

import com.hybris.bukkit.party.Party;

/**
 * If extended, do not forget to super in the onLoad, onEnable and onDisable
 */
public abstract class OptionalPartyPlugin extends PartyPlugin{
	
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
			this.log.info(this.getDescription().getName() + " did not found plugin Party [Optional]");
			this.setManager(null);
			return;
		}
		else if(party instanceof Party){
			if(!pluginManager.isEnabled(party)){
				pluginManager.enablePlugin(party);
			}
			
			Party partyPlugin = (Party)party;
			this.setManager(partyPlugin.getManager());
		}
		else{
			this.log.info(this.getDescription().getName() + " found plugin Party but which is not Party... [Optional]");
			this.setManager(null);
			return;
		}
	}
	
	public void onDisable(){
		this.setManager(null);
	}
	
}