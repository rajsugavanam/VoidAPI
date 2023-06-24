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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class PrebuiltMultiblockTemplate {

	private static final Set<Block> knownTemplateBlocks = new HashSet<>();
	private final ResourceLocation resLoc;
	private final BlockPos masterPos;
	private final Block masterBlock;
	private StructureTemplate template;
	private boolean addable = false;

	public PrebuiltMultiblockTemplate(ResourceLocation resLoc, BlockPos masterPos, Block masterBlock) {
		this.resLoc = resLoc;
		this.masterPos = masterPos;
		if (!knownTemplateBlocks.contains(masterBlock)) {
			this.masterBlock = masterBlock;
			knownTemplateBlocks.add(masterBlock);
		} else {
			throw new RuntimeException();
		}
	}

	public boolean isAddable() {
		return addable;
	}

	public ResourceLocation getTemplateLocation() {
		return resLoc;
	}

	public PrebuiltMultiblockTemplate withServerTemplate(MinecraftServer server) {
		if (template == null) {
			Optional<StructureTemplate> templateOpt = TemplateManager.getTemplate(resLoc, server);
			if (templateOpt.isPresent()) {
				template = templateOpt.get();
				this.addable = true;
			} else {
				LogUtils.getLogger().error("Multiblock template at "+resLoc.toString()+"not found.");
				throw new NullPointerException();
			}
		}
		return this;
	}

	public StructureTemplate getStructureTemplate() {
		return template;
	}

	public Block getMasterBlock() {
		return masterBlock;
	}

	public BlockPos getSize() {
		if (template != null) {
			return new BlockPos(template.getSize());
		} else {
			return null;
		}
	}

	public ResourceLocation getResLoc() {
		return resLoc;
	}

	public BlockPos getCenterPos() {
		return masterPos;
	}

	public BlockPos getCorner1CenterOffset() {
		return masterPos.multiply(-1);
	}

	public BlockPos getCorner2CenterOffset() {
		return new BlockPos(template.getSize().subtract(masterPos));
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		} else if (o == null || o.getClass() != getClass()) {
			return false;
		} else {
			PrebuiltMultiblockTemplate pmt = (PrebuiltMultiblockTemplate) o;
			// prebuilt templates should only have one block assigned.
			// A Set of templates can only contain one template for each block
			if (masterBlock.equals(pmt.masterBlock)) {
				return true;
			} else {
				return false;
			}
		}

	}
}
