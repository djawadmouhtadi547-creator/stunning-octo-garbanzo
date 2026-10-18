package com.prime.iamnot;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(PrimeMod.MODID)
public class PrimeMod {
    public static final String MODID = "prime";

    public PrimeMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PrimeConfig.SPEC);
    }
}
