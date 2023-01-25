package me.liki;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Listener;

public class Tpa extends JavaPlugin implements Listener {
	private class _Location {
		String world;
		double x;
		double y;
		double z;
	}
	
	//Log messages in minecraft console
	Logger log = Logger.getLogger("Minecraft");
	//Hashmap<Player,location>
	HashMap<UUID,_Location> backMap = new HashMap<>();
	//Keeps track whether tp is off or on
	boolean tp_off = false;
	
	
	//Dictionary keeping track of teleport requests
	static TargetMap targetMap = new TargetMap();
	
//Help functions----------------------------------------------------------------------------------------------------------------------------
	private boolean teleport(Player senderPlayer,Player targetPlayer) {
		targetMap.remove_pair(senderPlayer,targetPlayer);
		targetPlayer.sendMessage(ChatColor.AQUA + "Teleport request accepted");
		senderPlayer.sendMessage(ChatColor.AQUA + "Teleporting...");
		senderPlayer.teleport(targetPlayer);
		addLocation(senderPlayer);
		return true;
	}
	
	private boolean no_tp(Player senderPlayer,Player targetPlayer) {
		targetMap.remove_pair(senderPlayer,targetPlayer);
		targetPlayer.sendMessage(ChatColor.AQUA + "Teleport request denied");
		senderPlayer.sendMessage(ChatColor.AQUA + "Teleport request denied");
		return true;
	}
	
	private boolean back_tp(Player player) {
		player.sendMessage(ChatColor.AQUA + "Teleporting...");
		_Location location = backMap.get(player.getUniqueId());
		Location loc = new Location(getServer().getWorld(location.world),location.x,location.y,location.z);
		player.teleport(loc);
		backMap.remove(player.getUniqueId());
		return true;
	}
	
	private boolean sendMessage(String message, CommandSender player) {
		player.sendMessage(ChatColor.AQUA + message);
		log.log(Level.INFO, String.format("[Tpa] " + message));
		return false;
	}
	
	private void addLocation(Player player) {
		Location location = player.getLocation();
		backMap.computeIfAbsent(player.getUniqueId(), k -> new _Location()).world = location.getWorld().getName();
		backMap.get(player.getUniqueId()).x = location.getX();
		backMap.get(player.getUniqueId()).y = location.getY();
		backMap.get(player.getUniqueId()).z = location.getZ();
	}
	
//Enable en disable-----------------------------------------------------------------------------------------------------------------------//
	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this,this);
		log.log(Level.INFO, String.format("[Tpa] Succesfully started up"));
	}
	@Override
	public void onDisable() {
		log.log(Level.INFO, String.format("[Tpa] Succesfully disabled tpa"));
	}	

//Command handling------------------------------------------------------------------------------------------------------------------------//
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		//General conditions
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.AQUA + "Liki, stop playing around with the console");
			return true;
		}		
		//Handle tpoff
		if (command.getName().equals("tptoggle")) {
			if (!(sender.hasPermission("tpa.off"))) {
				sendMessage("You don't have permission to send this command",sender);
			}
			tp_off = !tp_off;
			if(tp_off) {
				sender.sendMessage(ChatColor.AQUA + "Tpa is now off");
			} else {
				sender.sendMessage(ChatColor.AQUA + "Tpa is now on");
			}
			return true;
		}
// Back ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (command.getName().equals("back")) {
			//Permission check
			if (!sender.hasPermission("tpa.backondeath") && !sender.hasPermission("tpa.back")) {
				return sendMessage("You don't have permission to use this command",sender);
			}
			if (args.length != 0) {
				return sendMessage("Wrong number of arguments",sender);
			}
			Player player = (Player) sender;
			if (!(backMap.containsKey(player.getUniqueId()))) {
				sender.sendMessage(ChatColor.AQUA + "You don't have a back location");
				return false;
			}
			return back_tp(player);
		}
		//Tp_off
		if (tp_off) {
			return sendMessage("Tpa is currently disabled, ask " + ChatColor.GREEN + "Brick " + ChatColor.AQUA + "for more information",(Player) sender);
		}
		
//Tpa ask //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (command.getName().equals("tpa") || command.getName().equals("tpask")) {
			//Permission check
			if (!sender.hasPermission("tpa.tpa")) {
				return sendMessage("You don't have permission to use this command",sender);
			}
			if (args.length != 1) {
				return sendMessage("Wrong number of arguments",sender);
			}
			if (!getServer().getOnlinePlayers().contains(getServer().getPlayer(args[0]))) {
				return sendMessage("Player is not online!",sender);
			}
			Player senderPlayer = (Player) sender;
			Player targetPlayer = (Player) getServer().getPlayer(args[0]);
			if (targetPlayer == senderPlayer) {
				return sendMessage("You cannot teleport to yourself", sender);
			}
			targetMap.ask_request(senderPlayer,targetPlayer);
			(new BukkitRunnable() {
				public void run() {
					Tpa.targetMap.remove_pair(senderPlayer,targetPlayer);
				}
			}).runTaskLaterAsynchronously((JavaPlugin) this, 1800L);
			return true;
		}
//Tpa accept ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (command.getName().equals("tpaccept") || command.getName().equals("tpyes")) {
			//Permission check
			if (!sender.hasPermission("tpa.tpa")) {
				return sendMessage("You don't have permission to use this command",sender);
			}
			if (args.length == 0) {
				Player targetPlayer = (Player) sender;
				UUID sender_UUID = targetMap.get_player(targetPlayer);
				Player senderPlayer = getServer().getPlayer(sender_UUID);
				if (senderPlayer != null) {
					return teleport(senderPlayer,targetPlayer);
				}
				return false;
			} else if (args.length == 1) {
				Player targetPlayer = (Player) sender;
				Player senderPlayer = getServer().getPlayer(args[0]);
				if (targetMap.accept_request(senderPlayer,targetPlayer)) {
					return teleport(senderPlayer,targetPlayer);
				}
				return false;
			} else {
				return sendMessage("Wrong number of arguments",sender);
			}
		}
//Tpa deny /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (command.getName().equals("tpdeny") || command.getName().equals("tpno")) {
			//Permission check
			if (!sender.hasPermission("tpa.tpa")) {
				return sendMessage("You don't have permission to use this command",sender);
			}
			if (args.length == 0) {
				Player targetPlayer = (Player) sender;
				UUID sender_UUID = targetMap.get_player(targetPlayer);
				Player senderPlayer = getServer().getPlayer(sender_UUID);
				if (senderPlayer != null) {
					return no_tp(senderPlayer,targetPlayer);
				}
				return false;
			} else if (args.length == 1) {
				Player targetPlayer = (Player) sender;
				Player senderPlayer = getServer().getPlayer(args[0]);
				if (targetMap.accept_request(senderPlayer,targetPlayer)) {
					return no_tp(senderPlayer,targetPlayer);
				}
				return false;
			} else {
				return sendMessage("Wrong number of arguments",sender);
			}
		}
		return sendMessage("Unknown command",sender);
	}
//Playerdeath-----------------------------------------------------------------------------------------------------------------------------//
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (player.hasPermission("tpa.backondeath") || player.hasPermission("tpa.back")) {
			addLocation(player);
			player.setBedSpawnLocation(new Location(getServer().getWorld("world"),0,101,0),true);
			(new BukkitRunnable() {
				public void run() {
					player.sendMessage(ChatColor.AQUA + "You died, congrats, you can use " + ChatColor.RED + "/back" + ChatColor.AQUA + " to return to your death location");
				}
			}).runTaskLaterAsynchronously((JavaPlugin) this, 1L);
		}
	}
}
