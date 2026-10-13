package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MpaServiceImpl implements MpaService {
    private final MpaStorage mpaStorage;

    @Override
    public List<Mpa> getAll() {
        return mpaStorage.findAll();
    }

    @Override
    public Optional<Mpa> getById(Long id) {
        return mpaStorage.findById(id);
    }

    @Override
    public List<Mpa> getAllByIds(List<Long> ids) {
        return mpaStorage.findAllByIds(ids);
    }
}
