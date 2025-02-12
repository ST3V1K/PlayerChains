package net.st3v1k.playerchains;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PlayerChains implements ModInitializer {

    public static final String MOD_ID = "playerchains";
    public static double MAX_LENGTH = 3.5d;

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("chain")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(literal("add")
                                .then(
                                        argument("first", EntityArgumentType.player())
                                                .then(
                                                        argument("second", EntityArgumentType.player())
                                                                .executes((context) -> {
                                                                    ServerPlayerEntity first = EntityArgumentType.getPlayer(context, "first");
                                                                    ServerPlayerEntity second = EntityArgumentType.getPlayer(context, "second");
                                                                    if (first == second) {
                                                                        context.getSource().sendError(Text.literal("Cannot chain a player to itself."));
                                                                    }

                                                                    addConnection(first, second);
                                                                    return 1;
                                                                }))))
                        .then(literal("remove")
                                .then(argument("first", EntityArgumentType.player())
                                        .then(argument("second", EntityArgumentType.player())
                                                .executes((context) -> {
                                                    ServerPlayerEntity first = EntityArgumentType.getPlayer(context, "first");
                                                    ServerPlayerEntity second = EntityArgumentType.getPlayer(context, "second");
                                                    removeConnection(first, second);
                                                    return 1;
                                                }))))));
    }

    public static void addConnection(ServerPlayerEntity first, ServerPlayerEntity second) {
        if (first == second) {
            return;
        }

        ChainComponent chain1 = PlayerChainsComponents.CHAIN.get(first);
        chain1.addConnection(second.getGameProfile().getId());
        ChainComponent chain2 = PlayerChainsComponents.CHAIN.get(second);
        chain2.addConnection(first.getGameProfile().getId());
    }

    public static void removeConnection(ServerPlayerEntity first, ServerPlayerEntity second) {
        ChainComponent chain1 = PlayerChainsComponents.CHAIN.get(first);
        chain1.removeConnection(second.getGameProfile().getId());
        ChainComponent chain2 = PlayerChainsComponents.CHAIN.get(second);
        chain2.removeConnection(first.getGameProfile().getId());
    }
}