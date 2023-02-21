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

package com.theswirlingvoid.void_api.block;

import com.theswirlingvoid.void_api.ModMain;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
	public static final DeferredRegister<Block> BLOCKS =
			DeferredRegister.create(ForgeRegistries.BLOCKS, ModMain.MODID);
	public static final DeferredRegister<Item> BLOCK_ITEMS =
			DeferredRegister.create(ForgeRegistries.ITEMS, ModMain.MODID);
	//------------------------------------------------------------------
	public static final RegistryObject<Block> EXPERIMENTAL_MULTIPART =
			BLOCKS.register(
					"experimental_multipart",
					() -> new ExperimentalMultipart()
			);


	//------------------------------------------------------------------
	public static final RegistryObject<Item> EXPERIMENTAL_MULTIPART_BI =
			BLOCK_ITEMS.register(
					"experimental_multipart",
					() -> new BlockItem(EXPERIMENTAL_MULTIPART.get(), new Item.Properties())
			);

	
	//------------------------------------------------------------------
	public static void registerBlocks() {
		BLOCKS.register(ModMain.modEventBus);
		BLOCK_ITEMS.register(ModMain.modEventBus);
	}
}
