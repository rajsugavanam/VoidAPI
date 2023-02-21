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

package com.theswirlingvoid.void_api.multipart.prebuilt;

import com.mojang.logging.LogUtils;
import com.theswirlingvoid.void_api.block.ExperimentalMultipart;
import com.theswirlingvoid.void_api.mixin.StructureTemplateAccessor;
import com.theswirlingvoid.void_api.multipart.change_detection.ChangeListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.List;

public class MultiblockCore implements ChangeListener {
	PrebuiltMultiblockTemplate multiblockTemplate;
	BlockPos observingOrigin;
	Block block;
	List<BlockPos> observingPositions = new ArrayList<>();
	MinecraftServer server;

	public MultiblockCore(PrebuiltMultiblockTemplate multiblockTemplate, Block block, BlockPos observingOrigin, MinecraftServer server) {
		this.multiblockTemplate = multiblockTemplate;
		this.observingOrigin = observingOrigin;
		this.block = block;
		this.server = server;
	}

	private void createObservingPositions() {
		StructureTemplate.Palette palette =
				((StructureTemplateAccessor) multiblockTemplate.getTemplate(server)).getPalettes().get(0);

		palette.blocks().forEach((sbi) -> {
			// why do i have to do this subtraction magic? god knows!
			observingPositions.add(observingOrigin.subtract(sbi.pos.multiply(-1)));
		});
	}

	@Override
	public void onBlockChange(BlockPos pos, LevelChunk chunk, BlockState state, BlockState newstate) {
		// experimental code
		if (chunk.getStatus() == ChunkStatus.FULL && state.getBlock() == block) {
			if (newstate.getBlock() == block) { // PLACED
				LogUtils.getLogger().info("onBlockChange(); PLACED");
			} else { 							// BROKEN
				LogUtils.getLogger().info("onBlockChange(); BROKEN");
			}
		}
//		BlockPos affected = PrebuiltMultiblockTemplate.withTransformations(
//				pos,
//				new BlockPos(2,2,3),
//				Mirror.NONE,
//				Rotation.CLOCKWISE_90
//		);
//
//		LogUtils.getLogger().info(affected.toString());
	}
}
