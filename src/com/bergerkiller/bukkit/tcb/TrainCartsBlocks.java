package com.bergerkiller.bukkit.tcb;

import org.bukkit.command.CommandSender;

import com.bergerkiller.bukkit.common.PluginBase;
import com.bergerkiller.bukkit.common.config.FileConfiguration;
import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tcb.blocks.BlockAction;

public class TrainCartsBlocks extends PluginBase {
	@Override
	public void permissions() {
	}

	@Override
	public void enable() {
		this.register(TCBListener.class);
		
		FileConfiguration config = new FileConfiguration(TrainCarts.plugin, "blocks.yml");
		config.load();
		config.setHeader("This is the configuration of the Blocks add-on of TrainCarts.");
		config.addHeader("In here you can configure all blocks available");
		config.setHeader("blocks", "\nConfiguration for individual action blocks can be set below");
		BlockAction.init(config.getNode("blocks"));
		config.save();
	}

	@Override
	public int getMinimumLibVersion() {
		return 1;
	}

	@Override
	public void disable() {
		BlockAction.deinit();
	}

	@Override
	public boolean command(CommandSender sender, String command, String[] args) {
		return false;
	}

}
