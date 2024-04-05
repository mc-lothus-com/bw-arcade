package com.lothus.engines.bedwars.service;

import com.lothus.engines.bedwars.Bedwars;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

public class Services {

    private static Bedwars plugin;

    private Services(Bedwars plugin) {
        Services.plugin = plugin;
    }

    public static Services create(Bedwars plugin) {
        return new Services(plugin);
    }

    public static <T> T add(Class<T> clazz, T instance) {
        Validate.notNull(clazz, "clazz can't be null.");
        Validate.notNull(instance, "instance can't be null.");

        plugin.getServer().getServicesManager().register(clazz, instance, plugin, ServicePriority.Normal);
        return instance;
    }

    public static <T> T remove(Class<T> clazz, T instance) {
        Validate.notNull(clazz, "clazz can't be null.");
        Validate.notNull(instance, "instance can't be null.");

        plugin.getServer().getServicesManager().unregister(clazz, instance);
        return instance;
    }

    public static <T> T add(Class<T> clazz, T instance, ServicePriority priority) {
        Validate.notNull(clazz, "clazz can't be null.");
        Validate.notNull(instance, "instance can't be null.");
        Validate.notNull(priority, "priority can't be null.");

        plugin.getServer().getServicesManager().register(clazz, instance, plugin, priority);
        return instance;
    }

    public static <T> T get(Class<T> clazz) {
        Validate.notNull(clazz, "clazz can't be null.");
        RegisteredServiceProvider<T> registration = plugin.getServer().getServicesManager().getRegistration(clazz);

        return registration == null ? null : registration.getProvider();
    }

    public Services() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}