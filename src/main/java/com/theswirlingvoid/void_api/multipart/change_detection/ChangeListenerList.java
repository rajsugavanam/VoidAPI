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

import com.mojang.logging.LogUtils;
import com.theswirlingvoid.void_api.multipart.prebuilt.MultiblockCoreSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

public class ChangeListenerList {

	public static ChangeListenerList INSTANCE = new ChangeListenerList();

	private Set<ChangeListener> listeners = new HashSet<>();
	private Set<ChangeListener> listenersToRemove = new HashSet<>();
	private Set<ChangeListener> listenersToAdd = new HashSet<>();

//	private static final String DATA_FILE_NAME = "changelistenerlist";
//	private static final String LISTENER_SAVE_NAME = "listeners";
//	private static final String TOADD_SAVE_NAME = "listenersToAdd";
//	private static final String TOREMOVE_SAVE_NAME = "listenersToRemove";

	private ChangeListenerList() {} // no construction for u!

//	@Override
//	public CompoundTag save(CompoundTag tag) {
//		update(false); // if updating made this dirty we would be stuck in an infinite loop
//		CompoundTag listenerTag = new CompoundTag();
//		int idx = 0;
//		for (ChangeListener listener : listeners) {
//			listenerTag.put(String.valueOf(idx), listener.getSaveData());
//			idx++;
//		}
//
//		CompoundTag masterTag = new CompoundTag();
//		masterTag.put("listenerData", listenerTag);
//		return masterTag;
//	}

	public Set<ChangeListener> getListeners() {
		return listeners;
	}

	public void setListeners(Set<ChangeListener> listeners) {
		this.listeners = listeners;
	}

	/**
	 * Load the listener data into the <code>INSTANCE</code> field.
	 * @param server The server where the the list data is held
	 */
//	public void loadListenerList(MinecraftServer server) {
//		if (!server.overworld().isClientSide) {
//			DimensionDataStorage storage = server.overworld().getDataStorage();
//			storage.computeIfAbsent(
//					this::load, // if data DOES exist
//					this::create, // if data doesn't exist
//					DATA_FILE_NAME
//			);
//		} else {
//			throw new RuntimeException("An error has occured. Do not run ChangeListenerList::getListenerList from the client.");
//		}
//	}
//
//	public ChangeListenerList create() {
//		return INSTANCE;
//	}
//
//	public ChangeListenerList load(CompoundTag savedTag) {
//		CompoundTag listenerTag = savedTag.getCompound("listenerData");
//		for (String key : listenerTag.getAllKeys()) {
//			CompoundTag subTag = listenerTag.getCompound(key);
//		}
//	}

	/**
	 * Schedules a listener to be added. It will be added to the list when <code>update()</code> is called.
	 * @param listener The listener object to add
	 */
	public void scheduleAddListener(ChangeListener listener) {
		listenersToAdd.add(listener);
		LogUtils.getLogger().info("Added listener "+listener);
	}
	public void scheduleRemoveListener(ChangeListener listener) {
		for (Iterator<ChangeListener> i = listeners.iterator(); i.hasNext();) {
			ChangeListener currentListener = i.next();
			if (currentListener.equals(listener)) {
				listenersToRemove.add(currentListener);
				LogUtils.getLogger().info("Removed listener "+currentListener);
				return;
			}
		}
	}

	/**
	 * Adds and removes the scheduled listeners.
	 */
	public void update() {
		listeners.removeAll(listenersToRemove);
		listeners.addAll(listenersToAdd);
		listenersToRemove.clear();
		listenersToAdd.clear();
	}

	/**
	 * Remove every listener that's stored.
	 */
	public void clearListeners() {
		listeners.clear();
	}

//	@Override
//	public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
//
//		GsonBuilder gson = new GsonBuilder();
////		gson.registerTypeAdapter(MultiblockCore.class, new MultiblockCoreAdapter());
////		gson.registerTypeAdapterFactory(new ChangeListenerAdapterFactory(MultiblockCore.class));
////		gson.registerTypeAdapter(MultiblockCore.class, new MultiblockCoreAdapter());
//		gson.registerTypeAdapterFactory(ChangeListener.class, new ChangeListenerAdapter());
//		Gson parser = gson.create();
//
//		String listenersJson = parser.toJson(listeners);
//		String toAddJson = parser.toJson(listenersToAdd);
//		String toRemoveJson = parser.toJson(listenersToRemove);
//
//		tag.putString(LISTENER_SAVE_NAME, listenersJson);
//		tag.putString(TOADD_SAVE_NAME, toAddJson);
//		tag.putString(TOREMOVE_SAVE_NAME, toRemoveJson);
//
//		LogUtils.getLogger().info(listenersJson);
//
//
//		LogUtils.getLogger().info("Successfully saved ChangeListenerList data.");
//		return tag;
//	}
}
