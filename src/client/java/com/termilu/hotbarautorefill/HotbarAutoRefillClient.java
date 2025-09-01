package com.termilu.hotbarautorefill;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HotbarAutoRefillClient implements ClientModInitializer {
	public static final String MOD_ID = "hotbar-auto-refill";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Duplication prevention
	private static long lastHandledTick = -1;
	private static Hand lastHandledHand = null;

	@Override
	public void onInitializeClient() {
		LOGGER.info("Hotbar Auto Refill: client init");
		// Avoiding fragile mixin injections by using Fabric events instead.
		// These events fire before the action is processed, so we need to
		// take a snapshot now and check the result next tick.
		// This is more robust and should need less frequent updates.
		TickRunner.init();

		// Right-click on blocks (placements, item uses on blocks)
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			if (world.isClient) {
				snapshotAndSchedule(player);
				LOGGER.info("UseBlockCallback triggered");
			}
			return ActionResult.PASS;
		});

		// Right-click in air (item use like ender pearls, food, etc.)
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (world.isClient) {
				snapshotAndSchedule(player);
				LOGGER.info("UseItemCallback triggered");
			}
			return ActionResult.PASS;
		});

		// Right-click on entity (minecarts, buckets on cows, etc.)
//		UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
//			if (world.isClient) snapshotAndSchedule(player);
//			LOGGER.info("UseEntityCallback triggered");
//			return ActionResult.PASS;
//		});
	}

	// Take a pre-use snapshot, then check the result next tick
	private static void snapshotAndSchedule(PlayerEntity player) {
		final ItemStack preMain = player.getMainHandStack().copy();
		final ItemStack preOff  = player.getOffHandStack().copy();
		final int preSelected   = player.getInventory().getSelectedSlot();

		TickRunner.schedule(() -> {
			Hand consumed = detectConsumedHand(player, preMain, preOff, preSelected);
			if (consumed == null) return;

			// Deduplicate: only handle once per tick per hand
			long currentTick = player.age;
			if (lastHandledTick == currentTick && lastHandledHand == consumed) return;
			lastHandledTick = currentTick;
			lastHandledHand = consumed;

			// Pass the *pre* stack so we know exactly what to refill (item + NBT)
			ItemStack preUsed = (consumed == Hand.MAIN_HAND) ? preMain : preOff;
			if (!preUsed.isEmpty()) {
				HotbarAutoRefillClient.LOGGER.info("Detected depletion in {}", consumed);
				RefillLogic.tryRefill(player, consumed, preUsed);
			}
		});
	}

	// Decide which hand actually hit zero after the action resolved
	private static Hand detectConsumedHand(PlayerEntity p, ItemStack preMain, ItemStack preOff, int preSelected) {
		ItemStack nowMain = p.getMainHandStack();
		ItemStack nowOff  = p.getOffHandStack();

		boolean mainDepleted = !preMain.isEmpty() && nowMain.isEmpty();
		boolean offDepleted  = !preOff.isEmpty()  && nowOff.isEmpty();

		if (mainDepleted ^ offDepleted) {
			return mainDepleted ? Hand.MAIN_HAND : Hand.OFF_HAND;
		}

		// Tie-breakers / odd cases:
		// - If both emptied (rare), prefer the previously selected slot.
		if (mainDepleted && offDepleted) {
			return (p.getInventory().getSelectedSlot() == preSelected) ? Hand.MAIN_HAND : Hand.OFF_HAND;
		}

		// If neither is empty, treat as "no consumption" (server may have canceled).
		return null;
	}
}