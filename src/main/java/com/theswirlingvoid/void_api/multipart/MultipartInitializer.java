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

package com.theswirlingvoid.void_api.multipart;

import com.theswirlingvoid.void_api.multipart.change_detection.ChangeListenerList;
import com.theswirlingvoid.void_api.multipart.prebuilt.CoreTemplates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public class MultipartInitializer {
	public static void initializeServer(MinecraftServer server) {
		CoreTemplates.registerCores(server);
//		ChangeListenerList.INSTANCE.loadListenerList(server);
	}
}
