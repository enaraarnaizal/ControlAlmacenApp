package com.example.controlalmacen.controller;

import com.example.controlalmacen.model.Usuario;

public class Sesion {

    private static Usuario usuario;
    private static boolean adminVerificado = false;

    private static long lastActivityMs = 0;

    public static void setUsuario(Usuario u) {
        usuario = u;
        lastActivityMs = System.currentTimeMillis();
    }
    public static boolean isAdminVerificado() {
        return adminVerificado;
    }

    public static void setAdminVerificado(boolean v) {
        adminVerificado = v;
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void clear() {
        usuario = null;
        lastActivityMs = 0;
        adminVerificado = false;
    }


    public static void touch() {
        lastActivityMs = System.currentTimeMillis();
    }

    public static boolean isExpired() {
        return usuario != null && (System.currentTimeMillis() - lastActivityMs) > 600000;
    }



}



