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
import my.celium.org.translation.ModLang;
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
    private static final ModLang L = RefillMod.LANG;
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
                    TextUtil.muted(L.get(source, "cmd.for_you") + "  "), state(source, on), TextUtil.body("   "),
                    TextUtil.button(L.get(source, on ? "cmd.turn_off" : "cmd.turn_on"), "/" + ROOT + " toggle",
                            L.get(source, on ? "refill.stop_hover" : "refill.again_hover")))));
        }
        for (ConfigValue<?> value : RefillMod.config.values()) {
            CommandHelper.info(source, row(source, value, op));
        }
        if (op) {
            CommandHelper.info(source, TextUtil.detail(L.get(source, "cmd.click_hint")));
        }
        return 1;
    }

    private static MutableComponent row(CommandSourceStack source, ConfigValue<?> value, boolean op) {
        String path = ConfigStrings.pathOf(value);
        String description = describe(source, value);
        MutableComponent name = TextUtil.muted(path + "  ").withStyle(style -> style.withHoverEvent(
                new HoverEvent.ShowText(TextUtil.body(description))));
        MutableComponent shown;
        if (value instanceof ConfigValue.BooleanValue bool) {
            boolean on = bool.get();
            shown = state(source, on);
            if (op) {
                shown = TextUtil.empty().append(shown).append(TextUtil.body("   ")).append(TextUtil.button(
                        L.get(source, on ? "cmd.turn_off" : "cmd.turn_on"), "/" + ROOT + " set " + path + " " + !on,
                        description));
            }
        } else {
            shown = TextUtil.hex(ConfigStrings.valueToString(value), TextUtil.ACCENT);
            if (op) {
                shown = TextUtil.empty().append(shown).append(TextUtil.body("   ")).append(TextUtil.suggest(
                        L.get(source, "cmd.change"), "/" + ROOT + " set " + path + " ", ConfigStrings.describe(value)));
            }
        }
        return TextUtil.item(TextUtil.empty().append(name).append(shown));
    }

    private static MutableComponent state(CommandSourceStack source, boolean on) {
        return on ? TextUtil.hex(L.get(source, "cmd.on"), TextUtil.SUCCESS) : TextUtil.hex(L.get(source, "cmd.off"), TextUtil.ERROR);
    }

    /** A setting's description in the reader's language. */
    private static String describe(CommandSourceStack source, ConfigValue<?> value) {
        String key = "config." + value.key() + ".desc";
        return L.has(key) ? L.get(source, key) : value.description();
    }

    // ---------------------------------------------------------------- toggle

    private static int toggle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = CommandHelper.player(context.getSource());
        if (!RefillMod.playerToggle.get()) {
            CommandHelper.failure(context.getSource(),
                    VOICE.fail(L.get(context.getSource(), "refill.no_toggle")));
            return 0;
        }
        boolean on = RefillPlayers.toggle(player.getUUID());
        RefillStats.count(on ? "toggle_on" : "toggle_off");
        CommandSourceStack source = context.getSource();
        CommandHelper.success(source, on
                ? VOICE.ok(L.text(source, "refill.on_again", state(source, true)))
                : VOICE.ok(L.text(source, "refill.off", state(source, false)), " ",
                        TextUtil.button(L.get(source, "cmd.turn_on"), "/" + ROOT + " toggle", L.get(source, "refill.again_hover"))));
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
            CommandHelper.failure(source, VOICE.fail(L.get(source, "cmd.invalid", raw, ConfigStrings.pathOf(value),
                    ConfigStrings.describe(value))));
            return 0;
        }
        RefillStats.count("command_set");
        String after = ConfigStrings.valueToString(value);
        if (before.equals(after)) {
            CommandHelper.success(source, VOICE.note(L.get(source, "cmd.already", ConfigStrings.pathOf(value), after)));
        } else {
            CommandHelper.success(source, VOICE.ok(L.text(source, "cmd.now", ConfigStrings.pathOf(value),
                    TextUtil.hex(after, TextUtil.ACCENT)), TextUtil.muted("   " + L.get(source, "cmd.was", before))));
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
        CommandHelper.success(source, VOICE.ok(L.text(source, "cmd.back", ConfigStrings.pathOf(value),
                TextUtil.hex(ConfigStrings.valueToString(value), TextUtil.ACCENT))));
        return 1;
    }

    private static int resetAll(CommandContext<CommandSourceStack> context) {
        RefillMod.config.resetAll();
        RefillStats.count("command_reset");
        CommandHelper.success(context.getSource(), VOICE.ok(L.get(context.getSource(), "cmd.all_reset")));
        return 1;
    }

    private static ConfigValue<?> find(CommandSourceStack source, String path) {
        ConfigValue<?> value = ConfigStrings.find(RefillMod.config, path);
        if (value == null) {
            CommandHelper.failure(source, VOICE.fail(L.get(source, "cmd.no_setting", path, ROOT)));
        }
        return value;
    }
}
