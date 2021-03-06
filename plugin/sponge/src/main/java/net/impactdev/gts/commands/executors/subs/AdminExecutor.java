package net.impactdev.gts.commands.executors.subs;

import net.impactdev.gts.commands.annotations.Alias;
import net.impactdev.gts.commands.annotations.Permission;
import net.impactdev.gts.commands.executors.GTSCmdExecutor;
import net.impactdev.gts.common.plugin.GTSPlugin;
import net.impactdev.gts.sponge.utils.Utilities;
import net.impactdev.gts.ui.admin.SpongeAdminMenu;
import net.impactdev.gts.util.GTSInfoGenerator;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.services.text.MessageService;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@Alias("admin")
@Permission("gts.admin.base")
public class AdminExecutor extends GTSCmdExecutor {

    public AdminExecutor(GTSPlugin plugin) {
        super(plugin);
    }

    @Override
    public Optional<Text> getDescription() {
        return Optional.empty();
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[0];
    }

    @Override
    public GTSCmdExecutor[] getSubcommands() {
        return new GTSCmdExecutor[] {
                new Info(this.plugin),
                new Ping(this.plugin)
        };
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext arguments) throws CommandException {
        if(source instanceof Player) {
            new SpongeAdminMenu((Player) source).open();
            return CommandResult.success();
        }

        throw new CommandException(Text.of("Only players can use the base command!"));
    }

    @Alias("info")
    @Permission("gts.admin.info")
    public static class Info extends GTSCmdExecutor {

        public Info(GTSPlugin plugin) {
            super(plugin);
        }

        @Override
        public Optional<Text> getDescription() {
            return Optional.empty();
        }

        @Override
        public CommandElement[] getArguments() {
            return new CommandElement[0];
        }

        @Override
        public GTSCmdExecutor[] getSubcommands() {
            return new GTSCmdExecutor[0];
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            MessageService<Text> service = Utilities.PARSER;

            new GTSInfoGenerator(src).create(src).thenAccept(x -> {
                src.sendMessage(service.parse("{{gts:prefix}} Report saved to: &a" + x));
            });
            return CommandResult.success();
        }

    }

    @Alias("ping")
    @Permission("gts.admin.ping")
    public static class Ping extends GTSCmdExecutor {

        public Ping(GTSPlugin plugin) {
            super(plugin);
        }

        @Override
        public Optional<Text> getDescription() {
            return Optional.empty();
        }

        @Override
        public CommandElement[] getArguments() {
            return new CommandElement[0];
        }

        @Override
        public GTSCmdExecutor[] getSubcommands() {
            return new GTSCmdExecutor[0];
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            MessageService<Text> service = Impactor.getInstance().getRegistry().get(MessageService.class);
            GTSPlugin.getInstance().getMessagingService()
                    .sendPing()
                    .thenAccept(pong -> {
                        if(pong.wasSuccessful()) {
                            src.sendMessage(service.parse(
                                    "&eGTS &7\u00bb Ping request &asuccessful&7, took &b" + pong.getResponseTime() + " ms&7!"
                            ));
                        }
                    });
            return CommandResult.success();
        }

    }
}
