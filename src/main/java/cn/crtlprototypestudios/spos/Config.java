package cn.crtlprototypestudios.spos;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Spos.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ENABLE_TELEPORT_DELAY = BUILDER
            .comment("Enable teleportation delay")
            .define("enableTeleportDelay", true);

    public static ForgeConfigSpec.IntValue TPA_REQUEST_EXPIRATION_TIME = BUILDER
            .comment("Time in seconds before a teleport request expires")
            .defineInRange("tpaRequestExpirationTime", 30, 5, 300);

    private static final ForgeConfigSpec.IntValue STAY_STILL_DURATION = BUILDER
            .comment("Time in seconds a player needs to stand still before teleporting")
            .defineInRange("stayStillDuration", 3, 1, 10);

    private static final ForgeConfigSpec.BooleanValue ALLOW_DEATH_BACK = BUILDER
            .comment("Allow using /back to teleport to death location")
            .define("allowDeathBack", true);

    private static final ForgeConfigSpec.BooleanValue ALLOW_COMMAND_TPA = BUILDER
            .comment("Enable /tpa command")
            .define("allowCommandTpa", true);

    private static final ForgeConfigSpec.BooleanValue ALLOW_COMMAND_BACK = BUILDER
            .comment("Enable /back command")
            .define("allowCommandBack", true);

    private static final ForgeConfigSpec.BooleanValue ALLOW_COMMAND_SPAWN = BUILDER
            .comment("Enable /spawn command")
            .define("allowCommandSpawn", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int stayStillDuration;
    public static int tpaRequestExpirationTime;
    public static boolean enableTeleportDelay;
    public static boolean allowDeathBack;
    public static boolean allowCommandTpa;
    public static boolean allowCommandBack;
    public static boolean allowCommandSpawn;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        stayStillDuration = STAY_STILL_DURATION.get();
        tpaRequestExpirationTime = TPA_REQUEST_EXPIRATION_TIME.get();
        enableTeleportDelay = ENABLE_TELEPORT_DELAY.get();
        allowDeathBack = ALLOW_DEATH_BACK.get();
        allowCommandTpa = ALLOW_COMMAND_TPA.get();
        allowCommandBack = ALLOW_COMMAND_BACK.get();
        allowCommandSpawn = ALLOW_COMMAND_SPAWN.get();
    }
}

