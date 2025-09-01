package com.termilu.hotbarautorefill;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import static com.termilu.hotbarautorefill.HotbarAutoRefill.LOGGER;

public class RefillLogic {

    // Implement the logic for refilling the hotbar here
    public static void tryRefill(PlayerEntity player, Hand hand) {
        // Log which hand was used to place the block
        LOGGER.info("Block placed from " + (hand == Hand.MAIN_HAND ? "MAIN_HAND" : "OFF_HAND"));
    }
}
