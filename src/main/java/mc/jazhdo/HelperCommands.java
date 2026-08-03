package mc.jazhdo;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@Plugin(id = "helper-commands", name = "HelperCommands", version = "0.1.4")
public class HelperCommands {
    private final ProxyServer proxy;
    private final Logger logger;

    @Inject
    public HelperCommands(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        // Log start
        logger.log(Level.INFO, "HelperCommands starting...");

        // Register server transfer commands
        CommandManager cm = proxy.getCommandManager();
        List<String> cmds = List.of(
            "lobby", "base", "hub",
            "1block", "island", "one",
            "creative", "create", "build",
            "survival", "live", "1.12",
            "events", "games", "fun",
            "new-survival", "survive", "1.21"
        );
        List<String> servers = List.of("lobby", "oneblock", "creative", "survival", "events", "new-survival");
        for (int i = 0; i < 6; i++)
            for (String command : cmds.subList(i*3, (i*3)+3))
                cm.register(cm.metaBuilder(command).build(), new ServerCommand(proxy, servers.get(i), logger));

        // Register discord and website link commands
        for (String command : List.of("discord", "dc")) cm.register(cm.metaBuilder(command).build(), new MessageCommand("Discord", "discord.gg/X6Ab2B35n4"));
        for (String command : List.of("website", "www", "web", "site")) cm.register(cm.metaBuilder(command).build(), new MessageCommand("Website", "mc.itsjaz.com"));

        // Log end of start
        logger.log(Level.INFO, "HelperCommands done starting.");
    }
    
    private static class ServerCommand implements SimpleCommand {
        private final RegisteredServer target;
        private final Component teleportationMessage, otherSourceError = Component.text("Only players can use this!").color(NamedTextColor.RED);

        public ServerCommand(ProxyServer server, String serverName, Logger logger) {
            // Show normal teleportation messages or server not found errors
            Optional<RegisteredServer> serverObj = server.getServer(serverName);
            if (serverObj.isPresent()) {
                target = serverObj.get();
                teleportationMessage = Component.text("Sending you to " + serverName + " server...").color(NamedTextColor.YELLOW);
            } else {
                logger.log(Level.WARNING, "Server {0} was not found. All teleportation requests to that server will not work.", serverName);
                target = null;
                teleportationMessage = Component.text("Server " + serverName + " was not found. Contact staff for help.").color(NamedTextColor.RED);
            }
        }

        @Override
        public void execute(Invocation invocation) {
            // Make sure source exists
            CommandSource source = invocation.source();
            if (source == null) return;

            // Check that a player and not console is sending the command
            if (source instanceof Player player) {
                // Teleport if server exists and send cooresponding message
                if (target != null) player.createConnectionRequest(target).fireAndForget();
                player.sendMessage(teleportationMessage);
            } else source.sendMessage(otherSourceError);
        }
    }

    private static class MessageCommand implements SimpleCommand {
        private final Component message;

        public MessageCommand(String name, String link) {
            message = Component.text("JazhdoMC's Official " + name).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD).append(Component.text(": ")).append(
                Component.text("https://" + link)
                    .color(NamedTextColor.BLUE)
                    .decorate(TextDecoration.UNDERLINED)
                    .decorate(TextDecoration.ITALIC)
                    .clickEvent(ClickEvent.openUrl("https://" + link))
                    .hoverEvent(HoverEvent.showText(Component.text("JazhdoMC's Official " + name).color(NamedTextColor.BLUE)))
            );
        }

        @Override
        public void execute(Invocation invocation) {
            // Send link message
            invocation.source().sendMessage(message);
        }
    }
}
