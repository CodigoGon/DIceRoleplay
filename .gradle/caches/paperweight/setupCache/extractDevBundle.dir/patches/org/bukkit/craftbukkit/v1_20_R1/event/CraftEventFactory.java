package org.bukkit.craftbukkit.v1_20_R1.event;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.Statistic.Type;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.craftbukkit.v1_20_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_20_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R1.CraftLootTable;
import org.bukkit.craftbukkit.v1_20_R1.CraftRaid;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftStatistic;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockStates;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftRaider;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftSpellcaster;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMetaBook;
import org.bukkit.craftbukkit.v1_20_R1.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftVector;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Animals;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Fish;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Spellcaster;
import org.bukkit.entity.Strider;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BellResonateEvent;
import org.bukkit.event.block.BellRingEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.ArrowBodyCountChangeEvent;
import org.bukkit.event.entity.BatToggleSleepEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntitySpellCastEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.entity.StriderTemperatureChangeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerBucketFishEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerRecipeBookClickEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

public class CraftEventFactory {
    public static org.bukkit.block.Block blockDamage; // For use in EntityDamageByBlockEvent
    public static Entity entityDamage; // For use in EntityDamageByEntityEvent

    // helper methods
    private static boolean canBuild(ServerLevel world, Player player, int x, int z) {
        int spawnSize = Bukkit.getServer().getSpawnRadius();

        if (world.dimension() != Level.OVERWORLD) return true;
        if (spawnSize <= 0) return true;
        if (((CraftServer) Bukkit.getServer()).getHandle().getOps().isEmpty()) return true;
        if (player.isOp()) return true;

        BlockPos chunkcoordinates = world.getSharedSpawnPos();

        int distanceFromSpawn = Math.max(Math.abs(x - chunkcoordinates.getX()), Math.abs(z - chunkcoordinates.getZ()));
        return distanceFromSpawn > spawnSize;
    }

    public static <T extends Event> T callEvent(T event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * PlayerSignOpenEvent
     */
    public static boolean callPlayerSignOpenEvent(net.minecraft.world.entity.player.Player player, SignBlockEntity tileEntitySign, boolean front, PlayerSignOpenEvent.Cause cause) {
        final Block block = CraftBlock.at(tileEntitySign.getLevel(), tileEntitySign.getBlockPos());
        final Sign sign = (Sign) CraftBlockStates.getBlockState(block);
        final Side side = (front) ? Side.FRONT : Side.BACK;
        return CraftEventFactory.callPlayerSignOpenEvent((Player) player.getBukkitEntity(), sign, side, cause);
    }

    /**
     * PlayerSignOpenEvent
     */
    public static boolean callPlayerSignOpenEvent(Player player, Sign sign, Side side, PlayerSignOpenEvent.Cause cause) {
        final PlayerSignOpenEvent event = new PlayerSignOpenEvent(player, sign, side, cause);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    /**
     * PlayerBedEnterEvent
     */
    public static Either<net.minecraft.world.entity.player.Player.BedSleepingProblem, Unit> callPlayerBedEnterEvent(net.minecraft.world.entity.player.Player player, BlockPos bed, Either<net.minecraft.world.entity.player.Player.BedSleepingProblem, Unit> nmsBedResult) {
        BedEnterResult bedEnterResult = nmsBedResult.mapBoth(new Function<net.minecraft.world.entity.player.Player.BedSleepingProblem, BedEnterResult>() {
            @Override
            public BedEnterResult apply(net.minecraft.world.entity.player.Player.BedSleepingProblem t) {
                switch (t) {
                    case NOT_POSSIBLE_HERE:
                        return BedEnterResult.NOT_POSSIBLE_HERE;
                    case NOT_POSSIBLE_NOW:
                        return BedEnterResult.NOT_POSSIBLE_NOW;
                    case TOO_FAR_AWAY:
                        return BedEnterResult.TOO_FAR_AWAY;
                    case NOT_SAFE:
                        return BedEnterResult.NOT_SAFE;
                        // Paper start
                    case OBSTRUCTED:
                        return BedEnterResult.OBSTRUCTED;
                        // Paper end
                    default:
                        return BedEnterResult.OTHER_PROBLEM;
                }
            }
        }, t -> BedEnterResult.OK).map(java.util.function.Function.identity(), java.util.function.Function.identity());

        PlayerBedEnterEvent event = new PlayerBedEnterEvent((Player) player.getBukkitEntity(), CraftBlock.at(player.level(), bed), bedEnterResult);
        Bukkit.getServer().getPluginManager().callEvent(event);

        Result result = event.useBed();
        if (result == Result.ALLOW) {
            return Either.right(Unit.INSTANCE);
        } else if (result == Result.DENY) {
            return Either.left(net.minecraft.world.entity.player.Player.BedSleepingProblem.OTHER_PROBLEM);
        }

        return nmsBedResult;
    }

    /**
     * Entity Enter Love Mode Event
     */
    public static EntityEnterLoveModeEvent callEntityEnterLoveModeEvent(net.minecraft.world.entity.player.Player entityHuman, Animal entityAnimal, int loveTicks) {
        EntityEnterLoveModeEvent entityEnterLoveModeEvent = new EntityEnterLoveModeEvent((Animals) entityAnimal.getBukkitEntity(), entityHuman != null ? (HumanEntity) entityHuman.getBukkitEntity() : null, loveTicks);
        Bukkit.getPluginManager().callEvent(entityEnterLoveModeEvent);
        return entityEnterLoveModeEvent;
    }

    /**
     * Player Harvest Block Event
     */
    public static PlayerHarvestBlockEvent callPlayerHarvestBlockEvent(Level world, BlockPos blockposition, net.minecraft.world.entity.player.Player who, InteractionHand enumhand, List<ItemStack> itemsToHarvest) {
        List<org.bukkit.inventory.ItemStack> bukkitItemsToHarvest = new ArrayList<>(itemsToHarvest.stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList()));
        Player player = (Player) who.getBukkitEntity();
        PlayerHarvestBlockEvent playerHarvestBlockEvent = new PlayerHarvestBlockEvent(player, CraftBlock.at(world, blockposition), CraftEquipmentSlot.getHand(enumhand), bukkitItemsToHarvest);
        Bukkit.getPluginManager().callEvent(playerHarvestBlockEvent);
        return playerHarvestBlockEvent;
    }

    /**
     * Player Fish Bucket Event
     */
    public static PlayerBucketEntityEvent callPlayerFishBucketEvent(net.minecraft.world.entity.LivingEntity fish, net.minecraft.world.entity.player.Player entityHuman, ItemStack originalBucket, ItemStack entityBucket, InteractionHand enumhand) {
        Player player = (Player) entityHuman.getBukkitEntity();
        EquipmentSlot hand = CraftEquipmentSlot.getHand(enumhand);

        PlayerBucketEntityEvent event;
        if (fish instanceof AbstractFish) {
            event = new PlayerBucketFishEvent(player, (Fish) fish.getBukkitEntity(), CraftItemStack.asBukkitCopy(originalBucket), CraftItemStack.asBukkitCopy(entityBucket), hand);
        } else {
            event = new PlayerBucketEntityEvent(player, fish.getBukkitEntity(), CraftItemStack.asBukkitCopy(originalBucket), CraftItemStack.asBukkitCopy(entityBucket), hand);
        }
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Trade Index Change Event
     */
    public static TradeSelectEvent callTradeSelectEvent(ServerPlayer player, int newIndex, MerchantMenu merchant) {
        TradeSelectEvent tradeSelectEvent = new TradeSelectEvent(merchant.getBukkitView(), newIndex);
        Bukkit.getPluginManager().callEvent(tradeSelectEvent);
        return tradeSelectEvent;
    }

    @SuppressWarnings("deprecation") // Paper use deprecated event to maintain compat (it extends modern event)
    public static boolean handleBellRingEvent(Level world, BlockPos position, Direction direction, Entity entity) {
        Block block = CraftBlock.at(world, position);
        BlockFace bukkitDirection = CraftBlock.notchToBlockFace(direction);
        BellRingEvent event = new io.papermc.paper.event.block.BellRingEvent(block, bukkitDirection, (entity != null) ? entity.getBukkitEntity() : null); // Paper - deprecated BellRingEvent
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static Stream<net.minecraft.world.entity.LivingEntity> handleBellResonateEvent(Level world, BlockPos position, List<LivingEntity> bukkitEntities) {
        Block block = CraftBlock.at(world, position);
        BellResonateEvent event = new BellResonateEvent(block, bukkitEntities);
        Bukkit.getPluginManager().callEvent(event);
        return event.getResonatedEntities().stream().map((bukkitEntity) -> ((CraftLivingEntity) bukkitEntity).getHandle());
    }

    /**
     * Block place methods
     */
    public static BlockMultiPlaceEvent callBlockMultiPlaceEvent(ServerLevel world, net.minecraft.world.entity.player.Player who, InteractionHand hand, List<BlockState> blockStates, int clickedX, int clickedY, int clickedZ) {
        CraftWorld craftWorld = world.getWorld();
        CraftServer craftServer = world.getCraftServer();
        Player player = (Player) who.getBukkitEntity();

        Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);

        boolean canBuild = true;
        for (int i = 0; i < blockStates.size(); i++) {
            if (!CraftEventFactory.canBuild(world, player, blockStates.get(i).getX(), blockStates.get(i).getZ())) {
                canBuild = false;
                break;
            }
        }

        org.bukkit.inventory.ItemStack item;
        // Paper start - add hand to BlockMultiPlaceEvent
        EquipmentSlot equipmentSlot;
        if (hand == InteractionHand.MAIN_HAND) {
            item = player.getInventory().getItemInMainHand();
            equipmentSlot = EquipmentSlot.HAND;
        } else {
            item = player.getInventory().getItemInOffHand();
            equipmentSlot = EquipmentSlot.OFF_HAND;
        }

        BlockMultiPlaceEvent event = new BlockMultiPlaceEvent(blockStates, blockClicked, item, player, canBuild, equipmentSlot);
        // Paper end
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static BlockPlaceEvent callBlockPlaceEvent(ServerLevel world, net.minecraft.world.entity.player.Player who, InteractionHand hand, BlockState replacedBlockState, int clickedX, int clickedY, int clickedZ) {
        CraftWorld craftWorld = world.getWorld();
        CraftServer craftServer = world.getCraftServer();

        Player player = (Player) who.getBukkitEntity();

        Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
        Block placedBlock = replacedBlockState.getBlock();

        boolean canBuild = CraftEventFactory.canBuild(world, player, placedBlock.getX(), placedBlock.getZ());

        org.bukkit.inventory.ItemStack item;
        EquipmentSlot equipmentSlot;
        if (hand == InteractionHand.MAIN_HAND) {
            item = player.getInventory().getItemInMainHand();
            equipmentSlot = EquipmentSlot.HAND;
        } else {
            item = player.getInventory().getItemInOffHand();
            equipmentSlot = EquipmentSlot.OFF_HAND;
        }

        BlockPlaceEvent event = new BlockPlaceEvent(placedBlock, replacedBlockState, blockClicked, item, player, canBuild, equipmentSlot);
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    public static void handleBlockDropItemEvent(Block block, BlockState state, ServerPlayer player, List<ItemEntity> items) {
        // Paper start
        List<Item> list = new ArrayList<>();
        for (ItemEntity item : items) {
            list.add((Item) item.getBukkitEntity());
        }
        BlockDropItemEvent event = new BlockDropItemEvent(block, state, player.getBukkitEntity(), list);
        // Paper end
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            // Paper start
            for (Item bukkit : list) {
                if (!bukkit.isValid()) {
                    Entity item = ((org.bukkit.craftbukkit.v1_20_R1.entity.CraftItem) bukkit).getHandle();
                    item.level().addFreshEntity(item);
                }
            }
        } else {
            for (Item bukkit : list) {
                if (bukkit.isValid()) {
                    bukkit.remove();
                }
            }
            // Paper end
        }
    }

    public static EntityPlaceEvent callEntityPlaceEvent(UseOnContext itemactioncontext, Entity entity) {
        return CraftEventFactory.callEntityPlaceEvent(itemactioncontext.getLevel(), itemactioncontext.getClickedPos(), itemactioncontext.getClickedFace(), itemactioncontext.getPlayer(), entity, itemactioncontext.getHand());
    }

    public static EntityPlaceEvent callEntityPlaceEvent(Level world, BlockPos clickPosition, Direction clickedFace, net.minecraft.world.entity.player.Player human, Entity entity, InteractionHand enumhand) {
        Player who = (human == null) ? null : (Player) human.getBukkitEntity();
        org.bukkit.block.Block blockClicked = CraftBlock.at(world, clickPosition);
        org.bukkit.block.BlockFace blockFace = org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock.notchToBlockFace(clickedFace);

        EntityPlaceEvent event = new EntityPlaceEvent(entity.getBukkitEntity(), who, blockClicked, blockFace, CraftEquipmentSlot.getHand(enumhand));
        entity.level().getCraftServer().getPluginManager().callEvent(event);

        return event;
    }

    /**
     * Bucket methods
     */
    public static PlayerBucketEmptyEvent callPlayerBucketEmptyEvent(ServerLevel world, net.minecraft.world.entity.player.Player who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemInHand, InteractionHand enumhand) {
        return (PlayerBucketEmptyEvent) CraftEventFactory.getPlayerBucketEvent(false, world, who, changed, clicked, clickedFace, itemInHand, Items.BUCKET, enumhand);
    }

    public static PlayerBucketFillEvent callPlayerBucketFillEvent(ServerLevel world, net.minecraft.world.entity.player.Player who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemInHand, net.minecraft.world.item.Item bucket, InteractionHand enumhand) {
        return (PlayerBucketFillEvent) CraftEventFactory.getPlayerBucketEvent(true, world, who, clicked, changed, clickedFace, itemInHand, bucket, enumhand);
    }

    private static PlayerEvent getPlayerBucketEvent(boolean isFilling, ServerLevel world, net.minecraft.world.entity.player.Player who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemstack, net.minecraft.world.item.Item item, InteractionHand enumhand) {
        Player player = (Player) who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asNewCraftStack(item);
        Material bucket = CraftMagicNumbers.getMaterial(itemstack.getItem());

        CraftServer craftServer = (CraftServer) player.getServer();

        Block block = CraftBlock.at(world, changed);
        Block blockClicked = CraftBlock.at(world, clicked);
        BlockFace blockFace = CraftBlock.notchToBlockFace(clickedFace);
        EquipmentSlot hand = CraftEquipmentSlot.getHand(enumhand);

        PlayerEvent event;
        if (isFilling) {
            event = new PlayerBucketFillEvent(player, block, blockClicked, blockFace, bucket, itemInHand, hand);
            ((PlayerBucketFillEvent) event).setCancelled(!CraftEventFactory.canBuild(world, player, changed.getX(), changed.getZ()));
        } else {
            event = new PlayerBucketEmptyEvent(player, block, blockClicked, blockFace, bucket, itemInHand, hand);
            ((PlayerBucketEmptyEvent) event).setCancelled(!CraftEventFactory.canBuild(world, player, changed.getX(), changed.getZ()));
        }

        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * Player Interact event
     */
    public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.world.entity.player.Player who, Action action, ItemStack itemstack, InteractionHand hand) {
        if (action != Action.LEFT_CLICK_AIR && action != Action.RIGHT_CLICK_AIR) {
            throw new AssertionError(String.format("%s performing %s with %s", who, action, itemstack));
        }
        return CraftEventFactory.callPlayerInteractEvent(who, action, null, Direction.SOUTH, itemstack, hand);
    }

    public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.world.entity.player.Player who, Action action, BlockPos position, Direction direction, ItemStack itemstack, InteractionHand hand) {
        return CraftEventFactory.callPlayerInteractEvent(who, action, position, direction, itemstack, false, hand, null);
    }


    // Paper start - cancelledItem param
    public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.world.entity.player.Player who, Action action, BlockPos position, Direction direction, ItemStack itemstack, boolean cancelledBlock, InteractionHand hand, Vec3 targetPos) {
        return CraftEventFactory.callPlayerInteractEvent(who, action, position, direction, itemstack, cancelledBlock, false, hand, targetPos);
    }
    public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.world.entity.player.Player who, Action action, BlockPos position, Direction direction, ItemStack itemstack, boolean cancelledBlock, boolean cancelledItem, InteractionHand hand, Vec3 targetPos) {
        // Paper end - cancelledItem param
        Player player = (who == null) ? null : (Player) who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

        Vector clickedPos = null;
        if (position != null && targetPos != null) {
            clickedPos = CraftVector.toBukkit(targetPos.subtract(Vec3.atLowerCornerOf(position)));
        }

        CraftWorld craftWorld = (CraftWorld) player.getWorld();
        CraftServer craftServer = (CraftServer) player.getServer();

        Block blockClicked = null;
        if (position != null) {
            blockClicked = craftWorld.getBlockAt(position.getX(), position.getY(), position.getZ());
        } else {
            switch (action) {
                case LEFT_CLICK_BLOCK:
                    action = Action.LEFT_CLICK_AIR;
                    break;
                case RIGHT_CLICK_BLOCK:
                    action = Action.RIGHT_CLICK_AIR;
                    break;
            }
        }
        BlockFace blockFace = CraftBlock.notchToBlockFace(direction);

        if (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0) {
            itemInHand = null;
        }

        PlayerInteractEvent event = new PlayerInteractEvent(player, action, itemInHand, blockClicked, blockFace, (hand == null) ? null : ((hand == InteractionHand.OFF_HAND) ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND), clickedPos);
        if (cancelledBlock) {
            event.setUseInteractedBlock(Event.Result.DENY);
        }
        // Paper start
        if (cancelledItem) {
            event.setUseItemInHand(Result.DENY);
        }
        // Paper end
        craftServer.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * EntityTransformEvent
     */
    public static EntityTransformEvent callEntityTransformEvent(net.minecraft.world.entity.LivingEntity original, net.minecraft.world.entity.LivingEntity coverted, EntityTransformEvent.TransformReason transformReason) {
        return CraftEventFactory.callEntityTransformEvent(original, Collections.singletonList(coverted), transformReason);
    }

    /**
     * EntityTransformEvent
     */
    public static EntityTransformEvent callEntityTransformEvent(net.minecraft.world.entity.LivingEntity original, List<net.minecraft.world.entity.LivingEntity> convertedList, EntityTransformEvent.TransformReason convertType) {
        List<org.bukkit.entity.Entity> list = new ArrayList<>();
        for (net.minecraft.world.entity.LivingEntity entityLiving : convertedList) {
            list.add(entityLiving.getBukkitEntity());
        }

        EntityTransformEvent event = new EntityTransformEvent(original.getBukkitEntity(), list, convertType);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * EntityShootBowEvent
     */
    public static EntityShootBowEvent callEntityShootBowEvent(net.minecraft.world.entity.LivingEntity who, ItemStack bow, ItemStack consumableItem, Entity entityArrow, InteractionHand hand, float force, boolean consumeItem) {
        LivingEntity shooter = (LivingEntity) who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(bow);
        CraftItemStack itemConsumable = CraftItemStack.asCraftMirror(consumableItem);
        org.bukkit.entity.Entity arrow = entityArrow.getBukkitEntity();
        EquipmentSlot handSlot = (hand == InteractionHand.MAIN_HAND) ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;

        if (itemInHand != null && (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0)) {
            itemInHand = null;
        }

        EntityShootBowEvent event = new EntityShootBowEvent(shooter, itemInHand, itemConsumable, arrow, handSlot, force, consumeItem);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * VillagerCareerChangeEvent
     */
    public static VillagerCareerChangeEvent callVillagerCareerChangeEvent(net.minecraft.world.entity.npc.Villager vilager, Profession future, VillagerCareerChangeEvent.ChangeReason reason) {
        VillagerCareerChangeEvent event = new VillagerCareerChangeEvent((Villager) vilager.getBukkitEntity(), future, reason);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    /**
     * BlockDamageEvent
     */
    public static BlockDamageEvent callBlockDamageEvent(ServerPlayer who, BlockPos pos, Direction direction, ItemStack itemstack, boolean instaBreak) { // Paper - Expose BlockFace
        Player player = who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

        Block blockClicked = CraftBlock.at(who.level(), pos);

        BlockDamageEvent event = new BlockDamageEvent(player, blockClicked, CraftBlock.notchToBlockFace(direction), itemInHand, instaBreak); // Paper - Expose BlockFace
        player.getServer().getPluginManager().callEvent(event);

        return event;
    }

    public static BlockDamageAbortEvent callBlockDamageAbortEvent(ServerPlayer who, BlockPos pos, ItemStack itemstack) {
        Player player = who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);

        Block blockClicked = CraftBlock.at(who.level(), pos);

        BlockDamageAbortEvent event = new BlockDamageAbortEvent(player, blockClicked, itemInHand);
        player.getServer().getPluginManager().callEvent(event);

        return event;
    }

    public static boolean doEntityAddEventCalling(Level world, Entity entity, SpawnReason spawnReason) {
        if (entity == null) return false;

        org.bukkit.event.Cancellable event = null;
        if (entity instanceof net.minecraft.world.entity.LivingEntity && !(entity instanceof ServerPlayer)) {
            boolean isAnimal = entity instanceof Animal || entity instanceof WaterAnimal || entity instanceof AbstractGolem;
            boolean isMonster = entity instanceof Monster || entity instanceof Ghast || entity instanceof Slime;
            boolean isNpc = entity instanceof Npc;

            if (spawnReason != SpawnReason.CUSTOM) {
                if (isAnimal && !world.getWorld().getAllowAnimals() || isMonster && !world.getWorld().getAllowMonsters() || isNpc && !world.getCraftServer().getServer().areNpcsEnabled()) {
                    entity.discard();
                    return false;
                }
            }

            event = CraftEventFactory.callCreatureSpawnEvent((net.minecraft.world.entity.LivingEntity) entity, spawnReason);
        } else if (entity instanceof ItemEntity) {
            event = CraftEventFactory.callItemSpawnEvent((ItemEntity) entity);
        } else if (entity.getBukkitEntity() instanceof org.bukkit.entity.Projectile) {
            // Not all projectiles extend EntityProjectile, so check for Bukkit interface instead
            event = CraftEventFactory.callProjectileLaunchEvent(entity);
        } else if (entity.getBukkitEntity() instanceof org.bukkit.entity.Vehicle) {
            event = CraftEventFactory.callVehicleCreateEvent(entity);
        } else if (entity.getBukkitEntity() instanceof org.bukkit.entity.LightningStrike) {
            LightningStrikeEvent.Cause cause = LightningStrikeEvent.Cause.UNKNOWN;
            switch (spawnReason) {
                case COMMAND:
                    cause = LightningStrikeEvent.Cause.COMMAND;
                    break;
                case CUSTOM:
                    cause = LightningStrikeEvent.Cause.CUSTOM;
                    break;
                case SPAWNER:
                    cause = LightningStrikeEvent.Cause.SPAWNER;
                    break;
            }
            // This event is called in nms-patches for common causes like Weather, Trap or Trident (SpawnReason.DEFAULT) then can ignore this cases for avoid two calls to this event
            if (cause == LightningStrikeEvent.Cause.UNKNOWN && spawnReason == SpawnReason.DEFAULT) {
                return true;
            }
            event = CraftEventFactory.callLightningStrikeEvent((LightningStrike) entity.getBukkitEntity(), cause);
        // Spigot start
        } else if (entity instanceof net.minecraft.world.entity.ExperienceOrb) {
            net.minecraft.world.entity.ExperienceOrb xp = (net.minecraft.world.entity.ExperienceOrb) entity;
            double radius = world.spigotConfig.expMerge;
            // Paper start - Call EntitySpawnEvent for ExperienceOrb entities.
            event = CraftEventFactory.callEntitySpawnEvent(entity);
            if (radius > 0 && !event.isCancelled() && !entity.isRemoved()) {
                // Paper end
                // Paper start - Maximum exp value when merging - Whole section has been tweaked, see comments for specifics
                final int maxValue = world.paperConfig().entities.behavior.experienceMergeMaxValue;
                final boolean mergeUnconditionally = world.paperConfig().entities.behavior.experienceMergeMaxValue <= 0;
                if (mergeUnconditionally || xp.value < maxValue) { // Paper - Skip iteration if unnecessary

                List<Entity> entities = world.getEntities(entity, entity.getBoundingBox().inflate(radius, radius, radius));
                for (Entity e : entities) {
                    if (e instanceof net.minecraft.world.entity.ExperienceOrb) {
                        net.minecraft.world.entity.ExperienceOrb loopItem = (net.minecraft.world.entity.ExperienceOrb) e;
                        // Paper start
                        if (!loopItem.isRemoved() && !(maxValue > 0 && loopItem.value >= maxValue) && new com.destroystokyo.paper.event.entity.ExperienceOrbMergeEvent((org.bukkit.entity.ExperienceOrb) entity.getBukkitEntity(), (org.bukkit.entity.ExperienceOrb) loopItem.getBukkitEntity()).callEvent()) { // Paper - ExperienceOrbMergeEvent
                            long newTotal = (long)xp.value + (long)loopItem.value;
                            if ((int) newTotal < 0) continue; // Overflow
                            if (maxValue > 0 && newTotal > (long)maxValue) {
                                loopItem.value = (int) (newTotal - maxValue);
                                xp.value = maxValue;
                            } else {
                            xp.value += loopItem.value;
                            loopItem.discard();
                            } // Paper end
                        }
                    }
                }
                } // Paper end - End iteration skip check - All tweaking ends here
            }
        // Spigot end
        } else if (!(entity instanceof ServerPlayer)) {
            event = CraftEventFactory.callEntitySpawnEvent(entity);
        }

        if (event != null && (event.isCancelled() || entity.isRemoved())) {
            Entity vehicle = entity.getVehicle();
            if (vehicle != null) {
                vehicle.discard();
            }
            for (Entity passenger : entity.getIndirectPassengers()) {
                passenger.discard();
            }
            entity.discard();
            return false;
        }

        return true;
    }

    /**
     * EntitySpawnEvent
     */
    public static EntitySpawnEvent callEntitySpawnEvent(Entity entity) {
        org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();

        EntitySpawnEvent event = new EntitySpawnEvent(bukkitEntity);
        bukkitEntity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * CreatureSpawnEvent
     */
    public static CreatureSpawnEvent callCreatureSpawnEvent(net.minecraft.world.entity.LivingEntity entityliving, SpawnReason spawnReason) {
        LivingEntity entity = (LivingEntity) entityliving.getBukkitEntity();
        CraftServer craftServer = (CraftServer) entity.getServer();

        CreatureSpawnEvent event = new CreatureSpawnEvent(entity, spawnReason);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * EntityTameEvent
     */
    public static EntityTameEvent callEntityTameEvent(Mob entity, net.minecraft.world.entity.player.Player tamer) {
        org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();
        org.bukkit.entity.AnimalTamer bukkitTamer = (tamer != null ? tamer.getBukkitEntity() : null);
        CraftServer craftServer = (CraftServer) bukkitEntity.getServer();

        EntityTameEvent event = new EntityTameEvent((LivingEntity) bukkitEntity, bukkitTamer);
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * ItemSpawnEvent
     */
    public static ItemSpawnEvent callItemSpawnEvent(ItemEntity entityitem) {
        org.bukkit.entity.Item entity = (org.bukkit.entity.Item) entityitem.getBukkitEntity();
        CraftServer craftServer = (CraftServer) entity.getServer();

        ItemSpawnEvent event = new ItemSpawnEvent(entity);

        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * ItemDespawnEvent
     */
    public static ItemDespawnEvent callItemDespawnEvent(ItemEntity entityitem) {
        org.bukkit.entity.Item entity = (org.bukkit.entity.Item) entityitem.getBukkitEntity();

        ItemDespawnEvent event = new ItemDespawnEvent(entity, entity.getLocation());

        entity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * ItemMergeEvent
     */
    public static boolean callItemMergeEvent(ItemEntity merging, ItemEntity mergingWith) {
        org.bukkit.entity.Item entityMerging = (org.bukkit.entity.Item) merging.getBukkitEntity();
        org.bukkit.entity.Item entityMergingWith = (org.bukkit.entity.Item) mergingWith.getBukkitEntity();

        ItemMergeEvent event = new ItemMergeEvent(entityMerging, entityMergingWith);

        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    /**
     * PotionSplashEvent
     */
    public static PotionSplashEvent callPotionSplashEvent(net.minecraft.world.entity.projectile.ThrownPotion potion, Map<LivingEntity, Double> affectedEntities) {
        ThrownPotion thrownPotion = (ThrownPotion) potion.getBukkitEntity();

        PotionSplashEvent event = new PotionSplashEvent(thrownPotion, affectedEntities);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static LingeringPotionSplashEvent callLingeringPotionSplashEvent(net.minecraft.world.entity.projectile.ThrownPotion potion, net.minecraft.world.entity.AreaEffectCloud cloud) {
        ThrownPotion thrownPotion = (ThrownPotion) potion.getBukkitEntity();
        AreaEffectCloud effectCloud = (AreaEffectCloud) cloud.getBukkitEntity();

        LingeringPotionSplashEvent event = new LingeringPotionSplashEvent(thrownPotion, effectCloud);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * BlockFadeEvent
     */
    public static BlockFadeEvent callBlockFadeEvent(LevelAccessor world, BlockPos pos, net.minecraft.world.level.block.state.BlockState newBlock) {
        CraftBlockState state = CraftBlockStates.getBlockState(world, pos);
        state.setData(newBlock);

        BlockFadeEvent event = new BlockFadeEvent(state.getBlock(), state);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handleMoistureChangeEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState newBlock, int flag) {
        CraftBlockState state = CraftBlockStates.getBlockState(world, pos, flag);
        state.setData(newBlock);

        MoistureChangeEvent event = new MoistureChangeEvent(state.getBlock(), state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }
        return !event.isCancelled();
    }

    public static boolean handleBlockSpreadEvent(Level world, BlockPos source, BlockPos target, net.minecraft.world.level.block.state.BlockState block) {
        return CraftEventFactory.handleBlockSpreadEvent(world, source, target, block, 2);
    }

    public static BlockPos sourceBlockOverride = null; // SPIGOT-7068: Add source block override, not the most elegant way but better than passing down a BlockPosition up to five methods deep.

    public static boolean handleBlockSpreadEvent(LevelAccessor world, BlockPos source, BlockPos target, net.minecraft.world.level.block.state.BlockState block, int flag) {
        // Suppress during worldgen
        if (!(world instanceof Level)) {
            world.setBlock(target, block, flag);
            return true;
        }

        CraftBlockState state = CraftBlockStates.getBlockState(world, target, flag);
        state.setData(block);

        BlockSpreadEvent event = new BlockSpreadEvent(state.getBlock(), CraftBlock.at(world, CraftEventFactory.sourceBlockOverride != null ? CraftEventFactory.sourceBlockOverride : source), state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }
        return !event.isCancelled();
    }

    public static EntityDeathEvent callEntityDeathEvent(net.minecraft.world.entity.LivingEntity victim) {
        return CraftEventFactory.callEntityDeathEvent(victim, new ArrayList<org.bukkit.inventory.ItemStack>(0));
    }

    public static EntityDeathEvent callEntityDeathEvent(net.minecraft.world.entity.LivingEntity victim, List<org.bukkit.inventory.ItemStack> drops) {
        // Paper start
        return CraftEventFactory.callEntityDeathEvent(victim, drops, com.google.common.util.concurrent.Runnables.doNothing());
    }
    public static EntityDeathEvent callEntityDeathEvent(net.minecraft.world.entity.LivingEntity victim, List<org.bukkit.inventory.ItemStack> drops, Runnable lootCheck) {
        // Paper end
        CraftLivingEntity entity = (CraftLivingEntity) victim.getBukkitEntity();
        EntityDeathEvent event = new EntityDeathEvent(entity, drops, victim.getExpReward());
        populateFields(victim, event); // Paper - make cancellable
        CraftWorld world = (CraftWorld) entity.getWorld();
        Bukkit.getServer().getPluginManager().callEvent(event);

        // Paper start - make cancellable
        if (event.isCancelled()) {
            return event;
        }
        playDeathSound(victim, event);
        // Paper end
        victim.expToDrop = event.getDroppedExp();
        lootCheck.run(); // Paper - advancement triggers before destroying items

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0) continue;

            world.dropItem(entity.getLocation(), stack); // Paper - note: dropItem already clones due to this being bukkit -> NMS
            if (stack instanceof CraftItemStack) stack.setAmount(0); // Paper - destroy this item - if this ever leaks due to game bugs, ensure it doesn't dupe, but don't nuke bukkit stacks of manually added items
        }

        return event;
    }

    public static PlayerDeathEvent callPlayerDeathEvent(ServerPlayer victim, List<org.bukkit.inventory.ItemStack> drops, net.kyori.adventure.text.Component deathMessage, String stringDeathMessage, boolean keepInventory) { // Paper - Adventure
        CraftPlayer entity = victim.getBukkitEntity();
        PlayerDeathEvent event = new PlayerDeathEvent(entity, drops, victim.getExpReward(), 0, deathMessage, stringDeathMessage); // Paper - Adventure
        event.setKeepInventory(keepInventory);
        event.setKeepLevel(victim.keepLevel); // SPIGOT-2222: pre-set keepLevel
        populateFields(victim, event); // Paper - make cancellable
        org.bukkit.World world = entity.getWorld();
        Bukkit.getServer().getPluginManager().callEvent(event);
        // Paper start - make cancellable
        if (event.isCancelled()) {
            return event;
        }
        playDeathSound(victim, event);
        // Paper end

        victim.keepLevel = event.getKeepLevel();
        victim.newLevel = event.getNewLevel();
        victim.newTotalExp = event.getNewTotalExp();
        victim.expToDrop = event.getDroppedExp();
        victim.newExp = event.getNewExp();

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR) continue;

            world.dropItem(entity.getLocation(), stack);
        }

        return event;
    }

    // Paper start - helper methods for making death event cancellable
    // Add information to death event
    private static void populateFields(net.minecraft.world.entity.LivingEntity victim, EntityDeathEvent event) {
        event.setReviveHealth(event.getEntity().getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
        event.setShouldPlayDeathSound(!victim.silentDeath && !victim.isSilent());
        net.minecraft.sounds.SoundEvent soundEffect = victim.getDeathSound();
        event.setDeathSound(soundEffect != null ? org.bukkit.craftbukkit.v1_20_R1.CraftSound.getBukkit(soundEffect) : null);
        event.setDeathSoundCategory(org.bukkit.SoundCategory.valueOf(victim.getSoundSource().name()));
        event.setDeathSoundVolume(victim.getSoundVolume());
        event.setDeathSoundPitch(victim.getVoicePitch());
    }

    // Play death sound manually
    private static void playDeathSound(net.minecraft.world.entity.LivingEntity victim, EntityDeathEvent event) {
        if (event.shouldPlayDeathSound() && event.getDeathSound() != null && event.getDeathSoundCategory() != null) {
            net.minecraft.world.entity.player.Player source = victim instanceof net.minecraft.world.entity.player.Player ? (net.minecraft.world.entity.player.Player) victim : null;
            double x = event.getEntity().getLocation().getX();
            double y = event.getEntity().getLocation().getY();
            double z = event.getEntity().getLocation().getZ();
            net.minecraft.sounds.SoundEvent soundEffect = org.bukkit.craftbukkit.v1_20_R1.CraftSound.getSoundEffect(event.getDeathSound());
            net.minecraft.sounds.SoundSource soundCategory = net.minecraft.sounds.SoundSource.valueOf(event.getDeathSoundCategory().name());
            victim.level().playSound(source, x, y, z, soundEffect, soundCategory, event.getDeathSoundVolume(), event.getDeathSoundPitch());
        }
    }
    // Paper end
    /**
     * Server methods
     */
    public static ServerListPingEvent callServerListPingEvent(Server craftServer, InetAddress address, String motd, int numPlayers, int maxPlayers) {
        ServerListPingEvent event = new ServerListPingEvent("", address, craftServer.motd(), numPlayers, maxPlayers); // Paper - Adventure
        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    private static EntityDamageEvent handleEntityDamageEvent(Entity entity, DamageSource source, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions) {
        return CraftEventFactory.handleEntityDamageEvent(entity, source, modifiers, modifierFunctions, false);
    }

    private static EntityDamageEvent handleEntityDamageEvent(Entity entity, DamageSource source, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions, boolean cancelled) {
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            DamageCause damageCause;
            Entity damager = CraftEventFactory.entityDamage;
            CraftEventFactory.entityDamage = null;
            EntityDamageEvent event;
            if (damager == null) {
                event = new EntityDamageByBlockEvent(null, entity.getBukkitEntity(), DamageCause.BLOCK_EXPLOSION, modifiers, modifierFunctions, source.explodedBlockState); // Paper - handle block state in damage
            } else if (entity instanceof EnderDragon && /*PAIL FIXME ((EntityEnderDragon) entity).target == damager*/ false) {
                event = new EntityDamageEvent(entity.getBukkitEntity(), DamageCause.ENTITY_EXPLOSION, modifiers, modifierFunctions);
            } else {
                if (damager instanceof org.bukkit.entity.TNTPrimed) {
                    damageCause = DamageCause.BLOCK_EXPLOSION;
                } else {
                    damageCause = DamageCause.ENTITY_EXPLOSION;
                }
                event = new EntityDamageByEntityEvent(damager.getBukkitEntity(), entity.getBukkitEntity(), damageCause, modifiers, modifierFunctions, source.isCritical()); // Paper - add critical damage API
            }
            event.setCancelled(cancelled);

            CraftEventFactory.callEvent(event);

            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            } else {
                entity.lastDamageCancelled = true; // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Keep track if the event was canceled
            }
            return event;
        } else if (source.getEntity() != null || source.getDirectEntity() != null) {
            Entity damager = source.getEntity();
            DamageCause cause = (source.isSweep()) ? DamageCause.ENTITY_SWEEP_ATTACK : DamageCause.ENTITY_ATTACK;

            if (source.isIndirect() && source.getDirectEntity() != null) {
                damager = source.getDirectEntity();
            }

            if (damager instanceof net.minecraft.world.entity.projectile.Projectile) {
                if (damager.getBukkitEntity() instanceof ThrownPotion) {
                    cause = DamageCause.MAGIC;
                } else if (damager.getBukkitEntity() instanceof Projectile) {
                    cause = DamageCause.PROJECTILE;
                }
            } else if (source.is(DamageTypes.THORNS)) {
                cause = DamageCause.THORNS;
            } else if (source.is(DamageTypes.SONIC_BOOM)) {
                cause = DamageCause.SONIC_BOOM;
            }
            // Paper start - fix handle of Falling Blocks
            else if (source.is(DamageTypes.FALLING_STALACTITE) || source.is(DamageTypes.FALLING_BLOCK) || source.is(DamageTypes.FALLING_ANVIL)) {
                cause = DamageCause.FALLING_BLOCK;
            }
            // Paper end

            return CraftEventFactory.callEntityDamageEvent(damager, entity, cause, modifiers, modifierFunctions, cancelled, source.isCritical()); // Paper - add critical damage API
        } else if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            EntityDamageEvent event = new EntityDamageByBlockEvent(null, entity.getBukkitEntity(), DamageCause.VOID, modifiers, modifierFunctions);
            event.setCancelled(cancelled);
            CraftEventFactory.callEvent(event);
            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            } else {
                entity.lastDamageCancelled = true; // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Keep track if the event was canceled
            }
            return event;
        } else if (source.is(DamageTypes.LAVA)) {
            EntityDamageEvent event = (new EntityDamageByBlockEvent(CraftEventFactory.blockDamage, entity.getBukkitEntity(), DamageCause.LAVA, modifiers, modifierFunctions));
            event.setCancelled(cancelled);

            Block damager = CraftEventFactory.blockDamage;
            CraftEventFactory.blockDamage = null; // SPIGOT-6639: Clear blockDamage to allow other entity damage during event call
            CraftEventFactory.callEvent(event);
            CraftEventFactory.blockDamage = damager; // SPIGOT-6639: Re-set blockDamage so that other entities which are also getting damaged have the right cause

            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            } else {
                entity.lastDamageCancelled = true; // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Keep track if the event was canceled
            }
            return event;
        } else if (CraftEventFactory.blockDamage != null) {
            DamageCause cause = null;
            Block damager = CraftEventFactory.blockDamage;
            if (source.is(DamageTypes.CACTUS) || source.is(DamageTypes.SWEET_BERRY_BUSH) || source.is(DamageTypes.STALAGMITE) || source.is(DamageTypes.FALLING_STALACTITE) || source.is(DamageTypes.FALLING_ANVIL)) {
                cause = DamageCause.CONTACT;
            } else if (source.is(DamageTypes.HOT_FLOOR)) {
                cause = DamageCause.HOT_FLOOR;
            } else if (source.is(DamageTypes.MAGIC)) {
                cause = DamageCause.MAGIC;
            } else if (source.is(DamageTypes.IN_FIRE)) {
                cause = DamageCause.FIRE;
            } else {
                throw new IllegalStateException(String.format("Unhandled damage of %s by %s from %s", entity, damager, source.getMsgId()));
            }
            EntityDamageEvent event = new EntityDamageByBlockEvent(damager, entity.getBukkitEntity(), cause, modifiers, modifierFunctions);
            event.setCancelled(cancelled);

            CraftEventFactory.blockDamage = null; // SPIGOT-6639: Clear blockDamage to allow other entity damage during event call
            CraftEventFactory.callEvent(event);
            CraftEventFactory.blockDamage = damager; // SPIGOT-6639: Re-set blockDamage so that other entities which are also getting damaged have the right cause

            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            } else {
                entity.lastDamageCancelled = true; // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Keep track if the event was canceled
            }
            return event;
        } else if (CraftEventFactory.entityDamage != null) {
            DamageCause cause = null;
            CraftEntity damager = CraftEventFactory.entityDamage.getBukkitEntity();
            CraftEventFactory.entityDamage = null;
            if (source.is(DamageTypes.FALLING_STALACTITE) || source.is(DamageTypes.FALLING_BLOCK) || source.is(DamageTypes.FALLING_ANVIL)) {
                cause = DamageCause.FALLING_BLOCK;
            } else if (damager instanceof LightningStrike) {
                cause = DamageCause.LIGHTNING;
            } else if (source.is(DamageTypes.FALL)) {
                cause = DamageCause.FALL;
            } else if (source.is(DamageTypes.DRAGON_BREATH)) {
                cause = DamageCause.DRAGON_BREATH;
            } else if (source.is(DamageTypes.MAGIC)) {
                cause = DamageCause.MAGIC;
            } else {
                throw new IllegalStateException(String.format("Unhandled damage of %s by %s from %s", entity, damager.getHandle(), source.getMsgId()));
            }
            EntityDamageEvent event = new EntityDamageByEntityEvent(damager, entity.getBukkitEntity(), cause, modifiers, modifierFunctions, source.isCritical()); // Paper - add critical damage API
            event.setCancelled(cancelled);
            CraftEventFactory.callEvent(event);
            if (!event.isCancelled()) {
                event.getEntity().setLastDamageCause(event);
            } else {
                entity.lastDamageCancelled = true; // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Keep track if the event was canceled
            }
            return event;
        }

        DamageCause cause = null;
        if (source.is(DamageTypes.IN_FIRE)) {
            cause = DamageCause.FIRE;
        } else if (source.is(DamageTypes.STARVE)) {
            cause = DamageCause.STARVATION;
        } else if (source.is(DamageTypes.WITHER)) {
            cause = DamageCause.WITHER;
        } else if (source.is(DamageTypes.IN_WALL)) {
            cause = DamageCause.SUFFOCATION;
        } else if (source.is(DamageTypes.DROWN)) {
            cause = DamageCause.DROWNING;
        } else if (source.is(DamageTypes.ON_FIRE)) {
            cause = DamageCause.FIRE_TICK;
        } else if (source.isMelting()) {
            cause = DamageCause.MELTING;
        } else if (source.isPoison()) {
            cause = DamageCause.POISON;
        } else if (source.is(DamageTypes.MAGIC)) {
            cause = DamageCause.MAGIC;
        } else if (source.is(DamageTypes.FALL)) {
            cause = DamageCause.FALL;
        } else if (source.is(DamageTypes.FLY_INTO_WALL)) {
            cause = DamageCause.FLY_INTO_WALL;
        } else if (source.is(DamageTypes.CRAMMING)) {
            cause = DamageCause.CRAMMING;
        } else if (source.is(DamageTypes.DRY_OUT)) {
            cause = DamageCause.DRYOUT;
        } else if (source.is(DamageTypes.FREEZE)) {
            cause = DamageCause.FREEZE;
        } else if (source.is(DamageTypes.GENERIC_KILL)) {
            cause = DamageCause.KILL;
        } else if (source.is(DamageTypes.OUTSIDE_BORDER)) {
            cause = DamageCause.WORLD_BORDER;
        } else {
            cause = DamageCause.CUSTOM;
        }

        if (cause != null) {
            return CraftEventFactory.callEntityDamageEvent(null, entity, cause, modifiers, modifierFunctions, cancelled, source.isCritical()); // Paper - add critical damage API
        }

        throw new IllegalStateException(String.format("Unhandled damage of %s from %s", entity, source.getMsgId()));
    }

    @Deprecated // Paper - Add critical damage API
    private static EntityDamageEvent callEntityDamageEvent(Entity damager, Entity damagee, DamageCause cause, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions) {
        return CraftEventFactory.callEntityDamageEvent(damager, damagee, cause, modifiers, modifierFunctions, false);
    }

    // Paper start - Add critical damage API
    @Deprecated
    private static EntityDamageEvent callEntityDamageEvent(Entity damager, Entity damagee, DamageCause cause, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions, boolean cancelled) {
        return CraftEventFactory.callEntityDamageEvent(damager, damagee, cause, modifiers, modifierFunctions, false, false);
    }

    private static EntityDamageEvent callEntityDamageEvent(Entity damager, Entity damagee, DamageCause cause, Map<DamageModifier, Double> modifiers, Map<DamageModifier, Function<? super Double, Double>> modifierFunctions, boolean cancelled, boolean critical) {
        // Paper end
        EntityDamageEvent event;
        if (damager != null) {
            event = new EntityDamageByEntityEvent(damager.getBukkitEntity(), damagee.getBukkitEntity(), cause, modifiers, modifierFunctions, critical); // Paper - add critical damage API
        } else {
            event = new EntityDamageEvent(damagee.getBukkitEntity(), cause, modifiers, modifierFunctions);
        }
        event.setCancelled(cancelled);
        CraftEventFactory.callEvent(event);

        if (!event.isCancelled()) {
            event.getEntity().setLastDamageCause(event);
        } else {
            damagee.lastDamageCancelled = true; // SPIGOT-5339, SPIGOT-6252, SPIGOT-6777: Keep track if the event was canceled
        }

        return event;
    }

    private static final Function<? super Double, Double> ZERO = Functions.constant(-0.0);

    public static EntityDamageEvent handleLivingEntityDamageEvent(Entity damagee, DamageSource source, double rawDamage, double hardHatModifier, double blockingModifier, double armorModifier, double resistanceModifier, double magicModifier, double absorptionModifier, Function<Double, Double> hardHat, Function<Double, Double> blocking, Function<Double, Double> armor, Function<Double, Double> resistance, Function<Double, Double> magic, Function<Double, Double> absorption) {
        Map<DamageModifier, Double> modifiers = new EnumMap<>(DamageModifier.class);
        Map<DamageModifier, Function<? super Double, Double>> modifierFunctions = new EnumMap<>(DamageModifier.class);
        modifiers.put(DamageModifier.BASE, rawDamage);
        modifierFunctions.put(DamageModifier.BASE, ZERO);
        if (source.is(DamageTypes.FALLING_BLOCK) || source.is(DamageTypes.FALLING_ANVIL)) {
            modifiers.put(DamageModifier.HARD_HAT, hardHatModifier);
            modifierFunctions.put(DamageModifier.HARD_HAT, hardHat);
        }
        if (damagee instanceof net.minecraft.world.entity.player.Player) {
            modifiers.put(DamageModifier.BLOCKING, blockingModifier);
            modifierFunctions.put(DamageModifier.BLOCKING, blocking);
        }
        modifiers.put(DamageModifier.ARMOR, armorModifier);
        modifierFunctions.put(DamageModifier.ARMOR, armor);
        modifiers.put(DamageModifier.RESISTANCE, resistanceModifier);
        modifierFunctions.put(DamageModifier.RESISTANCE, resistance);
        modifiers.put(DamageModifier.MAGIC, magicModifier);
        modifierFunctions.put(DamageModifier.MAGIC, magic);
        modifiers.put(DamageModifier.ABSORPTION, absorptionModifier);
        modifierFunctions.put(DamageModifier.ABSORPTION, absorption);
        return CraftEventFactory.handleEntityDamageEvent(damagee, source, modifiers, modifierFunctions);
    }

    // Non-Living Entities such as EntityEnderCrystal and EntityFireball need to call this
    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage) {
        return CraftEventFactory.handleNonLivingEntityDamageEvent(entity, source, damage, true);
    }

    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage, boolean cancelOnZeroDamage) {
        return CraftEventFactory.handleNonLivingEntityDamageEvent(entity, source, damage, cancelOnZeroDamage, false);
    }

    public static EntityDamageEvent callNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage, boolean cancelled) {
        final EnumMap<DamageModifier, Double> modifiers = new EnumMap<DamageModifier, Double>(DamageModifier.class);
        final EnumMap<DamageModifier, Function<? super Double, Double>> functions = new EnumMap(DamageModifier.class);

        modifiers.put(DamageModifier.BASE, damage);
        functions.put(DamageModifier.BASE, ZERO);

        return CraftEventFactory.handleEntityDamageEvent(entity, source, modifiers, functions, cancelled);
    }

    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage, boolean cancelOnZeroDamage, boolean cancelled) {
        final EntityDamageEvent event = CraftEventFactory.callNonLivingEntityDamageEvent(entity, source, damage, cancelled);

        if (event == null) {
            return false;
        }
        return event.isCancelled() || (cancelOnZeroDamage && event.getDamage() == 0);
    }

    public static PlayerLevelChangeEvent callPlayerLevelChangeEvent(Player player, int oldLevel, int newLevel) {
        PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(player, oldLevel, newLevel);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerExpChangeEvent callPlayerExpChangeEvent(net.minecraft.world.entity.player.Player entity, int expAmount) {
        Player player = (Player) entity.getBukkitEntity();
        PlayerExpChangeEvent event = new PlayerExpChangeEvent(player, expAmount);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerExpCooldownChangeEvent callPlayerXpCooldownEvent(net.minecraft.world.entity.player.Player entity, int newCooldown, PlayerExpCooldownChangeEvent.ChangeReason changeReason) {
        Player player = (Player) entity.getBukkitEntity();
        PlayerExpCooldownChangeEvent event = new PlayerExpCooldownChangeEvent(player, newCooldown, changeReason);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerItemMendEvent callPlayerItemMendEvent(net.minecraft.world.entity.player.Player entity, net.minecraft.world.entity.ExperienceOrb orb, net.minecraft.world.item.ItemStack nmsMendedItem, net.minecraft.world.entity.EquipmentSlot slot, int repairAmount, java.util.function.IntUnaryOperator durabilityToXpOp) { // Paper
        Player player = (Player) entity.getBukkitEntity();
        org.bukkit.inventory.ItemStack bukkitStack = CraftItemStack.asCraftMirror(nmsMendedItem);
        PlayerItemMendEvent event = new PlayerItemMendEvent(player, bukkitStack, CraftEquipmentSlot.getSlot(slot), (ExperienceOrb) orb.getBukkitEntity(), repairAmount, durabilityToXpOp); // Paper
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    // Paper start - Add orb
    public static PlayerExpChangeEvent callPlayerExpChangeEvent(net.minecraft.world.entity.player.Player entity, net.minecraft.world.entity.ExperienceOrb entityOrb) {
        Player player = (Player) entity.getBukkitEntity();
        ExperienceOrb source = (ExperienceOrb) entityOrb.getBukkitEntity();
        int expAmount = source.getExperience();
        PlayerExpChangeEvent event = new PlayerExpChangeEvent(player, source, expAmount);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
    // Paper end

    public static boolean handleBlockGrowEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState block) {
        return CraftEventFactory.handleBlockGrowEvent(world, pos, block, 3);
    }

    public static boolean handleBlockGrowEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState newData, int flag) {
        Block block = world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        CraftBlockState state = (CraftBlockState) block.getState();
        state.setData(newData);

        BlockGrowEvent event = new BlockGrowEvent(block, state);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            state.update(true);
        }

        return !event.isCancelled();
    }

    public static FluidLevelChangeEvent callFluidLevelChangeEvent(Level world, BlockPos block, net.minecraft.world.level.block.state.BlockState newData) {
        FluidLevelChangeEvent event = new FluidLevelChangeEvent(CraftBlock.at(world, block), CraftBlockData.fromData(newData));
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static FoodLevelChangeEvent callFoodLevelChangeEvent(net.minecraft.world.entity.player.Player entity, int level) {
        return CraftEventFactory.callFoodLevelChangeEvent(entity, level, null);
    }

    public static FoodLevelChangeEvent callFoodLevelChangeEvent(net.minecraft.world.entity.player.Player entity, int level, ItemStack item) {
        FoodLevelChangeEvent event = new FoodLevelChangeEvent(entity.getBukkitEntity(), level, (item == null) ? null : CraftItemStack.asBukkitCopy(item));
        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PigZapEvent callPigZapEvent(Entity pig, Entity lightning, Entity pigzombie) {
        PigZapEvent event = new PigZapEvent((Pig) pig.getBukkitEntity(), (LightningStrike) lightning.getBukkitEntity(), (PigZombie) pigzombie.getBukkitEntity());
        pig.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static boolean callHorseJumpEvent(Entity horse, float power) {
        HorseJumpEvent event = new HorseJumpEvent((AbstractHorse) horse.getBukkitEntity(), power);
        horse.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    // Paper start
    public static com.destroystokyo.paper.event.entity.EntityZapEvent callEntityZapEvent (Entity entity, Entity lightning, Entity changedEntity) {
        com.destroystokyo.paper.event.entity.EntityZapEvent event = new com.destroystokyo.paper.event.entity.EntityZapEvent(entity.getBukkitEntity(), (LightningStrike) lightning.getBukkitEntity(), changedEntity.getBukkitEntity());
        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }
    // Paper end

    public static boolean callEntityChangeBlockEvent(Entity entity, BlockPos position, net.minecraft.world.level.block.state.BlockState newBlock) {
        return CraftEventFactory.callEntityChangeBlockEvent(entity, position, newBlock, false);
    }

    public static boolean callEntityChangeBlockEvent(Entity entity, BlockPos position, net.minecraft.world.level.block.state.BlockState newBlock, boolean cancelled) {
        Block block = entity.level().getWorld().getBlockAt(position.getX(), position.getY(), position.getZ());

        EntityChangeBlockEvent event = new EntityChangeBlockEvent(entity.getBukkitEntity(), block, CraftBlockData.fromData(newBlock));
        event.setCancelled(cancelled);
        event.getEntity().getServer().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static CreeperPowerEvent callCreeperPowerEvent(Entity creeper, Entity lightning, CreeperPowerEvent.PowerCause cause) {
        CreeperPowerEvent event = new CreeperPowerEvent((Creeper) creeper.getBukkitEntity(), (LightningStrike) lightning.getBukkitEntity(), cause);
        creeper.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTargetEvent callEntityTargetEvent(Entity entity, Entity target, EntityTargetEvent.TargetReason reason) {
        EntityTargetEvent event = new EntityTargetEvent(entity.getBukkitEntity(), (target == null) ? null : target.getBukkitEntity(), reason);
        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTargetLivingEntityEvent callEntityTargetLivingEvent(Entity entity, net.minecraft.world.entity.LivingEntity target, EntityTargetEvent.TargetReason reason) {
        EntityTargetLivingEntityEvent event = new EntityTargetLivingEntityEvent(entity.getBukkitEntity(), (target == null) ? null : (LivingEntity) target.getBukkitEntity(), reason);
        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityBreakDoorEvent callEntityBreakDoorEvent(Entity entity, BlockPos pos, net.minecraft.world.level.block.state.BlockState newState) { // Paper
        org.bukkit.entity.Entity entity1 = entity.getBukkitEntity();
        Block block = CraftBlock.at(entity.level(), pos);

        EntityBreakDoorEvent event = new EntityBreakDoorEvent((LivingEntity) entity1, block, newState.createCraftBlockData()); // Paper
        entity1.getServer().getPluginManager().callEvent(event);

        return event;
    }

    public static AbstractContainerMenu callInventoryOpenEvent(ServerPlayer player, AbstractContainerMenu container) {
        // Paper start
        return callInventoryOpenEventWithTitle(player, container).getSecond();
    }
    public static com.mojang.datafixers.util.Pair<net.kyori.adventure.text.@org.jetbrains.annotations.Nullable Component, @org.jetbrains.annotations.Nullable AbstractContainerMenu> callInventoryOpenEventWithTitle(ServerPlayer player, AbstractContainerMenu container) {
        return CraftEventFactory.callInventoryOpenEventWithTitle(player, container, false);
        // Paper end
    }

    @Deprecated @io.papermc.paper.annotation.DoNotUse // Paper - use method that acknowledges title overrides
    public static AbstractContainerMenu callInventoryOpenEvent(ServerPlayer player, AbstractContainerMenu container, boolean cancelled) {
        // Paper start
        return callInventoryOpenEventWithTitle(player, container, cancelled).getSecond();
    }
    public static com.mojang.datafixers.util.Pair<net.kyori.adventure.text.@org.jetbrains.annotations.Nullable Component, @org.jetbrains.annotations.Nullable AbstractContainerMenu> callInventoryOpenEventWithTitle(ServerPlayer player, AbstractContainerMenu container, boolean cancelled) {
        // Paper end
        if (player.containerMenu != player.inventoryMenu) { // fire INVENTORY_CLOSE if one already open
            player.connection.handleContainerClose(new ServerboundContainerClosePacket(player.containerMenu.containerId), InventoryCloseEvent.Reason.OPEN_NEW); // Paper
        }

        CraftServer server = player.level().getCraftServer();
        CraftPlayer craftPlayer = player.getBukkitEntity();
        player.containerMenu.transferTo(container, craftPlayer);

        InventoryOpenEvent event = new InventoryOpenEvent(container.getBukkitView());
        event.setCancelled(cancelled);
        server.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            container.transferTo(player.containerMenu, craftPlayer);
            return com.mojang.datafixers.util.Pair.of(null, null); // Paper - title override
        }

        return com.mojang.datafixers.util.Pair.of(event.titleOverride(), container); // Paper - title override
    }

    public static ItemStack callPreCraftEvent(Container matrix, Container resultInventory, ItemStack result, InventoryView lastCraftView, boolean isRepair) {
        CraftInventoryCrafting inventory = new CraftInventoryCrafting(matrix, resultInventory);
        inventory.setResult(CraftItemStack.asCraftMirror(result));

        PrepareItemCraftEvent event = new PrepareItemCraftEvent(inventory, lastCraftView, isRepair);
        Bukkit.getPluginManager().callEvent(event);

        org.bukkit.inventory.ItemStack bitem = event.getInventory().getResult();

        return CraftItemStack.asNMSCopy(bitem);
    }

    // Paper start
    @Deprecated
    public static com.destroystokyo.paper.event.entity.ProjectileCollideEvent callProjectileCollideEvent(Entity entity, EntityHitResult position) {
        Projectile projectile = (Projectile) entity.getBukkitEntity();
        org.bukkit.entity.Entity collided = position.getEntity().getBukkitEntity();
        com.destroystokyo.paper.event.entity.ProjectileCollideEvent event = new com.destroystokyo.paper.event.entity.ProjectileCollideEvent(projectile, collided);

        if (projectile.getShooter() instanceof Player && collided instanceof Player) {
            if (!((Player) projectile.getShooter()).canSee((Player) collided)) {
                event.setCancelled(true);
                return event;
            }
        }

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
    // Paper end

    public static ProjectileLaunchEvent callProjectileLaunchEvent(Entity entity) {
        Projectile bukkitEntity = (Projectile) entity.getBukkitEntity();
        ProjectileLaunchEvent event = new ProjectileLaunchEvent(bukkitEntity);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static ProjectileHitEvent callProjectileHitEvent(Entity entity, HitResult position) {
        if (position.getType() == HitResult.Type.MISS) {
            return null;
        }

        Block hitBlock = null;
        BlockFace hitFace = null;
        if (position.getType() == HitResult.Type.BLOCK) {
            BlockHitResult positionBlock = (BlockHitResult) position;
            hitBlock = CraftBlock.at(entity.level(), positionBlock.getBlockPos());
            hitFace = CraftBlock.notchToBlockFace(positionBlock.getDirection());
        }

        org.bukkit.entity.Entity hitEntity = null;
        if (position.getType() == HitResult.Type.ENTITY) {
            hitEntity = ((EntityHitResult) position).getEntity().getBukkitEntity();
        }
        // Paper start - legacy event
        boolean cancelled = false;
        if (hitEntity != null && position instanceof EntityHitResult entityHitResult) {
            cancelled = callProjectileCollideEvent(entity, entityHitResult).isCancelled();
        }
        // Paper end

        ProjectileHitEvent event = new ProjectileHitEvent((Projectile) entity.getBukkitEntity(), hitEntity, hitBlock, hitFace);
        event.setCancelled(cancelled); // Paper - propagate legacy event cancellation to modern event
        entity.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static ExpBottleEvent callExpBottleEvent(Entity entity, int exp) {
        ThrownExpBottle bottle = (ThrownExpBottle) entity.getBukkitEntity();
        ExpBottleEvent event = new ExpBottleEvent(bottle, exp);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockRedstoneEvent callRedstoneChange(Level world, BlockPos pos, int oldCurrent, int newCurrent) {
        BlockRedstoneEvent event = new BlockRedstoneEvent(world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), oldCurrent, newCurrent);
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static NotePlayEvent callNotePlayEvent(Level world, BlockPos pos, NoteBlockInstrument instrument, int note) {
        NotePlayEvent event = new NotePlayEvent(world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), org.bukkit.Instrument.getByType((byte) instrument.ordinal()), new org.bukkit.Note(note));
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static void callPlayerItemBreakEvent(net.minecraft.world.entity.player.Player human, ItemStack brokenItem) {
        CraftItemStack item = CraftItemStack.asCraftMirror(brokenItem);
        PlayerItemBreakEvent event = new PlayerItemBreakEvent((Player) human.getBukkitEntity(), item);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static BlockIgniteEvent callBlockIgniteEvent(Level world, BlockPos block, BlockPos source) {
        org.bukkit.World bukkitWorld = world.getWorld();
        Block igniter = bukkitWorld.getBlockAt(source.getX(), source.getY(), source.getZ());
        IgniteCause cause;
        switch (igniter.getType()) {
            case LAVA:
                cause = IgniteCause.LAVA;
                break;
            case DISPENSER:
                cause = IgniteCause.FLINT_AND_STEEL;
                break;
            case FIRE: // Fire or any other unknown block counts as SPREAD.
            default:
                cause = IgniteCause.SPREAD;
        }

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(block.getX(), block.getY(), block.getZ()), cause, igniter);
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(Level world, BlockPos pos, Entity igniter) {
        org.bukkit.World bukkitWorld = world.getWorld();
        org.bukkit.entity.Entity bukkitIgniter = igniter.getBukkitEntity();
        IgniteCause cause;
        switch (bukkitIgniter.getType()) {
            case ENDER_CRYSTAL:
                cause = IgniteCause.ENDER_CRYSTAL;
                break;
            case LIGHTNING:
                cause = IgniteCause.LIGHTNING;
                break;
            case SMALL_FIREBALL:
            case FIREBALL:
                cause = IgniteCause.FIREBALL;
                break;
            case ARROW:
                cause = IgniteCause.ARROW;
                break;
            default:
                cause = IgniteCause.FLINT_AND_STEEL;
        }

        if (igniter instanceof net.minecraft.world.entity.projectile.Projectile) {
            Entity shooter = ((net.minecraft.world.entity.projectile.Projectile) igniter).getOwner();
            if (shooter != null) {
                bukkitIgniter = shooter.getBukkitEntity();
            }
        }

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), cause, bukkitIgniter);
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(Level world, int x, int y, int z, Explosion explosion) {
        org.bukkit.World bukkitWorld = world.getWorld();
        org.bukkit.entity.Entity igniter = explosion.source == null ? null : explosion.source.getBukkitEntity();

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), IgniteCause.EXPLOSION, igniter);
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(Level world, BlockPos pos, IgniteCause cause, Entity igniter) {
        BlockIgniteEvent event = new BlockIgniteEvent(world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), cause, igniter.getBukkitEntity());
        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    // Paper start
    /**
     * Incase plugins hooked into this or Spigot adds a new inventory close event. Prefer to pass a reason
     * @param human
     */
    @Deprecated
    public static void handleInventoryCloseEvent(net.minecraft.world.entity.player.Player human) {
        handleInventoryCloseEvent(human, org.bukkit.event.inventory.InventoryCloseEvent.Reason.UNKNOWN);
    }
    public static void handleInventoryCloseEvent(net.minecraft.world.entity.player.Player human, org.bukkit.event.inventory.InventoryCloseEvent.Reason reason) {
        // Paper end
        InventoryCloseEvent event = new InventoryCloseEvent(human.containerMenu.getBukkitView(), reason); // Paper
        human.level().getCraftServer().getPluginManager().callEvent(event);
        human.containerMenu.transferTo(human.inventoryMenu, human.getBukkitEntity());
    }

    public static ItemStack handleEditBookEvent(ServerPlayer player, int itemInHandIndex, ItemStack itemInHand, ItemStack newBookItem) {
        PlayerEditBookEvent editBookEvent = new PlayerEditBookEvent(player.getBukkitEntity(), (itemInHandIndex >= 0 && itemInHandIndex <= 8) ? itemInHandIndex : -1, (BookMeta) CraftItemStack.getItemMeta(itemInHand), (BookMeta) CraftItemStack.getItemMeta(newBookItem), newBookItem.getItem() == Items.WRITTEN_BOOK);
        player.level().getCraftServer().getPluginManager().callEvent(editBookEvent);

        // If they've got the same item in their hand, it'll need to be updated.
        if (itemInHand != null && itemInHand.getItem() == Items.WRITABLE_BOOK) {
            if (!editBookEvent.isCancelled()) {
                if (editBookEvent.isSigning()) {
                    itemInHand.setItem(Items.WRITTEN_BOOK);
                }
                CraftMetaBook meta = (CraftMetaBook) editBookEvent.getNewBookMeta();
                CraftItemStack.setItemMeta(itemInHand, meta);
            }
        }

        return itemInHand;
    }

    // Paper start - drop leash variable
    public static PlayerUnleashEntityEvent callPlayerUnleashEntityEvent(Mob entity, net.minecraft.world.entity.player.Player player, InteractionHand enumhand, boolean dropLeash) {
        PlayerUnleashEntityEvent event = new PlayerUnleashEntityEvent(entity.getBukkitEntity(), (Player) player.getBukkitEntity(), CraftEquipmentSlot.getHand(enumhand), dropLeash);
        // Paper end
        entity.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerLeashEntityEvent callPlayerLeashEntityEvent(Mob entity, Entity leashHolder, net.minecraft.world.entity.player.Player player, InteractionHand enumhand) {
        PlayerLeashEntityEvent event = new PlayerLeashEntityEvent(entity.getBukkitEntity(), leashHolder.getBukkitEntity(), (Player) player.getBukkitEntity(), CraftEquipmentSlot.getHand(enumhand));
        entity.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockShearEntityEvent callBlockShearEntityEvent(Entity animal, org.bukkit.block.Block dispenser, CraftItemStack is) {
        BlockShearEntityEvent bse = new BlockShearEntityEvent(dispenser, animal.getBukkitEntity(), is);
        Bukkit.getPluginManager().callEvent(bse);
        return bse;
    }

    public static boolean handlePlayerShearEntityEvent(net.minecraft.world.entity.player.Player player, Entity sheared, ItemStack shears, InteractionHand hand) {
        if (!(player instanceof ServerPlayer)) {
            return true;
        }

        PlayerShearEntityEvent event = new PlayerShearEntityEvent((Player) player.getBukkitEntity(), sheared.getBukkitEntity(), CraftItemStack.asCraftMirror(shears), (hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND));
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static Cancellable handleStatisticsIncrease(net.minecraft.world.entity.player.Player entityHuman, net.minecraft.stats.Stat<?> statistic, int current, int newValue) {
        Player player = ((ServerPlayer) entityHuman).getBukkitEntity();
        Event event;
        if (true) {
            org.bukkit.Statistic stat = CraftStatistic.getBukkitStatistic(statistic);
            if (stat == null) {
                System.err.println("Unhandled statistic: " + statistic);
                return null;
            }
            switch (stat) {
                case FALL_ONE_CM:
                case BOAT_ONE_CM:
                case CLIMB_ONE_CM:
                case WALK_ON_WATER_ONE_CM:
                case WALK_UNDER_WATER_ONE_CM:
                case FLY_ONE_CM:
                case HORSE_ONE_CM:
                case MINECART_ONE_CM:
                case PIG_ONE_CM:
                case PLAY_ONE_MINUTE:
                case SWIM_ONE_CM:
                case WALK_ONE_CM:
                case SPRINT_ONE_CM:
                case CROUCH_ONE_CM:
                case TIME_SINCE_DEATH:
                case SNEAK_TIME:
                case TOTAL_WORLD_TIME:
                case TIME_SINCE_REST:
                case AVIATE_ONE_CM:
                case STRIDER_ONE_CM:
                    // Do not process event for these - too spammy
                    return null;
                default:
            }
            if (stat.getType() == Type.UNTYPED) {
                event = new PlayerStatisticIncrementEvent(player, stat, current, newValue);
            } else if (stat.getType() == Type.ENTITY) {
                EntityType entityType = CraftStatistic.getEntityTypeFromStatistic((net.minecraft.stats.Stat<net.minecraft.world.entity.EntityType<?>>) statistic);
                event = new PlayerStatisticIncrementEvent(player, stat, current, newValue, entityType);
            } else {
                Material material = CraftStatistic.getMaterialFromStatistic(statistic);
                event = new PlayerStatisticIncrementEvent(player, stat, current, newValue, material);
            }
        }
        entityHuman.level().getCraftServer().getPluginManager().callEvent(event);
        return (Cancellable) event;
    }

    public static FireworkExplodeEvent callFireworkExplodeEvent(FireworkRocketEntity firework) {
        FireworkExplodeEvent event = new FireworkExplodeEvent((Firework) firework.getBukkitEntity());
        firework.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    // Paper start - disable this method, handled below
    public static void callPrepareAnvilEvent(InventoryView view, ItemStack item) { // Paper - verify nothing uses return - handled below in PrepareResult
        PrepareAnvilEvent event = new PrepareAnvilEvent(view, CraftItemStack.asCraftMirror(item)); // Paper - remove clone
        //event.getView().getPlayer().getServer().getPluginManager().callEvent(event); // Paper - disable event
        event.getInventory().setItem(2, event.getResult());
        //return event; // Paper
    }
    // Paper end

    // Paper start - disable this method, handled below
    public static void callPrepareGrindstoneEvent(InventoryView view, ItemStack item) {
        PrepareGrindstoneEvent event = new PrepareGrindstoneEvent(view, CraftItemStack.asCraftMirror(item)); // Paper - remove clone
        // event.getView().getPlayer().getServer().getPluginManager().callEvent(event); // Paper - disable event
        event.getInventory().setItem(2, event.getResult());
        // return event; // Paper
    }
    // Paper end

    // Paper start - disable this method, handled in callPrepareResultEvent
    public static void callPrepareSmithingEvent(InventoryView view, ItemStack item) { // Paper - verify nothing uses return - handled below in PrepareResult
        PrepareSmithingEvent event = new PrepareSmithingEvent(view, CraftItemStack.asCraftMirror(item)); // Paper - remove clone
        //event.getView().getPlayer().getServer().getPluginManager().callEvent(event); // Paper - disable event
        event.getInventory().setResult(event.getResult());
        //return event; // Paper
    }
    // Paper end

    // Paper start - support specific overrides for prepare result
    public static void callPrepareResultEvent(AbstractContainerMenu container, int resultSlot) {
        com.destroystokyo.paper.event.inventory.PrepareResultEvent event;
        InventoryView view = container.getBukkitView();
        org.bukkit.inventory.ItemStack origItem = view.getTopInventory().getItem(resultSlot);
        CraftItemStack result = origItem != null ? CraftItemStack.asCraftCopy(origItem) : null;
        if (view.getTopInventory() instanceof org.bukkit.inventory.AnvilInventory) {
            event = new PrepareAnvilEvent(view, result);
        } else if (view.getTopInventory() instanceof org.bukkit.inventory.GrindstoneInventory) {
            event = new PrepareGrindstoneEvent(view, result);
        } else if (view.getTopInventory() instanceof org.bukkit.inventory.SmithingInventory) {
            event = new PrepareSmithingEvent(view, result);
        } else {
            event = new com.destroystokyo.paper.event.inventory.PrepareResultEvent(view, result);
        }
        event.callEvent();
        event.getInventory().setItem(resultSlot, event.getResult());
        container.broadcastChanges();;
    }
    // Paper end

    /**
     * Mob spawner event.
     */
    public static SpawnerSpawnEvent callSpawnerSpawnEvent(Entity spawnee, BlockPos pos) {
        org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity entity = spawnee.getBukkitEntity();
        BlockState state = CraftBlock.at(spawnee.level(), pos).getState();
        if (!(state instanceof org.bukkit.block.CreatureSpawner)) {
            state = null;
        }

        SpawnerSpawnEvent event = new SpawnerSpawnEvent(entity, (org.bukkit.block.CreatureSpawner) state);
        entity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityToggleGlideEvent callToggleGlideEvent(net.minecraft.world.entity.LivingEntity entity, boolean gliding) {
        EntityToggleGlideEvent event = new EntityToggleGlideEvent((LivingEntity) entity.getBukkitEntity(), gliding);
        entity.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityToggleSwimEvent callToggleSwimEvent(net.minecraft.world.entity.LivingEntity entity, boolean swimming) {
        EntityToggleSwimEvent event = new EntityToggleSwimEvent((LivingEntity) entity.getBukkitEntity(), swimming);
        entity.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static AreaEffectCloudApplyEvent callAreaEffectCloudApplyEvent(net.minecraft.world.entity.AreaEffectCloud cloud, List<LivingEntity> entities) {
        AreaEffectCloudApplyEvent event = new AreaEffectCloudApplyEvent((AreaEffectCloud) cloud.getBukkitEntity(), entities);
        cloud.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static VehicleCreateEvent callVehicleCreateEvent(Entity entity) {
        Vehicle bukkitEntity = (Vehicle) entity.getBukkitEntity();
        VehicleCreateEvent event = new VehicleCreateEvent(bukkitEntity);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityBreedEvent callEntityBreedEvent(net.minecraft.world.entity.LivingEntity child, net.minecraft.world.entity.LivingEntity mother, net.minecraft.world.entity.LivingEntity father, net.minecraft.world.entity.LivingEntity breeder, ItemStack bredWith, int experience) {
        org.bukkit.entity.LivingEntity breederEntity = (LivingEntity) (breeder == null ? null : breeder.getBukkitEntity());
        CraftItemStack bredWithStack = bredWith == null ? null : CraftItemStack.asCraftMirror(bredWith).clone();

        EntityBreedEvent event = new EntityBreedEvent((LivingEntity) child.getBukkitEntity(), (LivingEntity) mother.getBukkitEntity(), (LivingEntity) father.getBukkitEntity(), breederEntity, bredWithStack, experience);
        child.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockPhysicsEvent callBlockPhysicsEvent(LevelAccessor world, BlockPos blockposition) {
        org.bukkit.block.Block block = CraftBlock.at(world, blockposition);
        BlockPhysicsEvent event = new BlockPhysicsEvent(block, block.getBlockData());
        // Suppress during worldgen
        if (world instanceof Level) {
            ((Level) world).getServer().server.getPluginManager().callEvent(event);
        }
        return event;
    }

    public static boolean handleBlockFormEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState block) {
        return CraftEventFactory.handleBlockFormEvent(world, pos, block, 3);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(net.minecraft.world.entity.LivingEntity entity, @Nullable MobEffectInstance oldEffect, @Nullable MobEffectInstance newEffect, EntityPotionEffectEvent.Cause cause) {
        return CraftEventFactory.callEntityPotionEffectChangeEvent(entity, oldEffect, newEffect, cause, true);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(net.minecraft.world.entity.LivingEntity entity, @Nullable MobEffectInstance oldEffect, @Nullable MobEffectInstance newEffect, EntityPotionEffectEvent.Cause cause, EntityPotionEffectEvent.Action action) {
        return CraftEventFactory.callEntityPotionEffectChangeEvent(entity, oldEffect, newEffect, cause, action, true);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(net.minecraft.world.entity.LivingEntity entity, @Nullable MobEffectInstance oldEffect, @Nullable MobEffectInstance newEffect, EntityPotionEffectEvent.Cause cause, boolean willOverride) {
        EntityPotionEffectEvent.Action action = EntityPotionEffectEvent.Action.CHANGED;
        if (oldEffect == null) {
            action = EntityPotionEffectEvent.Action.ADDED;
        } else if (newEffect == null) {
            action = EntityPotionEffectEvent.Action.REMOVED;
        }

        return CraftEventFactory.callEntityPotionEffectChangeEvent(entity, oldEffect, newEffect, cause, action, willOverride);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(net.minecraft.world.entity.LivingEntity entity, @Nullable MobEffectInstance oldEffect, @Nullable MobEffectInstance newEffect, EntityPotionEffectEvent.Cause cause, EntityPotionEffectEvent.Action action, boolean willOverride) {
        PotionEffect bukkitOldEffect = (oldEffect == null) ? null : CraftPotionUtil.toBukkit(oldEffect);
        PotionEffect bukkitNewEffect = (newEffect == null) ? null : CraftPotionUtil.toBukkit(newEffect);

        Preconditions.checkState(bukkitOldEffect != null || bukkitNewEffect != null, "Old and new potion effect are both null");

        EntityPotionEffectEvent event = new EntityPotionEffectEvent((LivingEntity) entity.getBukkitEntity(), bukkitOldEffect, bukkitNewEffect, cause, action, willOverride);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    public static boolean handleBlockFormEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState block, @Nullable Entity entity) {
        return CraftEventFactory.handleBlockFormEvent(world, pos, block, 3, entity);
    }

    public static boolean handleBlockFormEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState block, int flag) {
        return CraftEventFactory.handleBlockFormEvent(world, pos, block, flag, null);
    }

    public static boolean handleBlockFormEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState block, int flag, @Nullable Entity entity) {
        CraftBlockState blockState = CraftBlockStates.getBlockState(world, pos, flag);
        blockState.setData(block);

        BlockFormEvent event = (entity == null) ? new BlockFormEvent(blockState.getBlock(), blockState) : new EntityBlockFormEvent(entity.getBukkitEntity(), blockState.getBlock(), blockState);
        world.getCraftServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            blockState.update(true);
        }

        return !event.isCancelled();
    }

    public static boolean handleBatToggleSleepEvent(Entity bat, boolean awake) {
        BatToggleSleepEvent event = new BatToggleSleepEvent((Bat) bat.getBukkitEntity(), awake);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static boolean handlePlayerRecipeListUpdateEvent(net.minecraft.world.entity.player.Player who, ResourceLocation recipe) {
        PlayerRecipeDiscoverEvent event = new PlayerRecipeDiscoverEvent((Player) who.getBukkitEntity(), CraftNamespacedKey.fromMinecraft(recipe));
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static EntityPickupItemEvent callEntityPickupItemEvent(Entity who, ItemEntity item, int remaining, boolean cancelled) {
        EntityPickupItemEvent event = new EntityPickupItemEvent((LivingEntity) who.getBukkitEntity(), (Item) item.getBukkitEntity(), remaining);
        event.setCancelled(cancelled);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static LightningStrikeEvent callLightningStrikeEvent(LightningStrike entity, LightningStrikeEvent.Cause cause) {
        LightningStrikeEvent event = new LightningStrikeEvent(entity.getWorld(), entity, cause);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Raid events
     */
    public static boolean callRaidTriggerEvent(Raid raid, ServerPlayer player) {
        RaidTriggerEvent event = new RaidTriggerEvent(new CraftRaid(raid), raid.getLevel().getWorld(), player.getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static void callRaidFinishEvent(Raid raid, List<Player> players) {
        RaidFinishEvent event = new RaidFinishEvent(new CraftRaid(raid), raid.getLevel().getWorld(), players);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static void callRaidStopEvent(Raid raid, RaidStopEvent.Reason reason) {
        RaidStopEvent event = new RaidStopEvent(new CraftRaid(raid), raid.getLevel().getWorld(), reason);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static void callRaidSpawnWaveEvent(Raid raid, net.minecraft.world.entity.raid.Raider leader, List<net.minecraft.world.entity.raid.Raider> raiders) {
        Raider craftLeader = (CraftRaider) leader.getBukkitEntity();
        List<Raider> craftRaiders = new ArrayList<>();
        for (net.minecraft.world.entity.raid.Raider entityRaider : raiders) {
            craftRaiders.add((Raider) entityRaider.getBukkitEntity());
        }
        RaidSpawnWaveEvent event = new RaidSpawnWaveEvent(new CraftRaid(raid), raid.getLevel().getWorld(), craftLeader, craftRaiders);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static LootGenerateEvent callLootGenerateEvent(Container inventory, LootTable lootTable, LootContext lootInfo, List<ItemStack> loot, boolean plugin) {
        CraftWorld world = lootInfo.getLevel().getWorld();
        Entity entity = lootInfo.getParamOrNull(LootContextParams.THIS_ENTITY);
        NamespacedKey key = CraftNamespacedKey.fromMinecraft(world.getHandle().getServer().getLootData().lootTableToKey.get(lootTable));
        CraftLootTable craftLootTable = new CraftLootTable(key, lootTable);
        List<org.bukkit.inventory.ItemStack> bukkitLoot = loot.stream().map(CraftItemStack::asCraftMirror).collect(Collectors.toCollection(ArrayList::new));

        LootGenerateEvent event = new LootGenerateEvent(world, (entity != null ? entity.getBukkitEntity() : null), inventory.getOwner(), craftLootTable, CraftLootTable.convertContext(lootInfo), bukkitLoot, plugin);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean callStriderTemperatureChangeEvent(net.minecraft.world.entity.monster.Strider strider, boolean shivering) {
        StriderTemperatureChangeEvent event = new StriderTemperatureChangeEvent((Strider) strider.getBukkitEntity(), shivering);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static boolean handleEntitySpellCastEvent(SpellcasterIllager caster, SpellcasterIllager.IllagerSpell spell) {
        EntitySpellCastEvent event = new EntitySpellCastEvent((Spellcaster) caster.getBukkitEntity(), CraftSpellcaster.toBukkitSpell(spell));
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    /**
     * ArrowBodyCountChangeEvent
     */
    public static ArrowBodyCountChangeEvent callArrowBodyCountChangeEvent(net.minecraft.world.entity.LivingEntity entity, int oldAmount, int newAmount, boolean isReset) {
        org.bukkit.entity.LivingEntity bukkitEntity = (LivingEntity) entity.getBukkitEntity();

        ArrowBodyCountChangeEvent event = new ArrowBodyCountChangeEvent(bukkitEntity, oldAmount, newAmount, isReset);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    public static EntityExhaustionEvent callPlayerExhaustionEvent(net.minecraft.world.entity.player.Player humanEntity, EntityExhaustionEvent.ExhaustionReason exhaustionReason, float exhaustion) {
        EntityExhaustionEvent event = new EntityExhaustionEvent(humanEntity.getBukkitEntity(), exhaustionReason, exhaustion);
        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    public static PiglinBarterEvent callPiglinBarterEvent(net.minecraft.world.entity.monster.piglin.Piglin piglin, List<ItemStack> outcome, ItemStack input) {
        PiglinBarterEvent event = new PiglinBarterEvent((Piglin) piglin.getBukkitEntity(), CraftItemStack.asBukkitCopy(input), outcome.stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList()));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static void callEntitiesLoadEvent(Level world, ChunkPos coords, List<Entity> entities) {
        List<org.bukkit.entity.Entity> bukkitEntities = Collections.unmodifiableList(entities.stream().map(Entity::getBukkitEntity).collect(Collectors.toList()));
        EntitiesLoadEvent event = new EntitiesLoadEvent(new CraftChunk((ServerLevel) world, coords.x, coords.z), bukkitEntities);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static void callEntitiesUnloadEvent(Level world, ChunkPos coords, List<Entity> entities) {
        List<org.bukkit.entity.Entity> bukkitEntities = Collections.unmodifiableList(entities.stream().map(Entity::getBukkitEntity).collect(Collectors.toList()));
        EntitiesUnloadEvent event = new EntitiesUnloadEvent(new CraftChunk((ServerLevel) world, coords.x, coords.z), bukkitEntities);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static boolean callTNTPrimeEvent(Level world, BlockPos pos, TNTPrimeEvent.PrimeCause cause, Entity causingEntity, BlockPos causePosition) {
        org.bukkit.entity.Entity bukkitEntity = (causingEntity == null) ? null : causingEntity.getBukkitEntity();
        org.bukkit.block.Block bukkitBlock = (causePosition == null) ? null : CraftBlock.at(world, causePosition);

        TNTPrimeEvent event = new TNTPrimeEvent(CraftBlock.at(world, pos), cause, bukkitEntity, bukkitBlock);
        Bukkit.getPluginManager().callEvent(event);

        return !event.isCancelled();
    }

    public static PlayerRecipeBookClickEvent callRecipeBookClickEvent(ServerPlayer player, Recipe recipe, boolean shiftClick) {
        PlayerRecipeBookClickEvent event = new PlayerRecipeBookClickEvent(player.getBukkitEntity(), recipe, shiftClick);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTeleportEvent callEntityTeleportEvent(Entity nmsEntity, double x, double y, double z) {
        CraftEntity entity = nmsEntity.getBukkitEntity();
        Location to = new Location(entity.getWorld(), x, y, z, nmsEntity.getYRot(), nmsEntity.getXRot());
        EntityTeleportEvent event = new org.bukkit.event.entity.EntityTeleportEvent(entity, entity.getLocation(), to);

        Bukkit.getPluginManager().callEvent(event);

        return event;
    }

    public static boolean callEntityInteractEvent(Entity nmsEntity, Block block) {
        EntityInteractEvent event = new EntityInteractEvent(nmsEntity.getBukkitEntity(), block);
        Bukkit.getPluginManager().callEvent(event);

        return !event.isCancelled();
    }

    public static ExplosionPrimeEvent callExplosionPrimeEvent(Explosive explosive) {
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(explosive);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static ExplosionPrimeEvent callExplosionPrimeEvent(Entity nmsEntity, float size, boolean fire) {
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(nmsEntity.getBukkitEntity(), size, fire);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    // Paper start
    public static boolean handleBlockFailedDispenseEvent(ServerLevel serverLevel, BlockPos blockposition) {
        org.bukkit.block.Block block = serverLevel.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
        io.papermc.paper.event.block.BlockFailedDispenseEvent event = new io.papermc.paper.event.block.BlockFailedDispenseEvent(block);
        return event.callEvent();
    }

    public static boolean handleBlockPreDispenseEvent(ServerLevel serverLevel, BlockPos pos, ItemStack itemStack, int slot) {
        org.bukkit.block.Block block = serverLevel.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        io.papermc.paper.event.block.BlockPreDispenseEvent event = new io.papermc.paper.event.block.BlockPreDispenseEvent(block, org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asCraftMirror(itemStack), slot);
        return event.callEvent();
    }
    // Paper end

    // Paper start - missing BlockDispenseEvent calls
    @Nullable
    public static ItemStack handleBlockDispenseEvent(net.minecraft.core.BlockSource pointer, BlockPos to, ItemStack itemStack, net.minecraft.core.dispenser.DispenseItemBehavior instance) {
        org.bukkit.block.Block bukkitBlock = pointer.getLevel().getWorld().getBlockAt(pointer.getPos().getX(), pointer.getPos().getY(), pointer.getPos().getZ());
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemStack.copyWithCount(1));

        org.bukkit.event.block.BlockDispenseEvent event = new org.bukkit.event.block.BlockDispenseEvent(bukkitBlock, craftItem.clone(), new org.bukkit.util.Vector(to.getX(), to.getY(), to.getZ()));
        if (!net.minecraft.world.level.block.DispenserBlock.eventFired) {
            if (!event.callEvent()) {
                return itemStack;
            }
        }

        if (!event.getItem().equals(craftItem)) {
            // Chain to handler for new item
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            net.minecraft.core.dispenser.DispenseItemBehavior idispensebehavior = net.minecraft.world.level.block.DispenserBlock.DISPENSER_REGISTRY.get(eventStack.getItem());
            if (idispensebehavior != net.minecraft.core.dispenser.DispenseItemBehavior.NOOP && idispensebehavior != instance) {
                idispensebehavior.dispense(pointer, eventStack);
                return itemStack;
            }
        }
        return null;
    }
    // Paper end - missing BlockDispenseEvent calls

    // Paper start - add EntityFertilizeEggEvent
    /**
     * Calls the io.papermc.paper.event.entity.EntityFertilizeEggEvent.
     * If the event is cancelled, this method also resets the love on both the {@code breeding} and {@code other} entity.
     *
     * @param breeding the entity on which #spawnChildFromBreeding was called.
     * @param other    the partner of the entity.
     * @return the event after it was called. The instance may be used to retrieve the experience of the event.
     */
    public static io.papermc.paper.event.entity.EntityFertilizeEggEvent callEntityFertilizeEggEvent(net.minecraft.world.entity.animal.Animal breeding,
                                                                                                    net.minecraft.world.entity.animal.Animal other) {
        net.minecraft.server.level.ServerPlayer serverPlayer = breeding.getLoveCause();
        if (serverPlayer == null) serverPlayer = other.getLoveCause();
        final int experience = breeding.getRandom().nextInt(7) + 1; // From Animal#spawnChildFromBreeding(ServerLevel, Animal)

        final io.papermc.paper.event.entity.EntityFertilizeEggEvent event = new io.papermc.paper.event.entity.EntityFertilizeEggEvent((org.bukkit.entity.LivingEntity) breeding.getBukkitEntity(), (org.bukkit.entity.LivingEntity) other.getBukkitEntity(), serverPlayer == null ? null : serverPlayer.getBukkitEntity(), breeding.breedItem == null ? null : org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asCraftMirror(breeding.breedItem).clone(), experience);
        if (!event.callEvent()) {
            breeding.resetLove();
            other.resetLove(); // stop the pathfinding to avoid infinite loop
        }

        return event;
    }
    // Paper end - add EntityFertilizeEggEvent
}
