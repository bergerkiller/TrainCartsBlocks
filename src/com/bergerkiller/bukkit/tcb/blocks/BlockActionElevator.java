package com.bergerkiller.bukkit.tcb.blocks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.bergerkiller.bukkit.common.permissions.NoPermissionException;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.Permission;
import com.bergerkiller.bukkit.tc.Util;
import com.bergerkiller.bukkit.tc.signactions.SignActionElevator;

public class BlockActionElevator extends BlockAction {

	@Override
	public Material getDefaultMaterial() {
		return Material.GOLD_BLOCK;
	}

	@Override
	public String getName() {
		return "elevator";
	}
	
	public Block findElevator(Block from, BlockFace mode) {
		while ((from = Util.findRailsVertical(from, mode)) != null) {
			if (from.getRelative(BlockFace.DOWN).getTypeId() == this.getMaterial().getId()) {
				return from;
			}
		}
		return null;
	}
	
	public void doElevator(Block from, MinecartMember member) {
		from = from.getRelative(BlockFace.UP);
		if (SignActionElevator.ignoreTimes.isMarked(from, 1000)) {
			return;
		}
		Block next = findElevator(from, BlockFace.UP);
		if (next == null) {
			next = findElevator(from, BlockFace.DOWN);
		}
		if (next != null) {
			SignActionElevator.ignoreTimes.mark(next);
			BlockFace dir = SignActionElevator.getSpawnDirection(next);
			member.getGroup().teleportAndGo(next, dir);
		}
	}
	
	@Override
	public void onMemberEnter(MinecartMember member, Block block) {
		if (block.isBlockIndirectlyPowered() != this.isPowerInverted()) {
			doElevator(block, member);
		}
	}
	
	@Override
	public void onRedstoneChange(Block block, boolean powered) {
		if (powered != this.isPowerInverted()) {
			MinecartMember mm = MinecartMember.getAt(block.getRelative(BlockFace.UP));
			if (mm != null) {
				doElevator(block, mm);
			}
		}
	}

	@Override
	public boolean onBuild(Player player, Block block) throws NoPermissionException {
		Permission.BUILD_ELEVATOR.handle(player);
		player.sendMessage(ChatColor.GREEN + "You built the elevator action sign!");
		player.sendMessage(ChatColor.YELLOW + "It can teleport trains upwards or downwards alternatively");
		return true;
	}

}
