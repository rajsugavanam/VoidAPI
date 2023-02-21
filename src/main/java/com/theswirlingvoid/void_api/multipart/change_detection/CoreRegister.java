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
import com.theswirlingvoid.void_api.multipart.prebuilt.MultiblockCore;
import com.theswirlingvoid.void_api.multipart.prebuilt.PrebuiltMultiblockTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class CoreRegister implements ChangeListener {

	public static List<Pair<RegistryObject<Block>, PrebuiltMultiblockTemplate>> coreTemplates;
	private final MinecraftServer server;

	public CoreRegister(MinecraftServer server) {
		this.server = server;
	}

	@Override
	public void onBlockChange(BlockPos pos, LevelChunk chunk, BlockState state, BlockState newstate) {
		for (Pair<RegistryObject<Block>, PrebuiltMultiblockTemplate> pair :coreTemplates) {
			if (state.getBlock() == pair.first.get()) {

				ChangeListenerList.addListener(new MultiblockCore(
						pair.second,
						state.getBlock(),
						pos,
						server
				));

			}
		}
	}

	public static Pair<RegistryObject<Block>, PrebuiltMultiblockTemplate> addCoreBlock(RegistryObject<Block> blockRegistry, PrebuiltMultiblockTemplate template) {
		Pair<RegistryObject<Block>, PrebuiltMultiblockTemplate> p = Pair.of(blockRegistry, template);
		coreTemplates.add(p);
		return p;
	}
}
