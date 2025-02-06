package cn.crtlprototypestudios.spos.utility;

import cn.crtlprototypestudios.spos.Spos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class LocalizationHelper {
    public static MutableComponent getComponent(String key) {
        return Component.translatable(Spos.MODID + "." + key);
    }

    public static MutableComponent getComponent(String key, Object... args) {
        return Component.translatable(Spos.MODID + "." + key, args);
    }

    public static String getString(String key){
        return getComponent(key).getString();
    }

    public static String getString(String key, Object... args) {
        return getComponent(key, args).getString();
    }
}
