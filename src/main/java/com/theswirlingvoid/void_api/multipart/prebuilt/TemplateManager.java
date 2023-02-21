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

import com.theswirlingvoid.void_api.mixin.StructureTemplateAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public class TemplateManager {

	private static Optional<Resource> getModResource(ResourceLocation resLoc, MinecraftServer server) {
		return server.getResourceManager().getResource(resLoc);
	}

	public static Optional<StructureTemplate> getTemplate(ResourceLocation resLoc, MinecraftServer server) {
		Optional<Resource> res = getModResource(resLoc, server);
		if (res.isPresent()) {
			try {
				InputStream openedResource = res.get().open();
				CompoundTag tag = NbtIo.readCompressed(openedResource);
				StructureTemplate template = new StructureTemplate();
				template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
				return Optional.of(template);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return Optional.empty();
		}
	}

	private static List<StructureTemplate.Palette> getTemplatePalettes(StructureTemplate template) {
		return ((StructureTemplateAccessor) template).getPalettes();
	}

	public static List<StructureTemplate.StructureBlockInfo> getStructureBlocksFromTemplate(StructureTemplate template) {
		return getTemplatePalettes(template).get(0).blocks();
	}

}
