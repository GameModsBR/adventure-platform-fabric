/*
 * Copyright © 2020 zml [at] stellardrift [.] ca
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the “Software”), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ca.stellardrift.text.fabric.mixin;

import ca.stellardrift.text.fabric.FabricAudience;
import ca.stellardrift.text.fabric.GameEnums;
import ca.stellardrift.text.fabric.TextAdapter;
import com.mojang.authlib.GameProfile;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements FabricAudience {
    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    @Shadow public abstract void playSound(SoundEvent soundEvent, SoundCategory soundCategory, float f, float g);

    @Shadow public abstract void sendAbilitiesUpdate();

    public MixinServerPlayerEntity(World world, BlockPos pos, GameProfile gameProfile) {
        super(world, pos, gameProfile);
    }

    /**
     * Send a message to this receiver as a component
     *
     * @param text The text to send
     */
    @Override
    public void sendMessage(Component text) {
        sendMessage(MessageType.SYSTEM, text);
    }

    @Override
    public void sendMessage(MessageType type, Component text, UUID source) {
        if (type == MessageType.GAME_INFO) {
            showTitle(TitleS2CPacket.Action.ACTIONBAR, text);
        } else {
            networkHandler.sendPacket(new GameMessageS2CPacket(TextAdapter.adapt(text), type, source));
        }
    }

    @Override
    public void showTitle(TitleS2CPacket.Action field, Component text) {
        networkHandler.sendPacket(new TitleS2CPacket(field, TextAdapter.adapt(text)));
    }

    @Override
    public void showBossBar(@NonNull BossBar bar) {
        ((ServerBossBar) bar).addPlayer((ServerPlayerEntity) (Object) this);
    }

    @Override
    public void hideBossBar(@NonNull BossBar bar) {
        ((ServerBossBar) bar).removePlayer((ServerPlayerEntity) (Object) this);
    }

    @Override
    public void playSound(@NonNull Sound sound) {
        this.networkHandler.sendPacket(new PlaySoundIdS2CPacket(TextAdapter.adapt(sound.name()),
                GameEnums.SOUND_SOURCE.toMinecraft(sound.source()), this.getPos(), sound.volume(), sound.pitch()));
    }

    @Override
    public void stopSound(@NonNull SoundStop stop) {
        Sound.@Nullable Source src = stop.source();
        @Nullable SoundCategory cat = src == null ? null : GameEnums.SOUND_SOURCE.toMinecraft(src);
        this.networkHandler.sendPacket(new StopSoundS2CPacket(TextAdapter.adapt(stop.sound()), cat));
    }

    @Override
    public void showTitle(final @NonNull Title title) {
        final @Nullable Text titleText = TextAdapter.adapt(title.title());
        final @Nullable Text subtitle = TextAdapter.adapt(title.subtitle());

        if (titleText != null) {
            this.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, titleText));
        }
        if (subtitle != null) {
            this.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE, titleText));
        }

        final int fadeIn = ticks(title.fadeInTime());
        final int fadeOut = ticks(title.fadeOutTime());
        final int dwell = ticks(title.stayTime());
        if (fadeIn != -1 || fadeOut != -1 || dwell != -1) {
            this.networkHandler.sendPacket(new TitleS2CPacket(fadeIn, dwell, fadeOut));
        }

    }

    private int ticks(Duration duration) {
        return (int) duration.get(ChronoUnit.SECONDS) * 20;
    }

    @Override
    public void clearTitle() {
        this.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.CLEAR, null));
    }

    @Override
    public void resetTitle() {
        this.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.RESET, null));
    }

}
