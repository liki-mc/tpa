package me.liki;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.Location;

public class iPlayer extends Player {
	@overwrite
	void teleport(Player player) {
		this.teleport((Location) Player.getlocation());
	}
	
	@overwrite
	void teleport(Location location) {
	
	}
}
