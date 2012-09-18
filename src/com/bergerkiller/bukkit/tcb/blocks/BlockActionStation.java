package com.bergerkiller.bukkit.tcb.blocks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.bergerkiller.bukkit.common.BlockMap;
import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.common.permissions.NoPermissionException;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.Permission;
import com.bergerkiller.bukkit.tc.TrainCarts;

public class BlockActionStation extends BlockAction {

	@Override
	public Material getDefaultMaterial() {
		return Material.OBSIDIAN;
	}

	@Override
	public String getName() {
		return "station";
	}
	
	private double launchDistance = 10;
	private BlockMap<BlockFace> launchDirections = new BlockMap<BlockFace>();
	
	@Override
	public void load(ConfigurationNode node) {
		super.load(node);
		this.launchDistance = node.get("launchDistance", this.launchDistance);
	}
		
	@Override
	public void onGroupEnter(MinecartGroup group, Block block) {
		launchDirections.put(block, group.head().getDirectionTo());
		group.clearActions();
		if (block.isBlockIndirectlyPowered() != this.isPowerInverted()) {
			//launch
			group.head().addActionLaunch(this.launchDistance, TrainCarts.launchForce);
		} else {
			//stop
			group.middle().addActionLaunch(block.getLocation().add(0.5, 1.5, 0.5), 0.0);
			if (TrainCarts.playSoundAtStation) {
				group.addActionSizzle();
			}
			group.addActionWaitForever();
		}
	}
	
	@Override
	public void onRedstoneChange(Block block, boolean powered) {
		if (powered != this.isPowerInverted()) {
			MinecartMember member = MinecartMember.getAt(block.getRelative(BlockFace.UP));
			if (member != null) {
				member.getGroup().clearActions();
				BlockFace face = this.launchDirections.get(block);
				if (face == null) {
					member.addActionLaunch(this.launchDistance, TrainCarts.launchForce);
				} else {
					member.addActionLaunch(face, this.launchDistance, TrainCarts.launchForce);
				}
			}
		}
	}
	
	@Override
	public boolean onBuild(Player player, Block block) throws NoPermissionException {
		Permission.BUILD_STATION.handle(player);
		player.sendMessage(ChatColor.GREEN + "You built the station action sign!");
		player.sendMessage(ChatColor.YELLOW + "It can stop and launch trains.");
		return true;
	}
	
}
