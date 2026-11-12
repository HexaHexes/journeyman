package org.bitwisemadness.journeyman.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.bitwisemadness.journeyman.api.XpCurve;
import org.bitwisemadness.journeyman.data.ProfessionRegistry;
import org.bitwisemadness.journeyman.engine.ProfessionEngine;
import org.bitwisemadness.journeyman.state.JourneymanAttachments;
import org.bitwisemadness.journeyman.state.PlayerProfessions;

/**
 * The {@code /journeyman} command tree: inspection for everyone, mutation for
 * operators.
 *
 * <p>{@code xp add} flows through the engine funnel, so unknown and disabled
 * professions behave exactly as gameplay intake would. {@code xp set},
 * {@code xp remove} and {@code level set} are deliberate admin overrides that
 * write state directly; they validate the profession id themselves.</p>
 */
public final class JourneymanCommands {

    private static final SuggestionProvider<CommandSourceStack> PROFESSION_IDS =
        (context, builder) -> SharedSuggestionProvider.suggest(ProfessionRegistry.ids(), builder);

    private JourneymanCommands() {
    }

    /** Event-shaped registrar; called from the mod constructor. */
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    /** Registers the tree; called from the event registrar. */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("journeyman")
            .then(Commands.literal("professions").executes(JourneymanCommands::listProfessions))
            .then(Commands.literal("inspect")
                .executes(context -> inspect(context, context.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                    .requires(source -> source.hasPermission(2))
                    .executes(context -> inspect(context, EntityArgument.getPlayer(context, "player")))))
            .then(xpTree())
            .then(Commands.literal("level")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("set")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("profession", StringArgumentType.string()).suggests(PROFESSION_IDS)
                            .then(Commands.argument("level", IntegerArgumentType.integer(1, XpCurve.LEVEL_CAP))
                                .executes(JourneymanCommands::levelSet)))))));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> xpTree() {
        return Commands.literal("xp")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("add")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("profession", StringArgumentType.string()).suggests(PROFESSION_IDS)
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0D))
                            .executes(JourneymanCommands::xpAdd)))))
            .then(Commands.literal("set")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("profession", StringArgumentType.string()).suggests(PROFESSION_IDS)
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0D))
                            .executes(JourneymanCommands::xpSet)))))
            .then(Commands.literal("remove")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("profession", StringArgumentType.string()).suggests(PROFESSION_IDS)
                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0D))
                            .executes(JourneymanCommands::xpRemove)))));
    }

    private static int listProfessions(CommandContext<CommandSourceStack> context) {
        StringBuilder listing = new StringBuilder("Professions (").append(ProfessionRegistry.count()).append("):");
        for (String id : ProfessionRegistry.ids()) {
            listing.append("\n  ").append(id);
        }
        context.getSource().sendSuccess(() -> Component.literal(listing.toString()), false);
        return 1;
    }

    private static int inspect(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        PlayerProfessions state = player.getData(JourneymanAttachments.PROFESSIONS.get());
        StringBuilder listing = new StringBuilder("Professions of ")
            .append(player.getGameProfile().getName()).append(':');
        for (String id : ProfessionRegistry.ids()) {
            listing.append("\n  ").append(id)
                .append(" - level ").append(XpCurve.levelForXp(state.xp(id)))
                .append(", ").append(state.xp(id)).append(" XP");
        }
        context.getSource().sendSuccess(() -> Component.literal(listing.toString()), false);
        return 1;
    }

    private static int xpAdd(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        String profession = StringArgumentType.getString(context, "profession");
        double amount = DoubleArgumentType.getDouble(context, "amount");
        ProfessionEngine.Grant grant = ProfessionEngine.grant(target, profession, amount, "command");
        if (!grant.granted()) {
            context.getSource().sendFailure(Component.literal(
                "Grant rejected: unknown or disabled profession, or already at the cap."));
            return 0;
        }
        final long applied = grant.applied();
        final int newLevel = grant.newLevel();
        context.getSource().sendSuccess(() -> Component.literal(
            "Granted " + applied + " XP in " + profession + " to " + target.getGameProfile().getName()
                + " (now level " + newLevel + ")"), true);
        return 1;
    }

    private static int xpSet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        String profession = StringArgumentType.getString(context, "profession");
        long amount = Math.round(DoubleArgumentType.getDouble(context, "amount"));
        if (!valid(context, profession)) {
            return 0;
        }
        long clamped = Math.min(XpCurve.XP_AT_CAP, Math.max(0L, amount));
        state(target).set(profession, clamped);
        return reportSet(context, target, profession, clamped, XpCurve.levelForXp(clamped));
    }

    private static int xpRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        String profession = StringArgumentType.getString(context, "profession");
        long amount = Math.round(DoubleArgumentType.getDouble(context, "amount"));
        if (!valid(context, profession)) {
            return 0;
        }
        PlayerProfessions state = state(target);
        long before = state.xp(profession);
        long remaining = Math.max(0L, before - amount);
        state.set(profession, remaining);
        return reportSet(context, target, profession, remaining, XpCurve.levelForXp(remaining));
    }

    private static int levelSet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        String profession = StringArgumentType.getString(context, "profession");
        int level = IntegerArgumentType.getInteger(context, "level");
        if (!valid(context, profession)) {
            return 0;
        }
        long xp = XpCurve.xpToReach(level);
        state(target).set(profession, xp);
        context.getSource().sendSuccess(() -> Component.literal(
            "Set " + profession + " to level " + level + " (" + xp + " XP) for "
                + target.getGameProfile().getName()), true);
        return 1;
    }

    private static int reportSet(CommandContext<CommandSourceStack> context, ServerPlayer target,
                                 String profession, long xp, int level) {
        context.getSource().sendSuccess(() -> Component.literal(
            profession + " is now " + xp + " XP (level " + level + ") for "
                + target.getGameProfile().getName()), true);
        return 1;
    }

    private static boolean valid(CommandContext<CommandSourceStack> context, String profession) {
        if (ProfessionRegistry.definition(net.minecraft.resources.ResourceLocation.tryParse(profession)) == null) {
            context.getSource().sendFailure(Component.literal("Unknown profession: " + profession));
            return false;
        }
        return true;
    }

    private static PlayerProfessions state(ServerPlayer player) {
        return player.getData(JourneymanAttachments.PROFESSIONS.get());
    }
}
