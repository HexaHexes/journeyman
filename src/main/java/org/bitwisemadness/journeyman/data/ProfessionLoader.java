package org.bitwisemadness.journeyman.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

/**
 * Loads {@code data/<namespace>/professions/*.json} on server start and on
 * every {@code /reload}, rebuilding the profession snapshot from scratch each
 * time.
 *
 * <p>A file that fails to parse is logged and skipped, never fatal; the
 * profession id is the file's resource location, so a pack override replaces
 * the default under the same id. Nothing here trusts the file's name for
 * identity - the location is the identity.</p>
 */
public final class ProfessionLoader extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    public ProfessionLoader() {
        super(new Gson(), "professions");
    }

    /** Registers the listener; called from the mod constructor. */
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ProfessionLoader());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        Map<ResourceLocation, ProfessionDefinition> loaded = new TreeMap<>(ResourceLocation::compareTo);
        files.forEach((location, json) -> {
            try {
                loaded.put(location, ProfessionDefinition.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
            } catch (Exception e) {
                LOGGER.error("Skipping malformed profession definition {}", location, e);
            }
        });
        ProfessionRegistry.set(loaded);
        LOGGER.info("Loaded {} profession(s) from datapacks", loaded.size());
    }
}
