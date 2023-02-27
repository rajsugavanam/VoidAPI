/*
 * This file is part of VoidAPI.
 * Copyright (C) 2023, TheSwirlingVoid. All rights reserved.
 *
 * This project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.theswirlingvoid.void_api.multipart.change_detection;

import com.ibm.icu.impl.Pair;
import com.mojang.logging.LogUtils;
import com.theswirlingvoid.void_api.multipart.prebuilt.CoreTemplates;
import com.theswirlingvoid.void_api.multipart.prebuilt.MultiblockCore;
import com.theswirlingvoid.void_api.multipart.prebuilt.PrebuiltMultiblockTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

public class CoreRegister {

	private final MinecraftServer server;

	public CoreRegister(MinecraftServer server) {
		this.server = server;
	}

	public void addBlockIfCore(BlockPos pos, BlockState state, BlockState newstate) {
		for (Pair<RegistryObject<Block>, PrebuiltMultiblockTemplate> pair : CoreTemplates.getCoreTemplates()) {

			ChangeFunctions funcs = new ChangeFunctions(pair.first.get(), state, newstate);

			MultiblockCore core = new MultiblockCore(
					pair.second,
					funcs.getBlock(),
					pos,
					server
			);

			if (funcs.blockBroken() || funcs.blockPlaced()) {
				ChangeListenerList.getListeners().forEach((l) -> { LogUtils.getLogger().info(l.toString()); });
			}

			if (funcs.blockPlaced()) {
				ChangeListenerList.scheduleAddListener(core);
				LogUtils.getLogger().info("Added listener from placed block at " +pos+" with template "+pair.second);
			} else if (funcs.blockBroken()) {
				ChangeListenerList.scheduleRemoveListenerOfType(MultiblockCore.class, core);
				LogUtils.getLogger().info("Removed listener from broken block at " +pos+" with template "+pair.second);
			}
		}
	}
}
