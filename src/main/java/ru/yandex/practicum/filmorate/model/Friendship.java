package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship {
    private Long id;
    private Long userId;
    private Long friendId;
    private FriendshipStatus status;
    private LocalDateTime createdAt;
}
