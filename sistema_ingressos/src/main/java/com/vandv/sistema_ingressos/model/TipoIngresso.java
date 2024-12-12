package com.vandv.sistema_ingressos.model;

import jdk.jfr.Name;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
public enum TipoIngresso {
    NORMAL(1.0),
    VIP(2.0),
    MEIA_ENTRADA(0.5);

    private final double multiplicador;


    public double calcularPreco(double precoNormal) {
        return precoNormal * multiplicador;
    }
}

