package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaServiceImpl implements MpaService {

    private final MpaDbStorage mpaDbStorage;

    @Override
    public List<Mpa> findAll() {
        return mpaDbStorage.findAll();
    }

    @Override
    public Mpa findById(Long id) {
        Mpa mpa = mpaDbStorage.findById(id);
        if (mpa == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "MPA с id " + id + " не найден"
            );
        }
        return mpa;
    }
}
