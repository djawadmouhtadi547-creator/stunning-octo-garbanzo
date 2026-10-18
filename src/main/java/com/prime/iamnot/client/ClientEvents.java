package com.prime.iamnot.client;

import com.prime.iamnot.PrimeConfig;
import com.prime.iamnot.PrimeMod;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = PrimeMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Pre event) {
        if (!PrimeConfig.FLIPPED_LOGO.get()) return;
        if (!(event.getScreen() instanceof TitleScreen title)) return;
        try {
            // find the LogoRenderer field by type, so it works with any mapping name
            for (Field f : TitleScreen.class.getDeclaredFields()) {
                if (f.getType() == LogoRenderer.class) {
                    f.setAccessible(true);
                    if (!(f.get(title) instanceof FlippedLogoRenderer)) {
                        f.set(title, new FlippedLogoRenderer(false));
                    }
                }
            }
        } catch (Exception ignored) {
            // if anything goes wrong the normal logo is shown
        }
    }
}
