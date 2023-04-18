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

import com.google.gson.annotations.Expose;
import com.ibm.icu.impl.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.theswirlingvoid.void_api.mixin.StructureTemplateAccessor;
import com.theswirlingvoid.void_api.multipart.change_detection.ChangeFunctions;
import com.theswirlingvoid.void_api.multipart.change_detection.ChangeListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class MultiblockCore implements ChangeListener {
	private final ResourceKey<Level> dimension;
	private final BlockPos corePos;
	private final Block coreBlock;

	public MultiblockCore(Block coreBlock, ResourceKey<Level> dimension, BlockPos corePos) {
		this.coreBlock = coreBlock;
		this.dimension = dimension;
		this.corePos = corePos;

//		this.observingPositions = this.createObservingPositions();
	}

	public ResourceKey<Level> getLevelName() {
		return dimension;
	}

	public BlockPos getCorePos() {
		return corePos;
	}

	public Block getCoreBlock() {
		return coreBlock;
	}

	private PrebuiltMultiblockTemplate getBlockTemplate() {
		return CoreTemplates.getCoreTemplates().getOrDefault(coreBlock, null);
	}

	public List<BlockPos> getAbsoluteObservingPositions(MinecraftServer server) {

		List<BlockPos> positions = new ArrayList<>();

		PrebuiltMultiblockTemplate multiblockTemplate = getBlockTemplate();

		StructureTemplate.Palette palette =
				((StructureTemplateAccessor) multiblockTemplate.getTemplate(server)).getPalettes().get(0);

		palette.blocks().forEach((structBlockInfo) -> {
			// this just adds them. `structBlockInfo.pos` is a RELATIVE position!
			positions.add(corePos.subtract(structBlockInfo.pos.multiply(-1)));
		});

		return positions;
	}

	@Override
	public void onBlockChange(BlockPos pos, LevelChunk chunk, BlockState state, BlockState newstate) {
		// experimental code
		// idk wtf chunkstatus full means lmao
//		if (chunk.getLevel().dimension() == dimension) {
//			if (chunk.getStatus() == ChunkStatus.FULL) {
//				if (pos == corePos) {
//					ChangeFunctions funcs = new ChangeFunctions(state.getBlock(), state, newstate);
//
//					if (funcs.involvedBlockPlaced()) {
//						LogUtils.getLogger().info("onBlockChange(); PLACED");
//					} else if (funcs.involvedBlockBroken()) {
//						LogUtils.getLogger().info("onBlockChange(); BROKEN");
//					}
//				}
//			}
//		}
//		BlockPos affected = PrebuiltMultiblockTemplate.withTransformations(
//				pos,
//				new BlockPos(2,2,3),
//				Mirror.NONE,
//				Rotation.CLOCKWISE_90
//		);
//
//		LogUtils.getLogger().info(affected.toString());
	}

	public void onPlaced() {
		LogUtils.getLogger().info("onBlockChange(); PLACED");
	}

	public void onBroken() {
		LogUtils.getLogger().info("onBlockChange(); BROKEN");
	}

	@Override
	public boolean equals(ChangeListener l2) {
		if (l2 instanceof MultiblockCore core2) {
			if ((core2.coreBlock.equals(this.coreBlock)) &&
				(core2.corePos.equals(this.corePos)) &&
				(core2.dimension.equals(this.dimension)))
			{
				return true;
			}
		}
		return false;
	}

//	public Codec<MultiblockCore> getCodec() {
//		return RecordCodecBuilder.create((instance) -> instance.group(
//				ForgeRegistries.BLOCKS.getCodec().fieldOf("coreBlock").forGetter(MultiblockCore::getCoreBlock),
//				BlockPos.CODEC.fieldOf("corePos").forGetter(MultiblockCore::getCorePos),
//				Level.RESOURCE_KEY_CODEC.fieldOf("levelName").forGetter(MultiblockCore::getLevelName)
//		).apply(instance, (b, p, l) -> new MultiblockCore(b, l, p)));
//	}
}
