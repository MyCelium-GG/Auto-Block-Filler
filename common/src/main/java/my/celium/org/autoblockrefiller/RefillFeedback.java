package my.celium.org.autoblockrefiller;

import my.celium.org.util.TextUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * What the player notices when a refill happens: a quiet sound only they hear, a note above the
 * hotbar when the last stack goes in or nothing is left, and in winter a puff of snowflakes and a
 * sleigh-bell chime.
 */
final class RefillFeedback {
    /** Sleigh bells: bell notes stepping up a little, one per refill. */
    private static final float[] JINGLE = {1.5f, 1.68f, 1.89f, 2.0f};
    private static int jingle;

    private RefillFeedback() {
    }

    /** A slot was refilled; {@code stacksLeft} more matching stacks wait in the inventory. */
    static void refilled(ServerPlayer player, ItemStack used, int stacksLeft) {
        boolean winter = RefillMod.winter.get();
        if (RefillMod.refillSound.get()) {
            if (winter) {
                play(player, SoundEvents.NOTE_BLOCK_BELL, 0.35f, JINGLE[jingle++ & 3]);
            } else {
                play(player, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ITEM_PICKUP), 0.2f, 1.7f);
            }
        }
        if (winter) {
            snowPuff(player);
        }
        if (RefillMod.runningLowWarning.get() && stacksLeft == 0) {
            overlay(player, TextUtil.msg(TextUtil.hex("Last stack of ", TextUtil.MUTED), name(used)));
        }
    }

    /** The slot ran empty and nothing in the inventory could refill it. */
    static void ranOut(ServerPlayer player, ItemStack used) {
        if (RefillMod.runningLowWarning.get()) {
            overlay(player, TextUtil.msg(TextUtil.hex("Out of ", TextUtil.WARN), name(used)));
        }
    }

    private static Component name(ItemStack used) {
        return TextUtil.hex(used.getHoverName().getString(), TextUtil.BRIGHT);
    }

    private static void overlay(ServerPlayer player, Component message) {
        player.sendSystemMessage(message, true);
    }

    private static void play(ServerPlayer player, Holder<SoundEvent> sound, float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(sound, SoundSource.PLAYERS,
                player.getX(), player.getY(), player.getZ(), volume, pitch, player.getRandom().nextLong()));
    }

    /** A few snowflakes in front of the player, where the new stack lands in their hand. */
    private static void snowPuff(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 look = player.getLookAngle();
        double x = player.getX() + look.x * 0.6;
        double y = player.getEyeY() - 0.45 + look.y * 0.4;
        double z = player.getZ() + look.z * 0.6;
        level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, 7, 0.18, 0.12, 0.18, 0.01);
    }
}
