package com.hybris.bukkit.party;

import org.bukkit.entity.Player;
import java.util.ArrayList;

import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;

import com.hybris.bukkit.party.api.*;

public class PartyGroup extends Group{
	
	public void onEntityDamage(EntityDamageEvent event){
		if(event.isCancelled()){return;}
		
		if(!EntityDamageByEntityEvent.class.isAssignableFrom(event.getClass())){return;}
		EntityDamageByEntityEvent transtypedEvent = (EntityDamageByEntityEvent)event;
		
		EntityDamageEvent.DamageCause cause = event.getCause();
		if(cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK){return;}
		
		if(event.getDamage() <= 0){return;}
		
		Entity damaged = transtypedEvent.getEntity();
		Entity damager = transtypedEvent.getDamager();
		
		if(((damaged != null) && (damager != null)) && (HumanEntity.class.isAssignableFrom(damaged.getClass()) && HumanEntity.class.isAssignableFrom(damager.getClass()))){
			HumanEntity transtypedDamaged = (HumanEntity)damaged;
			HumanEntity transtypedDamager = (HumanEntity)damager;
			
			if(this.members.contains(transtypedDamaged.getName()) && this.members.contains(transtypedDamager.getName())){
				transtypedEvent.setCancelled(true);
				return;
			}
		}
		return;
	}
	
	private PartyGroupManager manager;
	
	protected Player master;
	protected ArrayList<String> members;
	protected int maxSize;
	protected int currentSize;
	protected ArrayList<String> invitedMembers;
	
	protected PartyGroup(PartyGroupManager manager, int maxSize, Player master, Player invited) throws GroupException{
		
		if(maxSize < 2){throw new GroupException("Internal Error:Group size too small");}
		if(master == null || invited == null){throw new GroupException("Internal Error:Either the master or the first member isn't mentionned");}
		if(manager == null){throw new GroupException("Internal Error:No manager given");}
		
		if(master.getName().equals(invited.getName())){
			throw new GroupException("You cannot invite yourself to make a new group");
		}
		
		this.manager = manager;
		
		this.maxSize = maxSize;
		this.currentSize = 2;
		this.members = new ArrayList<String>(this.maxSize);
		this.invitedMembers = new ArrayList<String>(this.maxSize - this.currentSize);
		this.members.add(master.getName());
		this.members.add(invited.getName());
		this.master = master;
	}
	
	protected boolean addMember(Player added){
		if(!this.invitedMembers.contains(added.getName())){return false;}
		if(this.currentSize >= this.maxSize){return false;}
		if(this.members.contains(added.getName())){return false;}
		if(this.master.getName().equals(added.getName())){return false;}
		
		this.currentSize++;
		this.invitedMembers.remove(added.getName());
		this.manager.removeFromAllInvitedGroups(added);
		this.members.add(added.getName());
		this.invitedMembers.ensureCapacity(this.maxSize - this.currentSize);
		return true;
	}
	
	protected boolean inviteMember(Player invited){
		if(this.invitedMembers.contains(invited.getName())){return false;}
		if(this.members.contains(invited.getName())){return false;}
		if((this.maxSize - this.currentSize - this.invitedMembers.size()) <= 0){return false;}
		if(this.master.getName().equals(invited.getName())){return false;}

		this.invitedMembers.add(invited.getName());
		return true;
	}
	
	protected boolean removeMember(Player removed){
		if(!this.members.contains(removed.getName())){return false;}
		
		this.members.remove(removed.getName());
		this.currentSize--;
		this.invitedMembers.ensureCapacity(this.maxSize - this.currentSize);
		
		if(this.currentSize < 2){
			this.getMember(this.members.toArray()[0].toString()).sendMessage("Due to insufficent number of teammates, the party has been destroyed");
			this.manager.deleteGroup(this);
		}
		else if(removed.getName().equals(this.master.getName())){
			this.master = this.getMember(this.members.toArray()[0].toString());
			this.master.sendMessage("You are the new leader of the party");
			for(String memberName : this.members){
				Player member = this.getMember(memberName);
				if(!memberName.equals(this.master)){
					member.sendMessage(this.master + " is the new leader of the party");
				}
			}
		}
		return true;
	}
	
	protected boolean deinviteMember(Player deinvited){
		if(!this.invitedMembers.contains(deinvited.getName())){return false;}
		
		this.invitedMembers.remove(deinvited.getName());
		return true;
	}
	
	public final ArrayList<Player> getMembers(){
		ArrayList<Player> members = new ArrayList<Player>(this.members.size());
		for(String memberName : this.members){
			members.add(this.getMember(memberName));
		}
		return members;
	}
	
	public final Player getMember(String name){
		if(this.members.contains(name)){
			return this.manager.getPlayer(name);
		}
		else{
			return null;
		}
	}
	
	public final Player getMaster(){
		return this.master;
	}
}