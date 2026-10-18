package com.prime.iamnot;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;

/** Makes a mob run away from players. */
public class PrimeAvoidGoal extends AvoidEntityGoal<Player> {
    public PrimeAvoidGoal(PathfinderMob mob) {
        super(mob, Player.class, 16.0F, 1.0D, 1.5D);
    }
}
