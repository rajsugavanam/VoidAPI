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

package com.theswirlingvoid.void_api.mixin;

import com.theswirlingvoid.void_api.multipart.change_detection.ChangeListenerHandler;
import com.theswirlingvoid.void_api.multipart.change_detection.ChangeListenerList;
import com.theswirlingvoid.void_api.multipart.prebuilt.MultiblockCore;
import com.theswirlingvoid.void_api.multipart.prebuilt.MultiblockCoreSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Level.class)
public abstract class LevelNotifyBlockMixin {

	@Shadow @Final public boolean isClientSide;

	@Shadow @Nullable public abstract MinecraftServer getServer();

	@Inject(method = "markAndNotifyBlock", at = @At("TAIL"), remap = false)
	private void markAndNotifyBlock(BlockPos pos, LevelChunk chunk,
									BlockState state, BlockState newstate,
									int unknown1, int unknown2, CallbackInfo info) {

		// this refers to the level/world
		if (!this.isClientSide && this.getServer() != null && chunk.getStatus() == ChunkStatus.FULL) {

			if (MultiblockCore.isValidCoreBlock(newstate.getBlock()) || MultiblockCore.isValidCoreBlock(state.getBlock())) {
				MultiblockCoreSavedData.get().modifyBlockIfCore(chunk.getLevel(), pos, state, newstate);
			}

			ChangeListenerList.INSTANCE.update();
			ChangeListenerHandler.onBlockChange(pos, chunk, state, newstate);
		}
	}
}
