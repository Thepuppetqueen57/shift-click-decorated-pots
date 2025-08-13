package xyz.puppet57.shiftClickDecoratedPots.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ShiftClickDecoratedPotsClient implements ClientModInitializer {

    private int clicksRemaining = 0;
    private BlockHitResult bhrToClick = null;

    @Override
    public void onInitializeClient() {
        MinecraftClient mc = MinecraftClient.getInstance();

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hand == Hand.MAIN_HAND) {
                BlockPos pos = hitResult.getBlockPos();
                if (player.isSneaking() && world.getBlockState(pos).getBlock() == Blocks.DECORATED_POT) {
                    assert mc.player != null;

                    Vec3d eyePos = mc.player.getEyePos();
                    Vec3d blockCenter = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    Vec3d direction = blockCenter.subtract(eyePos).normalize();

                    bhrToClick = new BlockHitResult(
                        blockCenter,
                        getClosestFacing(direction),
                        pos,
                        false
                    );
                    clicksRemaining = 64;
                }
            }
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (clicksRemaining > 0 && bhrToClick != null) {
                assert mc.player != null;
                if (!mc.player.getMainHandStack().isEmpty() && mc.interactionManager != null) {
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhrToClick);
                }
                clicksRemaining--;
                if (clicksRemaining == 0) {
                    bhrToClick = null;
                }
            }
        });
    }

    private static Direction getClosestFacing(Vec3d direction) {
        double absX = Math.abs(direction.x);
        double absY = Math.abs(direction.y);
        double absZ = Math.abs(direction.z);

        if (absX > absY && absX > absZ) {
            return direction.x > 0 ? Direction.EAST : Direction.WEST;
        } else if (absY > absX && absY > absZ) {
            return direction.y > 0 ? Direction.UP : Direction.DOWN;
        } else {
            return direction.z > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }
}
