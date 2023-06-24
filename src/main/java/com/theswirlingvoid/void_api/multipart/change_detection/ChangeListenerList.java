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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ChangeListenerList {

	public static ChangeListenerList INSTANCE = new ChangeListenerList();

	private Set<ChangeListener> listeners = new HashSet<>();
	private Set<ChangeListener> listenersToRemove = new HashSet<>();
	private Set<ChangeListener> listenersToAdd = new HashSet<>();

	private ChangeListenerList() {} // no construction for u!

	public Set<ChangeListener> getListeners() {
		return listeners;
	}

	public void setListeners(Set<ChangeListener> listeners) {
		this.listeners = listeners;
	}

	/**
	 * Schedules a listener to be added. It will be added to the list when <code>update()</code> is called.
	 * @param listener The listener object to add
	 */
	public void scheduleAddListener(ChangeListener listener) {
		listenersToAdd.add(listener);
	}
	public void scheduleRemoveListener(ChangeListener listener) {
		for (Iterator<ChangeListener> i = listeners.iterator(); i.hasNext();) {
			ChangeListener currentListener = i.next();
			if (currentListener.equals(listener)) {
				listenersToRemove.add(currentListener);
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
}
