package mc.jazhdo;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

import com.google.inject.Inject;
import java.util.Optional;

@Plugin(id = "velocity_plugin", name = "VelocityPlugin", version = "0.0.1")
public class VelocityPlugin {
    private final ProxyServer server;

    @Inject
    public VelocityPlugin(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        CommandManager cm = server.getCommandManager();
        cm.register(cm.metaBuilder("hub").build(), new ServerCommand(server, "lobby"));
        cm.register(cm.metaBuilder("lobby").build(), new ServerCommand(server, "lobby"));
        cm.register(cm.metaBuilder("leave").build(), new ServerCommand(server, "lobby"));
        cm.register(cm.metaBuilder("back").build(), new ServerCommand(server, "lobby"));
        cm.register(cm.metaBuilder("survival").build(), new ServerCommand(server, "survival"));
        cm.register(cm.metaBuilder("survive").build(), new ServerCommand(server, "survival"));
        cm.register(cm.metaBuilder("live").build(), new ServerCommand(server, "survival"));
        cm.register(cm.metaBuilder("earth").build(), new ServerCommand(server, "earth"));
        cm.register(cm.metaBuilder("world").build(), new ServerCommand(server, "earth"));
        cm.register(cm.metaBuilder("planet").build(), new ServerCommand(server, "earth"));
        cm.register(cm.metaBuilder("minigames").build(), new ServerCommand(server, "minigames"));
        cm.register(cm.metaBuilder("games").build(), new ServerCommand(server, "minigames"));
        cm.register(cm.metaBuilder("fun").build(), new ServerCommand(server, "minigames"));
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
            if (!(invocation.source() instanceof Player)) {
                invocation.source().sendMessage(Component.text("Only players can use this!"));
                return;
            }

            Player player = (Player) invocation.source();
            Optional<RegisteredServer> target = server.getServer(serverName);

            if (target.isEmpty()) {
                player.sendMessage(Component.text("Server not found!"));
                return;
            }

            player.createConnectionRequest(target.get()).fireAndForget();
            player.sendMessage(Component.text("Sending you to " + serverName + "..."));
        }
    }
}