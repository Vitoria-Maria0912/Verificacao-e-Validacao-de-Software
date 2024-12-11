package com.vandv.sistema_ingressos.exception.Show;

import com.vandv.sistema_ingressos.exception.CommerceException;

public class IdInvalidoException extends CommerceException {
    public IdInvalidoException() {super("Id passado está incorreto!");}
}