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

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Type;

public class MultiblockCoreAdapter implements JsonSerializer<MultiblockCore>, JsonDeserializer<MultiblockCore> {

	@Override
	public JsonElement serialize(MultiblockCore src, Type typeOfSrc, JsonSerializationContext context) {

		DataResult<JsonElement> levelResult = Level.RESOURCE_KEY_CODEC.encodeStart(JsonOps.COMPRESSED, src.getLevelName());
		DataResult<JsonElement> posResult = BlockPos.CODEC.encodeStart(JsonOps.COMPRESSED, src.getCorePos());
		DataResult<JsonElement> blockResult = ForgeRegistries.BLOCKS.getCodec().encodeStart(JsonOps.COMPRESSED, src.getCoreBlock());

		JsonObject root = new JsonObject();
		root.add("levelName", levelResult.getOrThrow(false, (s) -> {}));
		root.add("corePos", posResult.getOrThrow(false, (s) -> {}));
		root.add("coreBlock", blockResult.getOrThrow(false, (s) -> {}));

		return root;
	}

	@Override
	public MultiblockCore deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		Gson gson = new Gson();
		JsonObject obj = json.getAsJsonObject();

		DataResult<ResourceKey<Level>> levelResult = Level.RESOURCE_KEY_CODEC.parse(JsonOps.COMPRESSED, obj.get("levelName"));
		DataResult<BlockPos> posResult = BlockPos.CODEC.parse(JsonOps.COMPRESSED, obj.get("corePos"));
		DataResult<Block> blockResult = ForgeRegistries.BLOCKS.getCodec().parse(JsonOps.COMPRESSED, obj.get("coreBlock"));

		return new MultiblockCore(
			blockResult.getOrThrow(false, (s) -> {}),
			levelResult.getOrThrow(false, (s) -> {}),
			posResult.getOrThrow(false, (s) -> {})
		);
	}
}
