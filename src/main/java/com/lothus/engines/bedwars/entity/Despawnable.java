package com.lothus.engines.bedwars.entity;

import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.game.GameManager;
import com.lothus.engines.bedwars.team.Team;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class Despawnable {

    private final LivingEntity e;
    private Team team;
    private double despawn = 250.0;
    private int health;
    private UUID uuid;

    private GameManager api;

    public Despawnable(LivingEntity e, Team team, int despawn) {
        this.e = e;
        if (e == null) return;
        this.uuid = e.getUniqueId();
        this.team = team;

        if (despawn != 0) {
            this.despawn = despawn;
        }
        this.api = Bedwars.getInstance().getGameManager();

        api.getDespawnables().put(uuid, this);
        this.setName();
    }

    public void refresh() {
        if (e.isDead() || e == null || team == null) {
            api.getDespawnables().remove(uuid);
            return;
        }
        setName();

        despawn = (despawn - 0.1);
        if (despawn <= 0) {
            e.damage(e.getHealth() + 100);
            api.getDespawnables().remove(e.getUniqueId());
        }
    }

    private void setName() {
        int percentuale = (int) ((e.getHealth() * 100) / e.getMaxHealth());
        String s = String.valueOf(despawn);

        String name = team.getTeamColor().name() + " : §a" + s.substring(0, Math.min(s.length(), 6)) + "s §f- §b" + percentuale + "%";
        e.setCustomName(name);
        e.setCustomNameVisible(false);
    }

    public LivingEntity getEntity() {
        return e;
    }

    public Team getTeam() {
        return team;
    }

    public void destroy() {
        if (getEntity() != null) {
            getEntity().damage(Integer.MAX_VALUE);
        }
        team = null;
        api.getDespawnables().remove(uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LivingEntity) return ((LivingEntity) obj).getUniqueId().equals(e.getUniqueId());
        return false;
    }
}