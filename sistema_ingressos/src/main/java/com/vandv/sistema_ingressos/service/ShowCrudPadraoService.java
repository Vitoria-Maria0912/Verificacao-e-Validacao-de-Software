package com.vandv.sistema_ingressos.service;

import com.vandv.sistema_ingressos.dto.ShowPostDto;
import com.vandv.sistema_ingressos.model.Show;
import com.vandv.sistema_ingressos.repository.ShowRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowCrudPadraoService implements ShowCrudService{
    @Autowired
    ShowRepository showRepository;

    private ModelMapper modelMapper = new ModelMapper();
    @Override
    public Show showCreate(ShowPostDto showPostDto) {
        Show show = modelMapper.map(showPostDto, Show.class);
        System.out.println("oiiiiii");
        return showRepository.save(show);
    }

    @Override
    public void showDelete(Long id) {
        showRepository.deleteById(id);
    }

    @Override
    public Show showFindById(Long id) {
        return showRepository.findById(id).orElse(null);
    }

    @Override
    public List<Show> showFindAll() {
        return showRepository.findAll();
    }
}
