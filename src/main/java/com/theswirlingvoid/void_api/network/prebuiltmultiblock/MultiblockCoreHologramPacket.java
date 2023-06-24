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

package com.theswirlingvoid.void_api.network.prebuiltmultiblock;

import com.theswirlingvoid.void_api.multipart.prebuilt.CoreTemplates;
import com.theswirlingvoid.void_api.multipart.prebuilt.MultiblockCore;
import com.theswirlingvoid.void_api.multipart.prebuilt.PrebuiltMultiblockTemplate;
import com.theswirlingvoid.void_api.network.PacketHandler;
import com.theswirlingvoid.void_api.rendering.MultiblockCoreHologramRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.function.Supplier;

public class MultiblockCoreHologramPacket {

	// for future reference:
	// [SERVER] packet -> bytebuf -----------> [CLIENT] bytebuf -> packet -> do stuff with packet class variables

	private static final String MULTIBLOCK_NAME_NBT_STRING = "multiblock_name";
	private MultiblockCoreHologramPacket() {}

	public static class Clientbound {

		public final BlockPos corePos;
		public final Map<BlockPos, BlockState> renderList;
		public final String multiblockName;

		public Clientbound(BlockPos corePos, Map<BlockPos, BlockState> renderList, String multiblockName) {
			this.corePos = corePos;
			this.renderList = renderList;
			this.multiblockName = multiblockName;
		}

		public static void encode(Clientbound msg, FriendlyByteBuf friendlyByteBuf) {

			friendlyByteBuf.writeBlockPos(msg.corePos);
			friendlyByteBuf.writeMap(
					msg.renderList,
					(fBuf, pos) -> fBuf.writeBlockPos(pos),
					(fBuf, state) -> fBuf.writeNbt(NbtUtils.writeBlockState(state))
			);
			CompoundTag nameNbt = new CompoundTag();
			nameNbt.putString(MULTIBLOCK_NAME_NBT_STRING, msg.multiblockName);
			friendlyByteBuf.writeNbt(nameNbt);
			 // friendly byte buf is a queue. FIFO
		}

		public static Clientbound decode(FriendlyByteBuf friendlyByteBuf) {

			BlockPos readBlockPos = friendlyByteBuf.readBlockPos();
			Map<BlockPos, BlockState> readMap = friendlyByteBuf.readMap(
					(fBuf) -> fBuf.readBlockPos(),
					(fBuf) -> {
						// mojang. why did you screw up this method????????
						CompoundTag nbtState = fBuf.readNbt();
						return NbtUtils.readBlockState(
								Minecraft.getInstance().level.holderLookup(Registries.BLOCK), nbtState
						);
					}
			);
			String readName = friendlyByteBuf.readNbt().getString(MULTIBLOCK_NAME_NBT_STRING);

			return new Clientbound(readBlockPos, readMap, readName);
		}

		public static void handle(Clientbound msg, Supplier<NetworkEvent.Context> ctx) {
			// HANDLED ON CLIENT
			// call from consumerMainThread!!!!! because i removed enqueueWork!!!!!!!
//			ctx.get().enqueueWork(() -> {
			DistExecutor.unsafeRunWhenOn(
					Dist.CLIENT,
					() -> {
						return () -> Clientbound.handlePacket(msg, ctx);
					}
			);
//			});
//			ctx.get().setPacketHandled(true);
		}

		private static void handlePacket(Clientbound msg, Supplier<NetworkEvent.Context> ctx) {
			MultiblockCoreHologramRenderer.blocksToRender.put(msg.corePos, msg.renderList);
			MultiblockCoreHologramRenderer.setStructureName(msg.multiblockName);
		}

	}

	public static class Serverbound {
		public final BlockPos clientCorePos;
		public final int mirror;
		public final int rotation;

		public Serverbound(BlockPos corePos, int mirror, int rotation) {
			this.clientCorePos = corePos;
			this.mirror = mirror;
			this.rotation = rotation;
		}

		public static void encode(Serverbound msg, FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeBlockPos(msg.clientCorePos);
			friendlyByteBuf.writeInt(msg.mirror);
			friendlyByteBuf.writeInt(msg.rotation);
		}

		public static Serverbound decode(FriendlyByteBuf friendlyByteBuf) {
			BlockPos readPos = friendlyByteBuf.readBlockPos();
			int mirror = friendlyByteBuf.readInt();
			int rotation = friendlyByteBuf.readInt();
			return new Serverbound(readPos, mirror, rotation);
		}

		public static void handle(Serverbound msg, Supplier<NetworkEvent.Context> ctx) {
			// HANDLED ON SERVER
			ServerPlayer sender = ctx.get().getSender();
			Level senderLevel = sender.level;
			BlockPos potentialCorePos = msg.clientCorePos;
			BlockState potentialCoreState = senderLevel.getBlockState(msg.clientCorePos);

			if (CoreTemplates.isBlockSavedCore(potentialCorePos, senderLevel, potentialCoreState)) {

				MultiblockCore coreToRender =
						new MultiblockCore(
								potentialCorePos,
								senderLevel.dimension(),
								CoreTemplates.getFromBlock(potentialCoreState.getBlock())
						);

				PrebuiltMultiblockTemplate template = coreToRender.getTemplate();
				String blockName = ForgeRegistries.BLOCKS.getKey(template.getMasterBlock()).getPath();
				String namespace = template.getResLoc().getNamespace();
				Component text = Component.translatable("multiblock."+namespace+"."+blockName);

				Mirror mirror = Mirror.values()[msg.mirror];
				Rotation rotation = Rotation.values()[msg.rotation];

				MultiblockCoreHologramPacket.Clientbound renderPacket =
						new MultiblockCoreHologramPacket.Clientbound(
								potentialCorePos,
								coreToRender.getAbsoluteObservingPositions(mirror, rotation),
								text.getString()
						);

				PacketHandler.INSTANCE.sendTo(
						renderPacket,
						sender.connection.getConnection(),
						NetworkDirection.PLAY_TO_CLIENT
				);
			}


		}
	}
}
