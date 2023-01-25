package me.liki;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;


public class TargetMap {
	static HashMap<UUID,LinkedList<UUID>> map = new HashMap<>();
	
	public boolean accept_request(Player senderPlayer, Player targetPlayer) {
		//Senderplayer is present
		if (!(map.containsKey(targetPlayer.getUniqueId()))) {
			targetPlayer.sendMessage(ChatColor.AQUA + "You don't have any pending requests");
			return false;
		}
		if (!(map.get(targetPlayer.getUniqueId()).contains(senderPlayer.getUniqueId()))) {
			targetPlayer.sendMessage(ChatColor.AQUA + "No pending request from that player");
			return false;
		}
		return true;
	}
	public UUID get_player(Player targetPlayer) {
		if (!(map.containsKey(targetPlayer.getUniqueId()))) {
			targetPlayer.sendMessage(ChatColor.AQUA + "You don't have any pending requests");
			return null;
		}
		UUID sender_UUID = map.get(targetPlayer.getUniqueId()).getFirst();
		return sender_UUID;
	}
	public void remove_pair(Player senderPlayer, Player targetPlayer) {
		map.get(targetPlayer.getUniqueId()).remove(senderPlayer.getUniqueId());
		if(map.get(targetPlayer.getUniqueId()).size() == 0) {
			map.remove(targetPlayer.getUniqueId());
		}
	}
	public void ask_request(Player senderPlayer, Player targetPlayer) {
		if (map.computeIfAbsent(targetPlayer.getUniqueId(), k -> new LinkedList<>()).contains(senderPlayer.getUniqueId())) {
			senderPlayer.sendMessage(ChatColor.AQUA + "You already have a pending request to this player");
			return;
		}
		targetPlayer.sendMessage(ChatColor.AQUA + "Request from " + senderPlayer.getName());
		senderPlayer.sendMessage(ChatColor.AQUA + "Sent request to " + targetPlayer.getName());
		map.computeIfAbsent(targetPlayer.getUniqueId(), k -> new LinkedList<>()).addLast(senderPlayer.getUniqueId());
	}
}
