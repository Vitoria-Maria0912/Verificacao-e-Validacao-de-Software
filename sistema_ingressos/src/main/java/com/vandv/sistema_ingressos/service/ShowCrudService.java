package com.vandv.sistema_ingressos.service;

import com.vandv.sistema_ingressos.dto.ShowPostDto;
import com.vandv.sistema_ingressos.model.Show;

import java.util.List;

public interface ShowCrudService {
    Show showCreate(ShowPostDto showPostDto);
    void showDelete(Long id);
    Show showFindById(Long id);
    List<Show> showFindAll();
}
