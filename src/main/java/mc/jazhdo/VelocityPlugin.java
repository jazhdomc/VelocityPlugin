package mc.jazhdo;

import java.util.List;
import java.util.Optional;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
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
import net.kyori.adventure.text.minimessage.MiniMessage;

@Plugin(id = "velocity_plugin", name = "VelocityPlugin", version = "0.1.3")
public class VelocityPlugin {
    private final ProxyServer server;

    @Inject
    public VelocityPlugin(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onPlayerJoin(ServerConnectedEvent event) {
        Optional<RegisteredServer> previousServer = event.getPreviousServer();
        String username = event.getPlayer().getUsername();
        for (Player player : event.getServer().getPlayersConnected()) player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>" + (previousServer.isPresent() ? previousServer.get().getServerInfo().getName() : "Server List") + " -> " + username));
        if (previousServer.isPresent()) for (Player player : previousServer.get().getPlayersConnected()) player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>" + username + " -> " + event.getServer().getServerInfo().getName()));
    }

    @Subscribe
    public void onPlayerLeave(DisconnectEvent event) {
        Player player = event.getPlayer();
        player.getCurrentServer().ifPresent(serverConnection -> {
            for (Player p : serverConnection.getServer().getPlayersConnected()) p.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>" + player.getUsername() + " -> Server List"));
        });
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        // Register easy transfer commands
        CommandManager cm = server.getCommandManager();
        List<String> cmds = List.of(
            "lobby", "base", "hub",
            "1block", "island", "one",
            "creative", "create", "build",
            "survival", "live", "1.12",
            "events", "games", "fun",
            "new-survival", "survive", "1.21"
        );
        List<String> servers = List.of("lobby", "oneblock", "creative", "survival", "events", "new-survival");
        for (int i = 0; i < cmds.size(); i++) cm.register(cm.metaBuilder(cmds.get(i)).build(), new ServerCommand(server, servers.get(i/3)));
        cmds = List.of(
            "discord", "dc",
            "website", "www", "web", "site"
        );
        List<String> links = List.of("Discord", "Website", "https://discord.gg/X6Ab2B35n4", "https://mc.itsjaz.com");
        for (int i = 0; i < cmds.size(); i++) {
            int type = ((i > 1) ? 1 : 0);
            cm.register(cm.metaBuilder(cmds.get(i)).build(), new MessageCommand("JazhdoMC's Official " + links.get(type), links.get(type + 2)));
        }
    }
    
    private static class ServerCommand implements SimpleCommand {
        private final ProxyServer server;
        private final String serverName;

        public ServerCommand(ProxyServer server, String serverName) {
            this.server = server;
            this.serverName = serverName;
        }

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            if (!(source instanceof Player)) {
                if (source != null) source.sendMessage(Component.text("Only players can use this!"));
                return;
            }

            Player player = (Player) source;
            Optional<RegisteredServer> target = server.getServer(serverName);

            if (target.isEmpty()) {
                player.sendMessage(Component.text("Server not found!"));
                return;
            }

            player.createConnectionRequest(target.get()).fireAndForget();
            player.sendMessage(Component.text("Sending you to " + serverName + " server..."));
        }
    }

    private static class MessageCommand implements SimpleCommand {
        private final String message;
        private final String link;

        public MessageCommand(String message, String link) {
            this.message = message;
            this.link = link;
        }

        @Override
        public void execute(Invocation invocation) {
            invocation.source().sendMessage(
                Component.text(message).append(Component.text(": ")).append(
                    Component.text(link)
                        .color(NamedTextColor.BLUE)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl(link)).hoverEvent(HoverEvent.showText(Component.text(message))
                    )
                )
            );
        }
    }
}
