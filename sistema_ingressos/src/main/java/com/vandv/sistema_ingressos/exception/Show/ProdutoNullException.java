package com.vandv.sistema_ingressos.exception.Show;

import com.vandv.sistema_ingressos.exception.CommerceException;

public class ProdutoNullException extends CommerceException {
    public ProdutoNullException() {super("Produto está vazio!");}
}
