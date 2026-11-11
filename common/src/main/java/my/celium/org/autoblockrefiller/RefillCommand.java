package my.celium.org.autoblockrefiller;

import java.util.Locale;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import my.celium.org.command.CommandHelper;
import my.celium.org.config.ConfigStrings;
import my.celium.org.config.ConfigValue;
import my.celium.org.util.ChatVoice;
import my.celium.org.util.TextUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /autoblockrefiller}, also {@code /abr}:
 *
 * <ul>
 *   <li>{@code /autoblockrefiller} - every setting with its value; operators can click to change them;</li>
 *   <li>{@code /autoblockrefiller toggle} - any player turns refilling off or on for themselves;</li>
 *   <li>{@code /autoblockrefiller set <setting> <value>} - operators change a setting;</li>
 *   <li>{@code /autoblockrefiller reset <setting>|all} - operators put settings back to their defaults.</li>
 * </ul>
 */
public final class RefillCommand {
    static final ChatVoice VOICE = TextUtil.voice("Refiller");
    private static final String ROOT = "autoblockrefiller";
    private static final int OP = 2;

    private static final SuggestionProvider<CommandSourceStack> SETTINGS = (context, builder) -> {
        String typed = builder.getRemainingLowerCase();
        for (ConfigValue<?> value : RefillMod.config.values()) {
            String path = ConfigStrings.pathOf(value);
            if (path.toLowerCase(Locale.ROOT).startsWith(typed)) {
                builder.suggest(path);
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> VALUES = (context, builder) -> {
        ConfigValue<?> value = ConfigStrings.find(RefillMod.config, StringArgumentType.getString(context, "setting"));
        if (value instanceof ConfigValue.BooleanValue) {
            builder.suggest("true").suggest("false");
        }
        return builder.buildFuture();
    };

    private RefillCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(tree(ROOT));
        dispatcher.register(tree("abr"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> tree(String name) {
        return Commands.literal(name)
                .executes(RefillCommand::status)
                .then(Commands.literal("toggle").executes(RefillCommand::toggle))
                .then(Commands.literal("set")
                        .requires(source -> CommandHelper.permission(source, OP))
                        .then(Commands.argument("setting", StringArgumentType.word()).suggests(SETTINGS)
                                .then(Commands.argument("value", StringArgumentType.greedyString()).suggests(VALUES)
                                        .executes(RefillCommand::set))))
                .then(Commands.literal("reset")
                        .requires(source -> CommandHelper.permission(source, OP))
                        .then(Commands.literal("all").executes(RefillCommand::resetAll))
                        .then(Commands.argument("setting", StringArgumentType.word()).suggests(SETTINGS)
                                .executes(RefillCommand::reset)));
    }

    // ---------------------------------------------------------------- status

    private static int status(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        boolean op = CommandHelper.permission(source, OP);
        RefillStats.count("command_status");
        CommandHelper.info(source, VOICE.header(RefillMod.NAME + " " + RefillMod.META.version()));

        ServerPlayer player = source.getPlayer();
        if (player != null && RefillMod.playerToggle.get()) {
            boolean on = RefillPlayers.wantsRefills(player.getUUID());
            CommandHelper.info(source, TextUtil.item(TextUtil.msg(
                    TextUtil.muted("For you  "), state(on), TextUtil.body("   "),
                    TextUtil.button(on ? "Turn off" : "Turn on", "/" + ROOT + " toggle",
                            on ? "Stop refilling your slots" : "Refill your slots again"))));
        }
        for (ConfigValue<?> value : RefillMod.config.values()) {
            CommandHelper.info(source, row(value, op));
        }
        if (op) {
            CommandHelper.info(source, TextUtil.detail("Click a value to change it. Hover a setting to see what it does."));
        }
        return 1;
    }

    private static MutableComponent row(ConfigValue<?> value, boolean op) {
        String path = ConfigStrings.pathOf(value);
        MutableComponent name = TextUtil.muted(path + "  ").withStyle(style -> style.withHoverEvent(
                new HoverEvent.ShowText(TextUtil.body(value.description()))));
        MutableComponent shown;
        if (value instanceof ConfigValue.BooleanValue bool) {
            boolean on = bool.get();
            shown = state(on);
            if (op) {
                shown = TextUtil.empty().append(shown).append(TextUtil.body("   ")).append(TextUtil.button(
                        on ? "Turn off" : "Turn on", "/" + ROOT + " set " + path + " " + !on, value.description()));
            }
        } else {
            shown = TextUtil.hex(ConfigStrings.valueToString(value), TextUtil.ACCENT);
            if (op) {
                shown = TextUtil.empty().append(shown).append(TextUtil.body("   ")).append(TextUtil.suggest(
                        "Change", "/" + ROOT + " set " + path + " ", ConfigStrings.describe(value)));
            }
        }
        return TextUtil.item(TextUtil.empty().append(name).append(shown));
    }

    private static MutableComponent state(boolean on) {
        return on ? TextUtil.hex("on", TextUtil.SUCCESS) : TextUtil.hex("off", TextUtil.ERROR);
    }

    // ---------------------------------------------------------------- toggle

    private static int toggle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = CommandHelper.player(context.getSource());
        if (!RefillMod.playerToggle.get()) {
            CommandHelper.failure(context.getSource(),
                    VOICE.fail("This server does not let players turn refilling off for themselves."));
            return 0;
        }
        boolean on = RefillPlayers.toggle(player.getUUID());
        RefillStats.count(on ? "toggle_on" : "toggle_off");
        CommandHelper.success(context.getSource(), on
                ? VOICE.ok("Refilling is ", state(true), " for you again.")
                : VOICE.ok("Refilling is ", state(false), " for you. ",
                        TextUtil.button("Turn on", "/" + ROOT + " toggle", "Refill your slots again")));
        return 1;
    }

    // ---------------------------------------------------------------- operators

    private static int set(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ConfigValue<?> value = find(source, StringArgumentType.getString(context, "setting"));
        if (value == null) {
            return 0;
        }
        String raw = StringArgumentType.getString(context, "value").trim();
        String before = ConfigStrings.valueToString(value);
        try {
            ConfigStrings.parseAndSet(value, raw);
        } catch (IllegalArgumentException e) {
            CommandHelper.failure(source, VOICE.fail("'" + raw + "' does not work for "
                    + ConfigStrings.pathOf(value) + ". Allowed: " + ConfigStrings.describe(value) + "."));
            return 0;
        }
        RefillStats.count("command_set");
        String after = ConfigStrings.valueToString(value);
        if (before.equals(after)) {
            CommandHelper.success(source, VOICE.note(ConfigStrings.pathOf(value) + " is already " + after + "."));
        } else {
            CommandHelper.success(source, VOICE.ok(ConfigStrings.pathOf(value) + " is now ",
                    TextUtil.hex(after, TextUtil.ACCENT), TextUtil.muted("   was " + before)));
        }
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ConfigValue<?> value = find(source, StringArgumentType.getString(context, "setting"));
        if (value == null) {
            return 0;
        }
        value.reset();
        RefillStats.count("command_reset");
        CommandHelper.success(source, VOICE.ok(ConfigStrings.pathOf(value) + " is back to ",
                TextUtil.hex(ConfigStrings.valueToString(value), TextUtil.ACCENT), "."));
        return 1;
    }

    private static int resetAll(CommandContext<CommandSourceStack> context) {
        RefillMod.config.resetAll();
        RefillStats.count("command_reset");
        CommandHelper.success(context.getSource(), VOICE.ok("Every setting is back to its default."));
        return 1;
    }

    private static ConfigValue<?> find(CommandSourceStack source, String path) {
        ConfigValue<?> value = ConfigStrings.find(RefillMod.config, path);
        if (value == null) {
            CommandHelper.failure(source, VOICE.fail("There is no setting called '" + path + "'. Type /"
                    + ROOT + " to see them all."));
        }
        return value;
    }
}
