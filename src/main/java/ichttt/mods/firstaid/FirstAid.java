package ichttt.mods.firstaid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ichttt.mods.firstaid.client.ClientHooks;
import ichttt.mods.firstaid.common.EventHandler;
import ichttt.mods.firstaid.common.FirstAidAttachments;
import ichttt.mods.firstaid.common.RegistryObjects;
import ichttt.mods.firstaid.common.apiimpl.HealingItemApiHelperImpl;
import ichttt.mods.firstaid.common.compat.enhancedvisuals.EnhancedVisualsCompat;
import ichttt.mods.firstaid.common.compat.playerrevive.PRCompatManager;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import ichttt.mods.firstaid.common.registries.FirstAidRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(FirstAid.MODID)
public class FirstAid {
    public static final String MODID = "firstaid";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static boolean isSynced = false;

    public FirstAid(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(EventHandler.class);

        modEventBus.addListener(this::init);
        modEventBus.addListener(this::registerCreativeTab);
        modEventBus.addListener(FirstAidNetwork::register);

        RegistryObjects.registerToBus(modEventBus);
        FirstAidRegistries.setup(modEventBus);
        FirstAidAttachments.ATTACHMENT_TYPES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, FirstAidConfig.serverSpec);
        modContainer.registerConfig(ModConfig.Type.COMMON, FirstAidConfig.generalSpec);
        modContainer.registerConfig(ModConfig.Type.CLIENT, FirstAidConfig.clientSpec);

        //noinspection Convert2MethodRef
        if (FMLEnvironment.dist == Dist.CLIENT) { ClientHooks.setup(modEventBus); }
        //Setup API
        HealingItemApiHelperImpl.init();
    }

    private static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        // FirstAidNetwork.register is also wired directly; keep this hook for clarity in logs.
        LOGGER.debug("Payload handler registration event fired");
    }

    private void registerCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(RegistryObjects.CREATIVE_TAB.getKey())) {
            event.accept(RegistryObjects.BANDAGE.get());
            event.accept(RegistryObjects.PLASTER.get());
            event.accept(RegistryObjects.MORPHINE.get());
        }
    }

    @SuppressWarnings("Convert2MethodRef") //Fucking classloading
    public void init(FMLCommonSetupEvent event) {
        LOGGER.info("{} starting...", MODID);
        if (FirstAidConfig.GENERAL.debug.get()) {
            LOGGER.warn("DEBUG MODE ENABLED");
            LOGGER.warn("FirstAid may be slower than usual and will produce much noisier logs if debug mode is enabled");
            LOGGER.warn("Disable debug in firstaid config");
        }

        event.enqueueWork(() -> {
            // Compatibility bridges must never be able to abort mod loading.
            try {
                PRCompatManager.init();
            } catch (Throwable throwable) {
                FirstAid.LOGGER.warn("PlayerRevive compatibility failed to initialise", throwable);
            }
            try {
                EnhancedVisualsCompat.init();
            } catch (Throwable throwable) {
                FirstAid.LOGGER.warn("EnhancedVisuals compatibility failed to initialise", throwable);
            }
        });
    }
}
