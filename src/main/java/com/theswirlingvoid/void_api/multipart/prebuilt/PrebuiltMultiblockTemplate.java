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
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public class PrebuiltMultiblockTemplate {

	private final ResourceLocation resLoc;
	private final BlockPos multiblockMaster;
	private StructureTemplate template;

	public PrebuiltMultiblockTemplate(ResourceLocation resLoc, BlockPos multiblockMaster) {
		this.resLoc = resLoc;
		this.multiblockMaster = multiblockMaster;
	}

	public ResourceLocation getTemplateLocation() {
		return resLoc;
	}

	public StructureTemplate getTemplate(MinecraftServer server) {
		if (template == null) {
			Optional<StructureTemplate> templateOpt = TemplateManager.getTemplate(resLoc, server);
			if (templateOpt.isPresent()) {
				template = templateOpt.get();
				return template;
			} else {
				LogUtils.getLogger().error("Multiblock template at "+resLoc.toString()+"not found.");
				throw new NullPointerException();
			}
		} else {
			return template;
		}
	}

	public BlockPos getSize() {
		if (template != null) {
			return new BlockPos(template.getSize());
		} else {
			return null;
		}
	}

	public static BlockPos withTransformations(BlockPos origin, BlockPos relative, Mirror mirror, Rotation rot) {
		StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rot).setMirror(mirror);
		return origin.offset(StructureTemplate.calculateRelativePosition(settings, relative));
	}
}
