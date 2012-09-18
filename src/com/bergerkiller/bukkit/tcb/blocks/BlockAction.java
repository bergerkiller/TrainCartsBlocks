package com.bergerkiller.bukkit.tcb.blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.bergerkiller.bukkit.common.BlockSet;
import com.bergerkiller.bukkit.common.Task;
import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.common.permissions.NoPermissionException;
import com.bergerkiller.bukkit.common.utils.BlockUtil;
import com.bergerkiller.bukkit.common.utils.EnumUtil;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.TrainCarts;

public abstract class BlockAction {
	
	public static void init(ConfigurationNode materials) {
		register(materials, new BlockActionStation());
		register(materials, new BlockActionEjector());
		register(materials, new BlockActionElevator());
	}
	
	private static Map<Material, BlockAction> blocks = new HashMap<Material, BlockAction>();
	public static <T extends BlockAction> T register(Material material, T block) {
		blocks.put(material, block);
	    block.material = material;
		return block;
	}
	public static <T extends BlockAction> T register(String matname, T block) {
		Material mat = block.getDefaultMaterial();
		if (matname != null && matname.length() > 0) {
			mat = EnumUtil.parseMaterial(matname, mat);
		}
		return register(mat, block);
	}
	public static <T extends BlockAction> T register(ConfigurationNode config, T block) {
		config = config.getNode(block.getName());
		register(config.get("material", block.getDefaultMaterial().toString()), block).load(config);
		return block;
	}
	
	public static void deinit() {
		blocks.clear();
	}
		
	public static BlockAction get(Material material) {
		return blocks.get(material);
	}
	public static BlockAction get(Block block) {
		return get(block.getType());
	}
	
	public static void onBlockChange(Block from, Block to, MinecartMember member) {
		if (member.isDerailed()) {
			return; //just in case?
		}
		from = from.getRelative(BlockFace.DOWN);
		to = to.getRelative(BlockFace.DOWN);
		if (!BlockUtil.equals(from, to)) {
			BlockAction old = get(from);
			if (old != null) {
				old.onBlockLeave(member, from);
			}
		}
		BlockAction neww = get(to);
		if (neww != null) {
			neww.onBlockEnter(member, to);
		}
	}
	public static void onBlockUpdate(final Block block) {
		final BlockAction ba = get(block);
		if (ba != null) {
			final boolean powered = block.isBlockIndirectlyPowered();
			if (powered) {
				if (!ba.poweredBlocks.add(block)) {
					return;
				}
			} else {
				if (!ba.poweredBlocks.remove(block)) {
					return;
				}
			}
			new Task(TrainCarts.plugin) {
				public void run() {
					ba.onRedstoneChange(block, powered);
				}
			}.start();
		}
	}
		
	public abstract boolean onBuild(Player player, Block block) throws NoPermissionException;
	public abstract Material getDefaultMaterial();
	public abstract String getName();
	public void load(ConfigurationNode node) {
		this.invertPow = node.get("powerInverted", this.invertPow);
	}
	
	public boolean isPowerInverted() {
		return this.invertPow;
	}
	public Material getMaterial() {
		return this.material;
	}
	
	private boolean invertPow = false;
	private Material material = null;
	private final BlockSet poweredBlocks = new BlockSet();
	private final Set<MinecartGroup> activeGroups = new HashSet<MinecartGroup>();
	private final Set<MinecartMember> activeMembers = new HashSet<MinecartMember>();
	private void onBlockLeave(MinecartMember member, Block block) {
		if (activeMembers.remove(member)) {
			this.onMemberLeave(member, block);
			MinecartGroup group = member.getGroup();
			if (group.tail() == member && activeGroups.remove(group)) {
				this.onGroupLeave(member.getGroup(), block);
				this.cleanUp();
			}
		}
	}
	private void onBlockEnter(MinecartMember member, Block block) {
		if (activeMembers.add(member)) {
			this.onMemberEnter(member, block);
			MinecartGroup group = member.getGroup();
			if (activeGroups.add(group)) {
				this.onGroupEnter(group, block);
			}
		}
	}
	private void cleanUp() {
		Iterator<MinecartMember> members = activeMembers.iterator();
		while (members.hasNext()) {
			if (members.next().dead) {
				members.remove();
			}
		}
		Iterator<MinecartGroup> groups = activeGroups.iterator();
		while (groups.hasNext()) {
			if (!groups.next().isValid()) {
				groups.remove();
			}
		}
	}
	
	public void onMemberEnter(MinecartMember member, Block block) {}
	public void onMemberLeave(MinecartMember member, Block block) {}
	public void onGroupEnter(MinecartGroup group, Block block) {}
	public void onGroupLeave(MinecartGroup group, Block block) {}
	public void onRedstoneChange(Block block, boolean powered) {}
	
}
