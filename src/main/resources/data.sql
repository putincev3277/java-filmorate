-- ============================================
-- Filmorate: справочные данные
-- ============================================

MERGE INTO mpa (id, name, description) KEY (id)
    VALUES
    (1, 'G', 'General audiences'),
    (2, 'PG', 'Parental guidance suggested'),
    (3, 'PG-13', 'Parents strongly cautioned'),
    (4, 'R', 'Restricted'),
    (5, 'NC-17', 'Adults only');

MERGE INTO genres (id, name) KEY (id)
    VALUES
    (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик');
