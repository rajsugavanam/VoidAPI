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
import com.theswirlingvoid.void_api.multipart.change_detection.ChangeFunctions;
import com.theswirlingvoid.void_api.multipart.change_detection.ChangeListener;
import com.theswirlingvoid.void_api.multipart.change_detection.ChangeListenerList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

public class MultiblockCoreSavedData extends SavedData {

	private static final String DATA_FILE_NAME = "multiblock_cores";
	private final List<MultiblockCore> cores = new ArrayList<>();

	private MultiblockCoreSavedData() {}

	public static MultiblockCoreSavedData get() {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server != null) {
				DimensionDataStorage storage = server.overworld().getDataStorage();
				return storage.computeIfAbsent(
						MultiblockCoreSavedData::load, // if data DOES exist
						MultiblockCoreSavedData::new, // if data doesn't exist
						DATA_FILE_NAME
				);
			}
		}
		return new MultiblockCoreSavedData();
	}

	private void loadCore(MultiblockCore core, boolean dirty) {
		cores.add(core);
		core.changeListen();
		if (dirty) {
			setDirty();
		}
	}

	public List<MultiblockCore> getCores() {
		return cores;
	}

	public void addCore(MultiblockCore core) {
		loadCore(core, true);
	}

	public void removeCore(MultiblockCore core) {
		cores.remove(core);
		core.stopChangeListen();
		setDirty();
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
//		update(false); // if updating made this dirty we would be stuck in an infinite loop
		ListTag coreTag = new ListTag();
		for (MultiblockCore core : cores) {
			coreTag.add(core.getSaveData());
		}

		CompoundTag masterTag = new CompoundTag();
		masterTag.put("coreData", coreTag);
		return masterTag;
	}

	public static MultiblockCoreSavedData load(CompoundTag compoundTag) {
		MultiblockCoreSavedData data = new MultiblockCoreSavedData();
		ListTag listTag = compoundTag.getList("coreData", Tag.TAG_COMPOUND);
		for (Tag tag : listTag) {
			CompoundTag cTag = (CompoundTag) tag;
			data.loadCore(MultiblockCore.readSaveData(cTag), false);
		}
		return data;
	}

	public void modifyBlockIfCore(Level level, BlockPos pos, BlockState state, BlockState newstate) {
		CoreTemplates.getCoreTemplates().forEach((template) -> {

			ChangeFunctions funcs = new ChangeFunctions(template.getMasterBlock(), state, newstate);

			MultiblockCore potentialCore = new MultiblockCore(
					pos,
					level.dimension(),
					template
			);

			if (funcs.involvedBlockPlaced()) {
				this.addCore(potentialCore);
				potentialCore.onPlaced();
			} else if (funcs.involvedBlockBroken()) {
				this.removeCore(potentialCore);
				potentialCore.onBroken();
			}
		});
	}
}
