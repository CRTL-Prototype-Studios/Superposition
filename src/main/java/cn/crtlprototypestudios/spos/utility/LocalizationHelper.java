package cn.crtlprototypestudios.spos.utility;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class LocalizationHelper {
    public static MutableComponent getComponent(String key) {
        return Component.translatable("infauth." + key);
    }

    public static MutableComponent getComponent(String key, Object... args) {
        return Component.translatable("infauth." + key, args);
    }
}
