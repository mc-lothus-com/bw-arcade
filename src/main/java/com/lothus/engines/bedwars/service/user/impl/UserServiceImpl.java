package com.lothus.engines.bedwars.service.user.impl;

import com.lothus.engines.bedwars.service.user.UserService;
import com.lothus.engines.bedwars.user.User;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserServiceImpl implements UserService {

    private final Set<User> users = new HashSet<>();

    @Override
    public void create(User model) {
        if (get(model.getUuid()) == null) {
            users.add(model);
        }
    }

    @Override
    public void remove(UUID s) {
        users.remove(search(s).findAny().orElse(null));
    }

    @Override
    public User get(UUID s) {
        return search(s).findAny().orElse(null);
    }

    @Override
    public Stream<User> search(UUID s) {
        return users.stream().filter(user -> user.getUuid().equals(s));
    }

    @Override
    public Set<User> all() {
        return users;
    }

    @Override
    public Set<User> spectators() {
        return users.stream().filter(user -> user != null && user.isSpectator() && user.getPlayer() != null).collect(Collectors.toSet());
    }

    @Override
    public Set<User> playing() {
        return users.stream().filter(user -> user != null && !user.isSpectator() && user.getPlayer() != null).collect(Collectors.toSet());
    }
}
