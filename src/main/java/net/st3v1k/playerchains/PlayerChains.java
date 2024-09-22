package net.st3v1k.playerchains;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PlayerChains implements ModInitializer {

    public static final String MOD_ID = "playerchains";
    public static double MAX_LENGTH = 3.5d;

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
    }

    @SuppressWarnings("unused")
    public static void addConnection(ServerPlayerEntity first, ServerPlayerEntity second) {
        if (first == second) {
            return;
        }

        ChainComponent chain1 = PlayerChainsComponents.CHAIN.get(first);
        chain1.addConnection(second.getGameProfile().getId());
        ChainComponent chain2 = PlayerChainsComponents.CHAIN.get(second);
        chain2.addConnection(first.getGameProfile().getId());
    }

    @SuppressWarnings("unused")
    public static void removeConnection(ServerPlayerEntity first, ServerPlayerEntity second) {
        ChainComponent chain1 = PlayerChainsComponents.CHAIN.get(first);
        chain1.removeConnection(second.getGameProfile().getId());
        ChainComponent chain2 = PlayerChainsComponents.CHAIN.get(second);
        chain2.removeConnection(first.getGameProfile().getId());
    }
}