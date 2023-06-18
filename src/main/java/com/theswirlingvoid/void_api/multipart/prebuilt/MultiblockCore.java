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
import com.mojang.serialization.JsonOps;
import com.theswirlingvoid.void_api.mixin.StructureTemplateAccessor;
import com.theswirlingvoid.void_api.multipart.change_detection.ChangeListener;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class MultiblockCore extends ChangeListener {
//	private final ResourceKey<Level> dimension;
//	private final BlockPos corePos;
	private final PrebuiltMultiblockTemplate template;

	public MultiblockCore(BlockPos corePos, ResourceKey<Level> dimension, PrebuiltMultiblockTemplate template) {
		super(corePos,
			dimension,
			corePos.subtract(template.getCorner1CenterOffset().multiply(-1)),
			corePos.subtract(template.getCorner2CenterOffset().multiply(-1))
			// corePos + corner 1 or 2 offset
		);
		this.template = template;

	}

	public MultiblockCore(BlockPos corePos, ResourceKey<Level> dimension, Block block) {
		this(corePos, dimension, CoreTemplates.getFromBlock(block));
	}

	public Block getCoreBlock() {
		return template.getMasterBlock();
	}

//	private PrebuiltMultiblockTemplate getBlockTemplate() {
//		return CoreTemplates.getCoreTemplates().getOrDefault(coreBlock, null);
//	}

	public List<BlockPos> getAbsoluteObservingPositions(MinecraftServer server) {

		List<BlockPos> positions = new ArrayList<>();

		StructureTemplate.Palette palette =
				((StructureTemplateAccessor) template.addServerTemplate(server)).getPalettes().get(0);

		palette.blocks().forEach((structBlockInfo) -> {
			// this just adds them. `structBlockInfo.pos` is a RELATIVE position!
			positions.add(getListenerPos().subtract(structBlockInfo.pos.multiply(-1)));
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

	public CompoundTag getSaveData() {

		CompoundTag saveData = new CompoundTag();

		Tag lvlTag = Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, super.getDimension())
						.getOrThrow(false, (e) -> {});

		Tag blockTag = ForgeRegistries.BLOCKS.getCodec().encodeStart(NbtOps.INSTANCE, getCoreBlock())
						.getOrThrow(false, (e) -> {});

		saveData.putLong("corePos", getListenerPos().asLong());
		saveData.put("dimension", lvlTag);
		saveData.put("coreBlock", blockTag);

		return saveData;
	}

	public static MultiblockCore readSaveData(CompoundTag tag) {

		CompoundTag saveData = new CompoundTag();

		ResourceKey<Level> dimension = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, tag.get("dimension"))
				.getOrThrow(false, (e) -> {});

		Block block = ForgeRegistries.BLOCKS.getCodec().parse(NbtOps.INSTANCE, tag.get("coreBlock"))
				.getOrThrow(false, (e) -> {});

		return new MultiblockCore(
				BlockPos.of(tag.getLong("corePos")),
				dimension,
				block
		);
	}

	public static boolean isValidCoreBlock(Block block) {
		for (PrebuiltMultiblockTemplate template : CoreTemplates.getCoreTemplates()) {
			if (template.getMasterBlock() == block) {
				return true;
			}
		}
		return false;
	}

	public void onPlaced() {
		LogUtils.getLogger().info("onBlockChange(); PLACED");
	}

	public void onBroken() {
		LogUtils.getLogger().info("onBlockChange(); BROKEN");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o == null || getClass() != o.getClass()) {
			return false;
		} else {
			MultiblockCore core = (MultiblockCore) o;
			if ((core.getCoreBlock().equals(this.getCoreBlock())) &&
					(core.getListenerPos().equals(this.getListenerPos())) &&
					(core.getDimension().equals(this.getDimension())))
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
