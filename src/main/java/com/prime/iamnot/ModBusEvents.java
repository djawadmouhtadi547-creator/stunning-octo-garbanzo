package com.prime.iamnot;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PrimeMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {

    /** Peaceful animals need an attack attribute before they can bite. */
    @SubscribeEvent
    public static void onAttributes(EntityAttributeModificationEvent event) {
        give(event, EntityType.COW);
        give(event, EntityType.MOOSHROOM);
        give(event, EntityType.SHEEP);
        give(event, EntityType.PIG);
        give(event, EntityType.CHICKEN);
    }

    private static void give(EntityAttributeModificationEvent event, EntityType<? extends LivingEntity> type) {
        if (!event.has(type, Attributes.ATTACK_DAMAGE)) {
            event.add(type, Attributes.ATTACK_DAMAGE, 2.0D);
        }
        if (!event.has(type, Attributes.ATTACK_KNOCKBACK)) {
            event.add(type, Attributes.ATTACK_KNOCKBACK, 0.4D);
        }
    }
}
