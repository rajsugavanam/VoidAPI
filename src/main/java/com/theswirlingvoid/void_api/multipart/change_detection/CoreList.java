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

import com.theswirlingvoid.void_api.multipart.prebuilt.CoreTemplates;
import com.theswirlingvoid.void_api.multipart.prebuilt.MultiblockCore;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CoreList {

	private final MinecraftServer server;

	public CoreList(MinecraftServer server) {
		this.server = server;
	}

	public void modifyBlockIfCore(Level level, BlockPos pos, BlockState state, BlockState newstate) {
		CoreTemplates.getCoreTemplates().forEach((template) -> {

			ChangeFunctions funcs = new ChangeFunctions(template.getMasterBlock(), state, newstate);

			MultiblockCore potentialCore = new MultiblockCore(
					pos,
					level.dimension(),
					template
			);

			if (funcs.involvedBlockPlaced()) {
				ChangeListenerList.INSTANCE.scheduleAddListener(potentialCore);
				potentialCore.onPlaced();
			} else if (funcs.involvedBlockBroken()) {
				ChangeListenerList.INSTANCE.scheduleRemoveListener(potentialCore);
				potentialCore.onBroken();
			}
		});
	}
}
