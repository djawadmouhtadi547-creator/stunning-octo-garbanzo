package com.prime.iamnot;

import net.minecraftforge.common.ForgeConfigSpec;

public class PrimeConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue FLOATING_BLOCKS;
    public static final ForgeConfigSpec.BooleanValue FLOATING_ITEMS;
    public static final ForgeConfigSpec.BooleanValue HOSTILES_FLEE;
    public static final ForgeConfigSpec.BooleanValue PASSIVES_ATTACK;
    public static final ForgeConfigSpec.BooleanValue GENEROUS_VILLAGERS;
    public static final ForgeConfigSpec.BooleanValue ANGRY_GOLEMS;
    public static final ForgeConfigSpec.BooleanValue GOLEM_DROPS_BLOCKS;
    public static final ForgeConfigSpec.BooleanValue FALL_HEALS;
    public static final ForgeConfigSpec.BooleanValue FLIPPED_LOGO;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("reverse");
        FLOATING_BLOCKS = b.comment("Anvils / sand / gravel fly UP instead of falling")
                .define("floatingBlocks", true);
        FLOATING_ITEMS = b.comment("Dropped items float up and hover instead of falling")
                .define("floatingItems", true);
        HOSTILES_FLEE = b.comment("Zombies, skeletons, creepers... run away from the player")
                .define("hostilesFlee", true);
        PASSIVES_ATTACK = b.comment("Cows, sheep, pigs, chickens attack the player")
                .define("passivesAttack", true);
        GENEROUS_VILLAGERS = b.comment("Villagers give free gifts to nearby players")
                .define("generousVillagers", true);
        ANGRY_GOLEMS = b.comment("Iron golems attack the player")
                .define("angryGolems", true);
        GOLEM_DROPS_BLOCKS = b.comment("Iron golems drop 3 iron BLOCKS when killed")
                .define("golemDropsBlocks", true);
        FALL_HEALS = b.comment("Fall damage HEALS the player instead of hurting")
                .define("fallHeals", true);
        FLIPPED_LOGO = b.comment("Minecraft logo is upside down on the title screen (client)")
                .define("flippedLogo", true);
        b.pop();
        SPEC = b.build();
    }
}
