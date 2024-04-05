package com.lothus.engines.bedwars.commands;

import com.lothus.bukkit.commands.CommandBase;
import com.lothus.core.games.state.GameState;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import org.bukkit.command.CommandSender;

public class StartCommand extends CommandBase {

    public StartCommand() {
        super("start");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("lothus.setup")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return false;
        }
        Arena arena = Bedwars.getInstance().getGameManager().getArena();

        if (arena.getGameInfo().getState() == GameState.INICIANDO) {
            arena.getTimer().setTime(1);

            sender.sendMessage("§aPartida iniciada.");
        } else {
            sender.sendMessage("§cNão é possível iniciar a partida.");
        }
        return false;
    }
}
