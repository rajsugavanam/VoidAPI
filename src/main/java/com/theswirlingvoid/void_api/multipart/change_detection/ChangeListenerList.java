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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

public class ChangeListenerList {

	private static List<ChangeListener> listeners = new ArrayList<>();
	private static List<ChangeListener> listenersToRemove = new ArrayList<>();
	private static List<ChangeListener> listenersToAdd = new ArrayList<>();

	public static void onBlockChange(BlockPos pos, LevelChunk chunk, BlockState state, BlockState newstate) {

		listeners.removeAll(listenersToRemove);
		listeners.addAll(listenersToAdd);
		listenersToRemove.clear();
		listenersToAdd.clear();

		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onBlockChange(pos, chunk, state, newstate);
		}
	}

	public static void scheduleAddListener(ChangeListener listener) {
		listenersToAdd.add(listener);
	}
	public static <T extends ChangeListener> void scheduleRemoveListenerOfType(Class<T> c, ChangeListener listener) {
		for (int i = 0; i < listeners.size(); i++) {
			if (listeners.get(i).getClass().equals(c)) {
//				listeners.remove(i);
				listenersToRemove.add(listeners.get(i));
				return;
			}
		}
	}

	public static void clearListeners() {
		listeners.clear();
	}

	public static List<ChangeListener> getListeners() {
		return listeners;
	}
}
