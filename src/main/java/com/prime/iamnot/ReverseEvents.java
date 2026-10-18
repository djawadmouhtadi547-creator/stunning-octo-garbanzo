package com.prime.iamnot;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = PrimeMod.MODID)
public class ReverseEvents {

    private static final String TAG_Y0 = "prime_y0";

    // ------------------------------------------------------------------
    // 1) Physics: falling blocks and items go UP
    // ------------------------------------------------------------------
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) return;
        if (!(event.level instanceof ServerLevel level)) return;

        for (Entity e : level.getAllEntities()) {
            if (e instanceof FallingBlockEntity fb) {
                if (!PrimeConfig.FLOATING_BLOCKS.get()) continue;
                fb.setNoGravity(true);
                Vec3 m = fb.getDeltaMovement();
                fb.setDeltaMovement(m.x * 0.9D, 0.18D, m.z * 0.9D);
                fb.hurtMarked = true; // sync velocity to clients
            } else if (e instanceof ItemEntity item) {
                if (!PrimeConfig.FLOATING_ITEMS.get()) continue;
                floatItem(item);
            }
        }
    }

    private static void floatItem(ItemEntity item) {
        CompoundTag data = item.getPersistentData();
        if (!data.contains(TAG_Y0)) {
            data.putDouble(TAG_Y0, item.getY());
        }
        double y0 = data.getDouble(TAG_Y0);
        item.setNoGravity(true);
        Vec3 m = item.getDeltaMovement();
        if (item.getY() < y0 + 1.2D) {
            item.setDeltaMovement(m.x, 0.06D, m.z);
        } else {
            // hover just above the ground so the player can still pick it up
            item.setDeltaMovement(m.x * 0.8D, 0.0D, m.z * 0.8D);
        }
    }

    // ------------------------------------------------------------------
    // 2) Mob AI
    // ------------------------------------------------------------------
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        Entity entity = event.getEntity();

        if (entity instanceof IronGolem golem) {
            if (PrimeConfig.ANGRY_GOLEMS.get()) {
                golem.setPlayerCreated(false);
                if (!hasPlayerTarget(golem.targetSelector)) {
                    golem.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(golem, Player.class, true));
                }
            }
            return;
        }

        if (entity instanceof Animal animal && isAggressiveAnimal(animal)) {
            if (!PrimeConfig.PASSIVES_ATTACK.get()) return;
            removeGoals(animal.goalSelector, g -> g instanceof PanicGoal);
            if (!hasGoal(animal.goalSelector, MeleeAttackGoal.class)) {
                animal.goalSelector.addGoal(1, new MeleeAttackGoal(animal, 1.3D, true));
            }
            if (!hasPlayerTarget(animal.targetSelector)) {
                animal.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(animal, Player.class, true));
            }
            return;
        }

        if (entity instanceof Monster mob && isFleeing(mob)) {
            if (!PrimeConfig.HOSTILES_FLEE.get()) return;
            removeGoals(mob.targetSelector, g -> true);
            removeGoals(mob.goalSelector, g ->
                    g instanceof MeleeAttackGoal
                            || g instanceof RangedAttackGoal
                            || g instanceof RangedBowAttackGoal<?>
                            || g instanceof RangedCrossbowAttackGoal<?>
                            || g instanceof SwellGoal);
            if (!hasGoal(mob.goalSelector, PrimeAvoidGoal.class)) {
                mob.goalSelector.addGoal(1, new PrimeAvoidGoal(mob));
            }
            mob.setTarget(null);
        }
    }

    /** Backup: hostile mobs can never lock on to a player. */
    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (!PrimeConfig.HOSTILES_FLEE.get()) return;
        if (event.getEntity() instanceof Monster mob && isFleeing(mob)
                && event.getNewTarget() instanceof Player) {
            event.setCanceled(true);
        }
    }

    private static boolean isFleeing(Monster mob) {
        // brain-driven mobs are left alone
        return !(mob instanceof Warden || mob instanceof AbstractPiglin
                || mob instanceof Zoglin);
    }

    private static boolean isAggressiveAnimal(Animal a) {
        return a instanceof Cow || a instanceof Sheep || a instanceof Pig || a instanceof Chicken;
    }

    private static boolean hasGoal(GoalSelector selector, Class<?> type) {
        for (WrappedGoal w : selector.getAvailableGoals()) {
            if (type.isInstance(w.getGoal())) return true;
        }
        return false;
    }

    private static boolean hasPlayerTarget(GoalSelector selector) {
        return hasGoal(selector, NearestAttackableTargetGoal.class);
    }

    private static void removeGoals(GoalSelector selector, Predicate<Goal> filter) {
        for (WrappedGoal w : new ArrayList<>(selector.getAvailableGoals())) {
            if (filter.test(w.getGoal())) {
                selector.removeGoal(w.getGoal());
            }
        }
    }

    // ------------------------------------------------------------------
    // 3) Iron golem drops 3 iron blocks
    // ------------------------------------------------------------------
    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof IronGolem golem && PrimeConfig.GOLEM_DROPS_BLOCKS.get()) {
            event.getDrops().clear();
            event.getDrops().add(new ItemEntity(golem.level(), golem.getX(), golem.getY(), golem.getZ(),
                    new ItemStack(Items.IRON_BLOCK, 3)));
        }
    }

    // ------------------------------------------------------------------
    // 4) Generous villagers
    // ------------------------------------------------------------------
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) return;
        if (villager.level().isClientSide) return;
        if (!PrimeConfig.GENEROUS_VILLAGERS.get()) return;
        if (villager.tickCount % 80 != 0) return;

        Player player = villager.level().getNearestPlayer(villager, 6.0D);
        if (player == null || player.isSpectator()) return;
        if (villager.getRandom().nextInt(3) != 0) return;

        ItemStack gift;
        switch (villager.getRandom().nextInt(4)) {
            case 0 -> gift = new ItemStack(Items.EMERALD, 1 + villager.getRandom().nextInt(3));
            case 1 -> gift = new ItemStack(Items.BREAD, 2);
            case 2 -> gift = new ItemStack(Items.GOLDEN_CARROT, 1);
            default -> gift = new ItemStack(Items.APPLE, 2);
        }

        ItemEntity drop = new ItemEntity(villager.level(), villager.getX(), villager.getEyeY() - 0.3D,
                villager.getZ(), gift);
        Vec3 dir = player.position().subtract(villager.position());
        dir = new Vec3(dir.x, 0.0D, dir.z).normalize().scale(0.25D);
        drop.setDeltaMovement(dir.x, 0.2D, dir.z);
        drop.setPickUpDelay(10);
        villager.level().addFreshEntity(drop);
        villager.swing(InteractionHand.MAIN_HAND);
    }

    // ------------------------------------------------------------------
    // 5) Fall damage heals
    // ------------------------------------------------------------------
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!PrimeConfig.FALL_HEALS.get()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            float amount = (event.getDistance() - 3.0F) * event.getDamageMultiplier();
            if (amount > 0.0F) {
                player.heal(amount);
                event.setCanceled(true);
            }
        }
    }
}
