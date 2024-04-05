package com.lothus.engines.bedwars.team.color;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Color;

@Getter
@AllArgsConstructor
public enum TeamColor {

    RED("A", "993333", "§c§lVermelho", "Vermelho", Color.RED, 14, true),
    BLUE("B", "E5E533", "§9§lAzul", "Azul", Color.BLUE, 11, true),
    GREEN("C", "667F33", "§a§lVerde", "Verde", Color.GREEN, 5, true),
    YELLOW("D", "334CB2", "§e§lAmarelo", "Amarelo", Color.YELLOW, 4, true),
    CYAN("E", "3ED3E6", "§b§lCiano", "Ciano", Color.AQUA, 9, false),
    GRAY("F", "ACACAC", "§8§lCinza", "Cinza", Color.GRAY, 7, false),
    PURPLE("G", "9C3DC9", "§5§lRoxo", "Roxo", Color.PURPLE, 10, false),
    WHITE("H", "FFFFFF", "§f§lBranco", "Branco", Color.WHITE, 0, false);

    private final String position, color, coloredName, normalName;
    private final Color leather;
    private final int woolId;
    private final boolean isRanked;

}