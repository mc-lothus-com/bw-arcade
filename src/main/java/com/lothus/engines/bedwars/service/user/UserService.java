package com.lothus.engines.bedwars.service.user;

import com.lothus.engines.bedwars.user.User;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public interface UserService {

    void create(User model);

    void remove(UUID s);

    User get(UUID s);

    Stream<User> search(UUID s);

    Set<User> all();

    Set<User> spectators();

    Set<User> playing();

}
