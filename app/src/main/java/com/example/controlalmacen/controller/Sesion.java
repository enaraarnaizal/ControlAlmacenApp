package com.example.controlalmacen.controller;

import com.example.controlalmacen.model.Usuario;

public class Sesion {

    private static Usuario usuario;
    private static long lastActivityMs = 0;

    public static void setUsuario(Usuario u) {
        usuario = u;
        lastActivityMs = System.currentTimeMillis(); //arranca el contador
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void clear() {
        usuario = null;
        lastActivityMs = 0;
    }

    public static void touch() {
        lastActivityMs = System.currentTimeMillis();
    }

    public static boolean isExpired() {
        // 10 minutos = 600000
        return usuario != null && (System.currentTimeMillis() - lastActivityMs) > 600000;
    }
}