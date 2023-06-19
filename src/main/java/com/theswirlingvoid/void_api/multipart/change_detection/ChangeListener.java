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

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public abstract class ChangeListener {

	private final BlockPos listenerPos;
	private final ResourceKey<Level> dimension;
	private final BlockPos areaCorner1;
	private final BlockPos areaCorner2;

	protected ChangeListener(BlockPos center, ResourceKey<Level> dimension, BlockPos pos1, BlockPos pos2) {
		this.listenerPos = center;
		this.dimension = dimension;
		this.areaCorner1 = pos1;
		this.areaCorner2 = pos2;
	}

	public void addAsListener() {
		ChangeListenerList.INSTANCE.scheduleAddListener(this);
	}

	public void removeAsListener() {
		ChangeListenerList.INSTANCE.scheduleRemoveListener(this);
	}

	public BlockPos getListenerPos() {
		return listenerPos;
	}

	public ResourceKey<Level> getDimension() {
		return dimension;
	}

	public BlockPos getAreaCorner1() {
		return areaCorner1;
	}

	public BlockPos getAreaCorner2() {
		return areaCorner2;
	}

	public abstract void onBlockChange(BlockPos pos, LevelChunk chunk, BlockState state, BlockState newstate);

	public void changeListen() {
		ChangeListenerList.INSTANCE.scheduleAddListener(this);
	}

	public void stopChangeListen() {
		ChangeListenerList.INSTANCE.scheduleRemoveListener(this);
	}

	public boolean equals(ChangeListener l2) {
		if (l2.listenerPos.equals(this.listenerPos) && l2.dimension.equals(this.dimension)) {
			return true;
		}
		return false;
	}
}
