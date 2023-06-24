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

package com.theswirlingvoid.void_api.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.theswirlingvoid.void_api.VoidAPI;
import com.theswirlingvoid.void_api.network.PacketHandler;
import com.theswirlingvoid.void_api.network.prebuiltmultiblock.MultiblockCoreHologramPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = VoidAPI.MODID, value = Dist.CLIENT)
public class MultiblockCoreHologramRenderer {

	private static String structureName;
	public static Map<BlockPos, Map<BlockPos, BlockState>> blocksToRender = new HashMap<>();

	public static void renderCurrentBlockHolos(RenderLevelStageEvent event) {

		Minecraft minecraftClient = Minecraft.getInstance();
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {

			PoseStack poseStack = event.getPoseStack();

			BlockRenderDispatcher dispatcher = minecraftClient.getBlockRenderer();
			ModelBlockRenderer blockRenderer = dispatcher.getModelRenderer();
			BlockColors blockColors = minecraftClient.getBlockColors();
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder vertConsumer = tesselator.getBuilder();
			vertConsumer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

			for (Map<BlockPos, BlockState> renderSet : blocksToRender.values()) {
				for (Map.Entry<BlockPos, BlockState> entry : renderSet.entrySet()) {

					Vec3 renderView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

					BlockPos pos = entry.getKey();
					BlockState state = entry.getValue();

					poseStack.pushPose();
					poseStack.translate(pos.getX()-renderView.x, pos.getY()-renderView.y, pos.getZ()-renderView.z);

					poseStack.scale(0.8f,0.8f,0.8f);
					poseStack.translate(0.1f,0.1f,0.1f);

					BakedModel bakedModel = dispatcher.getBlockModel(state);

					int hex = blockColors.getColor(state, null, null, 0);
					// every bitshift right of 4 divides by 16; dividing by 16 is a hexadecimal shift right
					// so a bitshift of 8 shifts hex by 2 removing the first two digits
					float red = ((hex >> 16) & 0xFF) / 255.0f; // 4 digits knocked out, must bitwise and w/ 2 remaining digits
					float green = ((hex >> 8) & 0x00FF) / 255.0f; // two digits knocked out
					float blue = (hex & 0x0000FF) / 255.0f; // no digits knocked out


					ModelData modelData = ModelData.EMPTY;
					for (net.minecraft.client.renderer.RenderType rt : bakedModel.getRenderTypes(state, RandomSource.create(42), modelData)) {
						blockRenderer.renderModel(poseStack.last(), vertConsumer, state, bakedModel, red, green, blue, 0xF000F0, OverlayTexture.NO_OVERLAY, modelData, rt);
					}

					poseStack.popPose();

				}
			}

			float ticksDuration = 40f;
			float renderTickCycle = (event.getRenderTick() % ticksDuration);
//			float renderTickCycle = (float) Math.pow( ((1/7.36806)*(renderTickCycleX-ticksDuration)), 3) + ticksDuration;
			// sin period must be 40
			float opacityMin = 0.15f;
			float opacityMax = 0.9f;
			float opacity = (float) Math.pow(Math.sin( (Math.PI/ticksDuration)*renderTickCycle ), 2)*(opacityMax-opacityMin) + opacityMin;

			RenderSystem.setShaderColor(opacity,opacity,opacity, opacity);

			tesselator.end();

			RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);

		}

	}

	@SubscribeEvent
	public static void renderStage(RenderLevelStageEvent event) { // running on the client
//		for (Consumer<RenderLevelStageEvent> function : renderFunctions.values()) {
//			function.accept(event);
//		}

		final Mirror TEST_MIRROR = Mirror.NONE;
		final Rotation TEST_ROTATION = Rotation.CLOCKWISE_90;
		Minecraft minecraft = Minecraft.getInstance();

		BlockPos checkPos =
				new BlockPos(minecraft.player.getEyePosition().subtract(0,2,0));
//		Block lookBlock = minecraft.level.getBlockState(checkPos).getBlock();

		if (blocksToRender.isEmpty()) {
			// this packet will ask the server if it can fill blocksToRender with the multiblock block info
			// of the core at the position the client gives it. If the client is not currently rendering something else
			// here it will add the blocks the server sends to render
			MultiblockCoreHologramPacket.Serverbound serverPacket =
					new MultiblockCoreHologramPacket.Serverbound(checkPos, TEST_MIRROR.ordinal(), TEST_ROTATION.ordinal());

			PacketHandler.INSTANCE.sendToServer(
					serverPacket
			);
		} else {
			MultiblockCoreHologramRenderer.renderCurrentBlockHolos(event);
		}

//		if (lookBlock.equals(ModBlocks.MULTIBLOCK_CORE.get())) {
//			BlockHologramRenderer.renderCurrentBlockHolos(event);
//		}


	}

	@SubscribeEvent
	public static void renderText(RenderGuiOverlayEvent event) {
		if (!blocksToRender.isEmpty()) {

			PoseStack poseStack = event.getPoseStack();

//			poseStack.translate(0.25f,0.25f,0.25f); // half of the now empty space

			Minecraft minecraftClient = Minecraft.getInstance();

			Component comp = Component.literal(ChatFormatting.BOLD + "Viewing template of: " + ChatFormatting.DARK_AQUA + structureName);

			float j = minecraftClient.getWindow().getGuiScaledWidth()*0.05f;
			float k = minecraftClient.getWindow().getGuiScaledHeight()*0.8f;

//			float j = minecraftClient.getWindow().getGuiScaledWidth()/2f;
//			float k = minecraftClient.getWindow().getGuiScaledHeight()/2f;

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();

			poseStack.pushPose();
			poseStack.scale(0.75f,0.75f,0.75f);
			minecraftClient.font.drawShadow(poseStack, comp, j, k*(1/0.75f)*0.9f, 0xFFFFFFFF);
			poseStack.popPose();

			RenderSystem.disableBlend();
		}
	}

	@SubscribeEvent
	public static void interactTemp(PlayerInteractEvent event) {
		if (event.getEntity() != null) {
			if (event.getItemStack().getItem() == Items.DIAMOND_SWORD) {
				blocksToRender.clear();
			}
		}
	}

	public static void setStructureName(String name) {
		structureName = name;
	}
}
