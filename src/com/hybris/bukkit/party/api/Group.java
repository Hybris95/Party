package com.hybris.bukkit.party.api;

import org.bukkit.entity.Player;
import java.util.ArrayList;

import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageEvent;

public abstract class Group extends EntityListener{
	
	public abstract ArrayList<Player> getMembers();
	
	public abstract Player getMember(String name);
	
	public abstract Player getMaster();
}