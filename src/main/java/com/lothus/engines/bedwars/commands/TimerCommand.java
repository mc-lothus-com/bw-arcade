package com.lothus.engines.bedwars.commands;

import com.lothus.bukkit.commands.CommandBase;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import org.bukkit.command.CommandSender;

public class TimerCommand extends CommandBase {

    public TimerCommand() {
        super("timer");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("lothus.setup")) {
            sender.sendMessage("§cVocê não tem permissão para usar este comando.");
            return false;
        }
        if (args.length < 1) {
            sender.sendMessage("§cSintaxe incorreta, use §e/timer <tempo>§c.");
            return false;
        }
        int time;

        try {
            time = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cUse somente números.");
            return false;
        }
        Arena arena = Bedwars.getInstance().getGameManager().getArena();

        sender.sendMessage("§aO tempo da partida foi alterado para §f" + secondToMinutes(time) + "§a.");
        arena.getTimer().setTime(time);
        return false;
    }

    private String secondToMinutes(int segundos) {
        int minutos = segundos / 60;
        int segundosRestantes = segundos % 60;
        String formato = "%02d:%02d";
        return String.format(formato, minutos, segundosRestantes);
    }

}
