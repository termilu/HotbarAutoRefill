package com.termilu.hotbarautorefill;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public final class RefillLogic {
    private RefillLogic() {}

    /**
     * Refill the hand that just hit zero with a matching stack from inventory.
     * @param preUsed the *pre-use* stack from that hand (for item+nbt matching)
     */
    public static void tryRefill(PlayerEntity player, Hand hand, ItemStack preUsed) {
        if (player.isCreative() || player.isSpectator()) return; // don't fight creative/spectator logic
        if (preUsed.isEmpty()) return;

        final int dst = (hand == Hand.MAIN_HAND) ? player.getInventory().getSelectedSlot() : 40; // 40 = off-hand

        // Only proceed if the destination is still empty (server-confirmed depletion).
        if (!player.getInventory().getStack(dst).isEmpty()) return;

        final int src = findBestMatchSlot(player, preUsed, dst);
        if (src == -1) return;

        moveStack(player, src, dst);
    }

    private static int findBestMatchSlot(PlayerEntity player, ItemStack toMatch, int dst) {
        var inv = player.getInventory();
        int best = -1;
        int bestCount = Integer.MIN_VALUE;

        // Search all main inventory slots (hotbar 0..8 and inventory 9..35), skip destination
        for (int i = 0; i < 36; i++) {
            if (i == dst) continue;
            ItemStack st = inv.getStack(i);
            if (st.isEmpty()) continue;

            if (sameItemAndNbt(st, toMatch)) {
                // Prefer larger stacks (fewer future refills). Change to "prefer partial" if you like.
                int score = st.getCount();
                if (score > bestCount) {
                    bestCount = score;
                    best = i;
                }
            }
        }
        return best;
    }

    private static boolean sameItemAndNbt(ItemStack a, ItemStack b) {
        // Strict match: same item + same NBT
        return ItemStack.areEqual(a, b);
    }

    private static void moveStack(PlayerEntity player, int src, int dst) {
        var inv = player.getInventory();
        ItemStack from = inv.getStack(src);
        if (from.isEmpty()) return;

        // Destination should be empty here; move the entire stack.
        inv.setStack(dst, from.copy());
        inv.setStack(src, ItemStack.EMPTY);
    }
}