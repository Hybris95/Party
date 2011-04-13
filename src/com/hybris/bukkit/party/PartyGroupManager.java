package com.hybris.bukkit.party;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.Event;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.ArrayList;

import com.hybris.bukkit.party.api.*;

public class PartyGroupManager extends GroupManager{
	
	private Server server;
	private JavaPlugin plugin;
	protected HashMap<String,Group> groups;
	protected HashMap<String,Group> tmpGroups;
	protected int groupSize;
	
	protected PartyGroupManager(JavaPlugin plugin){
		this.server = plugin.getServer();
		this.plugin = plugin;
		this.groups = new HashMap<String,Group>();
		this.tmpGroups = new HashMap<String,Group>();
		this.groupSize = 5; // TODO Make the size configurable
		
		this.server.getPluginCommand("party").setExecutor(this);
		this.server.getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Normal, this.plugin);
		this.server.getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Normal, this.plugin);
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		
		if(sender.getServer() != this.server){
			sender.sendMessage("You have to be in the same server (LOGGED COMMAND)");
			return false;
		} // You have to be in the same server
		
		if(!Player.class.isAssignableFrom(sender.getClass())){
			sender.sendMessage("You have to be a Player");
			return false;
		} // You have to be a Player
		
		Player senderPlayer = (Player)sender;
		
		if(!command.getName().equalsIgnoreCase("party")){
			sender.sendMessage("You have to say /party");
			return false;
		} // You have to say /party
		
		if(args.length == 0){
			sender.sendMessage("You have to give at least one argument");
			return false;
		} // You have to give at least one argument
		
		String subcommand = args[0];
		Player player = null;
		
		if(subcommand.equalsIgnoreCase("join") || subcommand.equalsIgnoreCase("invite") || subcommand.equalsIgnoreCase("deinvite") || subcommand.equalsIgnoreCase("kick")){
			if(args.length < 2){
				sender.sendMessage("You have to give at least another argument");
				return false;
			} // You have to give at least another argument
			
			String name = args[1];
			player = sender.getServer().getPlayer(name);
			
			if(player == null){
				sender.sendMessage("You have to give an existing player as an argument");
				return false;
			} // You have to give an existing player as an argument
		}
		else if(subcommand.equalsIgnoreCase("leave") || subcommand.equalsIgnoreCase("list")){}
		else{
			sender.sendMessage("You have to give a known subcommand");
			return false;
		} // You have to give a known subcommand
			
		this.server = sender.getServer();
		
		if(subcommand.equalsIgnoreCase("join")){
			return this.join(senderPlayer, player);
		}
		else if(subcommand.equalsIgnoreCase("invite")){
			return this.invite(senderPlayer, player);
		}
		else if(subcommand.equalsIgnoreCase("deinvite")){
			return this.deinvite(senderPlayer, player);
		}
		else if(subcommand.equalsIgnoreCase("kick")){
			return this.kick(senderPlayer, player);
		}
		else if(subcommand.equalsIgnoreCase("leave")){
			return this.leave(senderPlayer);
		}
		else if(subcommand.equalsIgnoreCase("list")){
			return this.list(senderPlayer);
		}
		else{
			sender.sendMessage("Internal Error: Did not returned as it should have (LOGGED COMMAND)");
			return false;
		}
	}
	
	protected boolean join(Player sender, Player sended){
		
		Group senderGroup = this.getGroup(sender);
		Group sendedGroup = this.isLeader(sended);
		
		if(senderGroup == null){
			if((sendedGroup != null) && (sendedGroup instanceof PartyGroup)){
				if(((PartyGroup)sendedGroup).addMember(sender)){
					sender.sendMessage("You have successfully joined " + sended.getName() + " party");
					this.server.getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, sendedGroup, Event.Priority.High, this.plugin);
					return true;
				}
				else{
					sender.sendMessage("You cannot join  " + sended.getName() + " party");
					return false;
				}
			}
			else if(((sendedGroup = this.tmpGroups.get(sended.getName())) != null) && (sendedGroup instanceof PartyGroup)){
				if(((PartyGroup)sendedGroup).getMember(sender.getName()) != null){
					PartyGroup realGroup = null;
					try{
						realGroup = new PartyGroup(this, this.groupSize, sended, sender);
					}
					catch(GroupException e){
						sender.sendMessage(e.getMessage());
						return false;
					}
					this.server.getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, realGroup, Event.Priority.High, this.plugin);
					sender.sendMessage("You have joined " + sended.getName() + " party");
					sended.sendMessage(sender.getName() + " has joined your party");
					this.groups.put(sended.getName(),realGroup);
					this.tmpGroups.remove(sender.getName());
					return true;
				}
				else{
					return this.invite(sender, sended);
				}
			}
			else{
				return this.invite(sender, sended);
			}
		}
		else{
			sender.sendMessage("You are already in a group");
			return false;
		}
	}
	
	protected boolean invite(Player sender, Player sended){
		
		Group senderGroup = this.isLeader(sender);
		Group sendedGroup = this.getGroup(sended);
		
		if(sendedGroup == null){
			if((senderGroup != null) && (senderGroup instanceof PartyGroup)){
				if(((PartyGroup)senderGroup).inviteMember(sended)){
					sended.sendMessage(sender.getName() + " invited you to his party");
					sender.sendMessage("You invited " + sended.getName() + " to your party");
					return true;
				}
				else{
					sender.sendMessage("Could not invite " + sended.getName());
					return false;
				}
			}
			else if((senderGroup = this.getGroup(sender)) != null){
				sender.sendMessage("You are not the leader of your group, ask " + senderGroup.getMaster().getName());
				return false;
			}
			else{
				if(this.tmpGroups.containsKey(sender.getName())){
					sender.sendMessage("You have already invited " + sended.getName() + " to your party");
					return false;
				}
				else{
					PartyGroup tmpGroup = null;
					try{
						tmpGroup = new PartyGroup(this, 2, sender, sended);
					}
					catch(GroupException e){
						sender.sendMessage(e.getMessage());
						return false;
					}
					sended.sendMessage(sender.getName() + " invited you to his party");
					sender.sendMessage("Waiting for " + sended.getName() + " to join the party..");
					this.tmpGroups.put(sender.getName(),tmpGroup);
					return true;
				}
			}
		}
		else{
			sender.sendMessage(sended.getName() + " is already in a group");
			return false;
		}
	}
	
	protected boolean deinvite(Player sender, Player sended){
		
		Group senderGroup = this.isLeader(sender);
		Group tmpGroup = this.tmpGroups.get(sender.getName());
		
		if((senderGroup != null) && (senderGroup instanceof PartyGroup)){
			if(((PartyGroup)senderGroup).deinviteMember(sended)){
				sender.sendMessage("You have deinvited " + sended.getName());
				return true;
			}
			else{
				sender.sendMessage("You haven't invited " + sended.getName());
				return false;
			}
		}
		else if((tmpGroup != null) && (tmpGroup instanceof PartyGroup)){
			if(tmpGroup.getMember(sended.getName()) != null){
				this.tmpGroups.remove(sender.getName());
				sender.sendMessage("You have deinvited " + sended.getName());
				return true;
			}
			else{
				sender.sendMessage("You haven't invited " + sended.getName());
				return false;
			}
		}
		else{
			sender.sendMessage("You cannot deinvite " + sended.getName());
			return false;
		}
		
	}
	
	protected boolean kick(Player sender, Player sended){
		
		Group senderGroup = this.isLeader(sender);
		
		if((senderGroup != null) && (senderGroup instanceof PartyGroup)){
			if(senderGroup.getMember(sended.getName()) != null){
				if(((PartyGroup)senderGroup).removeMember(sended)){
					sender.sendMessage(sended.getName() + " has been kicked");
					return true;
				}
				else{
					sender.sendMessage("Could not kick " + sended.getName());
					return false;
				}
			}
			else{
				sender.sendMessage(sended.getName() + " is not in your party");
				return false;
			}
		}
		else{
			sender.sendMessage("You cannot kick " + sended.getName());
			return false;
		}
		
	}
	
	protected boolean leave(Player sender){
		
		Group senderGroup = this.getGroup(sender);
		
		if((senderGroup != null) && (senderGroup instanceof PartyGroup)){
			if(((PartyGroup)senderGroup).removeMember(sender)){
				sender.sendMessage("You left your party");
				return true;
			}
			else{
				sender.sendMessage("Couldn't leave your party");
				return false;
			}
		}
		else{
			sender.sendMessage("You are not in a group");
			return false;
		}
		
	}
	
	protected boolean list(Player sender){
		
		Group senderGroup = this.getGroup(sender);
		if(senderGroup != null){
			Player master = senderGroup.getMaster();
			ArrayList<Player> members = senderGroup.getMembers();
			
			sender.sendMessage("Members of the group :");
			String toDisplay = "";
			for(Player member : members){
				if(member != sender){
					if(member == master){
						toDisplay += ("[LEADER]" + member.getName() + " ");
					}
					else{
						toDisplay += (member.getName() + " ");
					}
				}
			}
			sender.sendMessage(toDisplay);
		}
		else{
			sender.sendMessage("You are not in a group");
		}
		
		return true;
	}
	
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		Group playerGroup = this.isLeader(player);
		
		String playerMessage = "PARTY:";
		String memberMessage = "JOINED: ";
		
		if(playerGroup != null){
			playerMessage = "Your party was waiting for you:";
			memberMessage = "The leader is back: ";
			// TODO TempLeaders system
		}
		else if((playerGroup = this.getGroup(player)) != null){
			playerMessage = "Your teammates were waiting for you:";
			memberMessage = "Your teammate is back: ";
		}
		else{
			return;
		}
		
		player.sendMessage(playerMessage);
		ArrayList<Player> members = playerGroup.getMembers();
		String membersOutput = "";
		for(Player member : members){
			if(!member.getName().equals(player.getName())){
				membersOutput += (member.getName() + " ");
				member.sendMessage(memberMessage + player.getName());
			}
		}
		player.sendMessage(membersOutput);
		
	}
	
	public void onPlayerQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();
		this.removeFromAllInvitedGroups(player);
		
		Group playerGroup = this.isLeader(player);
		
		String prefix = "THE MEMBER ";
		
		if(playerGroup != null){
			prefix = "The leader ";
			// TODO TempLeaders system
		}
		else if((playerGroup = this.getGroup(player)) != null){
			prefix = "";
		}
		else{
			return;
		}
		
		ArrayList<Player> members = playerGroup.getMembers();
		for(Player member : members){
			if(!member.getName().equals(player.getName())){
				member.sendMessage(prefix + player.getName() + " just disconnected but is still in the party");
			}
		}
		
	}
	
	private Group[] convertObjectArray(Object[] array){
		if(array.length < 1){
			return new Group[0];
		}
		
		if(Group.class.isAssignableFrom(array[0].getClass())){
			Group[] groups = new Group[array.length];
			for(int i = 0; i < array.length; i++){
				Object o = array[i];
				groups[i] = (Group)o;
			}
			return groups;
		}
		else{
			return new Group[0];
		}
	}
	
	public final Group getGroup(Player player){
		Group[] groups = this.convertObjectArray(this.groups.values().toArray());
		for(Group group : groups){
			if(group.getMember(player.getName()) != null){
				return group;
			}
		}
		return null;
	}
	
	public final Group isLeader(Player player){
		Group[] groups = this.convertObjectArray(this.groups.values().toArray());
		for(Group group : groups){
			if(group.getMaster().getName().equals(player.getName())){
				return group;
			}
		}
		return null;
	}
	
	protected void deleteGroups(){
		Group[] groups = this.convertObjectArray(this.groups.values().toArray());
		for(Group group : groups){
			this.deleteGroup(group);
		}
		this.tmpGroups.clear();
	}
	
	protected boolean deleteGroup(Group group){
		ArrayList<Player> members = group.getMembers();
		Player master = group.getMaster();
		if(this.groups.containsKey(master.getName())){
			if(group instanceof PartyGroup){
				for(Player member : members){
					if(!member.getName().equals(master.getName())){
						((PartyGroup)group).removeMember(member);
					}
				}
			}
			this.groups.remove(master.getName());
			return true;
		}
		else{
			return false;
		}
		
	}
	
	protected void removeFromAllInvitedGroups(Player player){
		Group[] tmpGroups = this.convertObjectArray(this.tmpGroups.values().toArray());
		for(Group tmpGroup : tmpGroups){
			if(tmpGroup instanceof PartyGroup){
				((PartyGroup)tmpGroup).removeMember(player);
			}
		}
		
		Group[] groups = this.convertObjectArray(this.groups.values().toArray());
		for(Group group : groups){
			if(group instanceof PartyGroup){
				((PartyGroup)group).deinviteMember(player);
			}
		}
	}
	
	Player getPlayer(String name){
		if(this.server == null){return null;}
		return this.server.getPlayer(name);
	}
	
}