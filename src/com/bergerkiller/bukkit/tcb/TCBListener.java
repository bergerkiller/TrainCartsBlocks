package com.bergerkiller.bukkit.tcb;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.bergerkiller.bukkit.common.permissions.NoPermissionException;
import com.bergerkiller.bukkit.tc.Util;
import com.bergerkiller.bukkit.tc.events.MemberBlockChangeEvent;
import com.bergerkiller.bukkit.tcb.blocks.BlockAction;

public class TCBListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void onMemberBlockChange(MemberBlockChangeEvent event) {
		BlockAction.onBlockChange(event.getFrom(), event.getTo(), event.getMember());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (!event.isCancelled()) {
			BlockAction.onBlockUpdate(event.getBlock());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!event.isCancelled()) {
			Block block = event.getBlock();
			if (Util.ISTCRAIL.get(block)) {
				block = block.getRelative(BlockFace.DOWN);
				BlockAction ba = BlockAction.get(block);
				if (ba != null) {
					try {
						if (ba.onBuild(event.getPlayer(), block)) {
							return;
						}
					} catch (NoPermissionException ex) {
						event.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to make this action block!");
					}
					event.setCancelled(true);
				}
			}
		}
	}

}
