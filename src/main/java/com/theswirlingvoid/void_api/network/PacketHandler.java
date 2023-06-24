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

package com.theswirlingvoid.void_api.network;

import com.theswirlingvoid.void_api.VoidAPI;
import com.theswirlingvoid.void_api.network.prebuiltmultiblock.MultiblockCoreHologramPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE =
			NetworkRegistry.newSimpleChannel(
					new ResourceLocation(VoidAPI.MODID, "main"),
					() -> PROTOCOL_VERSION,
					PROTOCOL_VERSION::equals,
					PROTOCOL_VERSION::equals
			);
	private static int ID = 0;

	private PacketHandler() {}

	public static void init() {
		INSTANCE.messageBuilder(MultiblockCoreHologramPacket.Clientbound.class, ID++)
				.encoder(MultiblockCoreHologramPacket.Clientbound::encode)
				.decoder(MultiblockCoreHologramPacket.Clientbound::decode)
				.consumerMainThread(MultiblockCoreHologramPacket.Clientbound::handle)
				.add();

		INSTANCE.messageBuilder(MultiblockCoreHologramPacket.Serverbound.class, ID++)
				.encoder(MultiblockCoreHologramPacket.Serverbound::encode)
				.decoder(MultiblockCoreHologramPacket.Serverbound::decode)
				.consumerMainThread(MultiblockCoreHologramPacket.Serverbound::handle)
				.add();
	}
}
