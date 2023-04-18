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

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

// i am going to throw myself out the window
public class ChangeListenerAdapterFactory implements TypeAdapterFactory {

	private final Class<? extends ChangeListener> implementationClass;

	public ChangeListenerAdapterFactory(Class<? extends ChangeListener> implementationClass) {
		this.implementationClass = implementationClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		if (ChangeListener.class.equals(type.getRawType())) {
			return (TypeAdapter<T>) gson.getAdapter(implementationClass);
		}
		return null;
	}
}
