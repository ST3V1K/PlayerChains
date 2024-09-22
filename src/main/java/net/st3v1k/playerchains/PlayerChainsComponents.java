package net.st3v1k.playerchains;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;

public class PlayerChainsComponents implements EntityComponentInitializer {
    public static final ComponentKey<ChainComponent> CHAIN = ComponentRegistry.getOrCreate(PlayerChains.id("chain"), ChainComponent.class);

    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(CHAIN, ChainComponent::new, RespawnCopyStrategy.CHARACTER);
    }
}
