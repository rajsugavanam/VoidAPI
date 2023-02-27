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

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ChangeFunctions {
	private final BlockState oldState;
	private final BlockState newState;
	private final Block block;

	private final boolean blockWasThere;
	private final boolean blockIsThere;
	private final boolean involvedBlock;

	public ChangeFunctions(Block block, BlockState oldState, BlockState newState) {
		this.oldState = oldState;
		this.newState = newState;
		this.block = block;

//		blockIsThere = (newState.getBlock() == block);
//		blockWasThere = (oldState.getBlock() == block);

		blockIsThere = (newState.getBlock() != Blocks.AIR);
		blockWasThere = (oldState.getBlock() != Blocks.AIR);
		involvedBlock = (newState.getBlock() == block || oldState.getBlock() == block);
	}

	public boolean blockBroken() {
		return (blockWasThere && !blockIsThere && involvedBlock);
	}

	public boolean blockPlaced() {
		return (!blockWasThere && blockIsThere && involvedBlock);
	}

	public BlockState getOldState() {
		return oldState;
	}

	public BlockState getNewState() {
		return newState;
	}

	public Block getBlock() {
		return block;
	}
}
