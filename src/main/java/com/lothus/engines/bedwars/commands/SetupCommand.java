package com.lothus.engines.bedwars.commands;

import com.lothus.bukkit.commands.CommandBase;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.location.Locations;
import com.lothus.engines.bedwars.location.manager.LocationManager;
import com.lothus.engines.bedwars.location.type.LocationType;
import com.lothus.engines.bedwars.team.color.TeamColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetupCommand extends CommandBase {

    private final LocationManager locationManager;

    public SetupCommand() {
        super("setup");

        locationManager = Bedwars.getInstance().getGameManager().getLocationManager();
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only for players.");
            return false;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("lothus.setup")) {
            player.sendMessage("§cVocê não tem permissão para usar este comando.");
            return false;
        }

        if (args.length < 1) {
            player.sendMessage("§cSintaxe incorreta, use §e/setup lobby§c.");
            player.sendMessage("§cSintaxe incorreta, use §e/setup bed <time>§c.");
            player.sendMessage("§cSintaxe incorreta, use §e/setup spawn <time>§c.");
            player.sendMessage("§cSintaxe incorreta, use §e/setup generator team <time> <iron/gold>§c.");
            player.sendMessage("§cSintaxe incorreta, use §e/setup generator server <diamond/emerald>§c.");
            player.sendMessage("§cSintaxe incorreta, use §e/setup villager <shop/upgrade> <time>§c.");
            return false;
        }

        if (args[0].equalsIgnoreCase("bed")) {
            if (args.length == 2) {
                String color = args[1];
                TeamColor teamColor;

                try {
                    teamColor = TeamColor.valueOf(color.toUpperCase());
                } catch (Exception e) {
                    player.sendMessage("§eTimes disponíveis: ");
                    player.sendMessage(" ");

                    for (TeamColor value : TeamColor.values()) {
                        player.sendMessage(ChatColor.valueOf(value.name()) + value.name());
                    }
                    player.sendMessage(" ");
                    return false;
                }
                player.sendMessage("§aLocalização da cama do time §f" + teamColor.name() + " §asalva.");
                locationManager.put(new Locations(LocationType.BED, player.getLocation(), teamColor));
                return true;
            } else {
                player.sendMessage("§cSintaxe incorreta, use §e/setup bed <time>§c.");
                return false;
            }
        }
        if (args[0].equalsIgnoreCase("spawn")) {
            if (args.length == 2) {
                String color = args[1];
                TeamColor teamColor;

                try {
                    teamColor = TeamColor.valueOf(color.toUpperCase());
                } catch (Exception e) {
                    player.sendMessage("§eTimes disponíveis: ");
                    player.sendMessage(" ");

                    for (TeamColor value : TeamColor.values()) {
                        player.sendMessage(ChatColor.valueOf(value.name()) + value.name());
                    }
                    player.sendMessage(" ");
                    return false;
                }
                player.sendMessage("§aLocalização do spawn do time §f" + teamColor.name() + " §asalva.");
                locationManager.put(new Locations(LocationType.SPAWN, player.getLocation(), teamColor));
                return true;
            } else {
                player.sendMessage("§cSintaxe incorreta, use §e/setup spawn <time>§c.");
                return false;
            }
        }
        if (args[0].equalsIgnoreCase("generator")) {
            if (args[1].equalsIgnoreCase("team")) {
                if (args.length == 4) {
                    String color = args[2];
                    TeamColor teamColor;
                    LocationType locationType;

                    try {
                        teamColor = TeamColor.valueOf(color.toUpperCase());
                    } catch (Exception e) {
                        player.sendMessage("§eCores disponíveis: ");
                        player.sendMessage(" ");

                        for (TeamColor value : TeamColor.values()) {
                            player.sendMessage(ChatColor.valueOf(value.name()) + value.name());
                        }
                        player.sendMessage(" ");
                        return false;
                    }
                    switch (args[3].toLowerCase()) {
                        case "iron":
                            locationType = LocationType.IRON_GENERATOR;
                            break;
                        case "gold":
                            locationType = LocationType.GOLD_GENERATOR;
                            break;
                        default:
                            player.sendMessage("§eGeradores disponíveis: iron, gold.");
                            return false;
                    }
                    player.sendMessage("§aLocalização do gerador de §f" + (locationType == LocationType.IRON_GENERATOR ? "ferro" : "ouro") + " §ado time §f" + teamColor.name() + " §asalva.");
                    locationManager.put(new Locations(locationType, player.getLocation(), teamColor));
                    return true;
                } else {
                    player.sendMessage("§cSintaxe incorreta, use §e/setup generator team <time> <iron/gold>§c.");
                    return false;
                }
            } else if (args[1].equalsIgnoreCase("server")) {
                if (args.length == 3) {
                    LocationType locationType;

                    switch (args[2].toLowerCase()) {
                        case "diamond":
                            locationType = LocationType.DIAMOND_GENERATOR;
                            break;
                        case "emerald":
                            locationType = LocationType.EMERALD_GENERATOR;
                            break;
                        default:
                            player.sendMessage("§eGeradores disponíveis: diamond, emerald.");
                            return false;
                    }
                    locationManager.put(new Locations(locationType, player.getLocation(), null));
                    player.sendMessage("§aLocalização do gerador de §f" + (locationType == LocationType.DIAMOND_GENERATOR ? "diamante" : "esmeralda") + " §ado servidor salva.");
                    return true;
                } else {
                    player.sendMessage("§cSintaxe incorreta, use §e/setup generator server <diamond/emerald>§c.");
                    return false;
                }
            }
        }
        if (args[0].equalsIgnoreCase("villager")) {
            if (args.length == 3) {
                LocationType locationType;
                String color = args[2];
                TeamColor teamColor;

                if (args[1].equalsIgnoreCase("shop")) {
                    locationType = LocationType.SHOP_VILLAGER;
                } else {
                    locationType = LocationType.UPGRADE_VILLAGER;
                }
                try {
                    teamColor = TeamColor.valueOf(color.toUpperCase());
                } catch (Exception e) {
                    player.sendMessage("§eTimes disponíveis: ");
                    player.sendMessage(" ");

                    for (TeamColor value : TeamColor.values()) {
                        player.sendMessage(ChatColor.valueOf((value.name() == "CYAN" ? "AQUA" : value.name())) + value.name());
                    }
                    player.sendMessage(" ");
                    return false;
                }
                player.sendMessage("§aLocalização do villager §f" + locationType.name().substring(0, 5).toLowerCase() + " §ado time §f" + teamColor.name() + " §asalva.");
                locationManager.put(new Locations(locationType, player.getLocation(), teamColor));
                return true;
            } else {
                player.sendMessage("§cSintaxe incorreta, use §e/setup villager <shop/upgrade> <time>§c.");
                return false;
            }
        }
        if (args[0].equalsIgnoreCase("lobby")) {
            locationManager.put(new Locations(LocationType.LOBBY, player.getLocation(), null));
            player.sendMessage("§aLocalização de entrada salva.");
        }
        return false;
    }
}
