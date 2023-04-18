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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import com.theswirlingvoid.void_api.multipart.prebuilt.MultiblockCore;
import com.theswirlingvoid.void_api.multipart.prebuilt.MultiblockCoreAdapter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ChangeListenerList extends SavedData {

	public static ChangeListenerList INSTANCE = new ChangeListenerList();

	public List<ChangeListener> listeners = new ArrayList<>();
	public List<ChangeListener> listenersToRemove = new ArrayList<>();
	public List<ChangeListener> listenersToAdd = new ArrayList<>();

	private static final String DATA_FILE_NAME = "changelistenerlist";
	private static final String LISTENER_SAVE_NAME = "listeners";
	private static final String TOADD_SAVE_NAME = "listenersToAdd";
	private static final String TOREMOVE_SAVE_NAME = "listenersToRemove";

	private ChangeListenerList() {} // no construction for u!

	/**
	 * Load the listener data into the <code>INSTANCE</code> field.
	 * @param server The server where the the list data is held
	 */
	public void loadListenerList(MinecraftServer server) {
		if (!server.overworld().isClientSide) {
			DimensionDataStorage storage = server.overworld().getDataStorage();
			storage.computeIfAbsent(
					this::load, // if data DOES exist
					this::create, // if data doesn't exist
					DATA_FILE_NAME
			);
		} else {
			throw new RuntimeException("An error has occured. Do not run ChangeListenerList::getListenerList() from the client.");
		}
	}

	public ChangeListenerList create() {
		return this;
	}

	public ChangeListenerList load(CompoundTag savedTag) {

		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapterFactory(new ChangeListenerAdapterFactory(MultiblockCore.class));
		gson.registerTypeAdapter(MultiblockCore.class, new MultiblockCoreAdapter());
		Gson parser = gson.create();

		listeners = parser.fromJson(savedTag.getString(LISTENER_SAVE_NAME), new TypeToken<List<ChangeListener>>() {});
		listenersToAdd = parser.fromJson(savedTag.getString(TOADD_SAVE_NAME), new TypeToken<List<ChangeListener>>() {});
		listenersToRemove = parser.fromJson(savedTag.getString(TOREMOVE_SAVE_NAME), new TypeToken<List<ChangeListener>>() {});

		LogUtils.getLogger().info("Successfully loaded ChangeListenerList data.");
		return this;
	}

	/**
	 * Schedules a listener to be added. It will be added to the list when <code>update()</code> is called.
	 * @param listener The listener object to add
	 */
	public void scheduleAddListener(ChangeListener listener) {
		listenersToAdd.add(listener);
		LogUtils.getLogger().info("Added multib. listener "+listener);
	}
	public <T extends ChangeListener> void scheduleRemoveListenerOfType(Class<T> c, T listener) {
		for (int i = 0; i < listeners.size(); i++) {
			ChangeListener currentListener = listeners.get(i);
			if (currentListener.getClass().equals(c)) {
				if (currentListener.equals(listener)) {
					listenersToRemove.add(currentListener);
					LogUtils.getLogger().info("Removed multib. listener "+currentListener);
					return;
				}
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

		this.setDirty();
	}

	/**
	 * Remove every listener that's stored.
	 */
	public void clearListeners() {
		listeners.clear();
	}

	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag) {

		GsonBuilder gson = new GsonBuilder();
//		gson.registerTypeAdapter(MultiblockCore.class, new MultiblockCoreAdapter());
		gson.registerTypeAdapterFactory(new ChangeListenerAdapterFactory(MultiblockCore.class));
		gson.registerTypeAdapter(MultiblockCore.class, new MultiblockCoreAdapter());
		Gson parser = gson.create();

		String listenersJson = parser.toJson(listeners);
		String toAddJson = parser.toJson(listenersToAdd);
		String toRemoveJson = parser.toJson(listenersToRemove);

		tag.putString(LISTENER_SAVE_NAME, listenersJson);
		tag.putString(TOADD_SAVE_NAME, toAddJson);
		tag.putString(TOREMOVE_SAVE_NAME, toRemoveJson);

		LogUtils.getLogger().info(listenersJson);


		LogUtils.getLogger().info("Successfully saved ChangeListenerList data.");
		return tag;
	}
}
