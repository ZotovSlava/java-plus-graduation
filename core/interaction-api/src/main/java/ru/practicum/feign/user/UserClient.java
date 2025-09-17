package ru.practicum.feign.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.UserCreateDto;
import ru.practicum.dto.user.UserRequestDto;

import java.util.List;

@FeignClient(name = "user", path = "/admin/users")
public interface UserClient {
    @PostMapping
    UserRequestDto create(@RequestBody UserCreateDto userCreateDto);

    @GetMapping
    List<UserRequestDto> get(
            @RequestParam(required = false) List<Integer> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    );

    @GetMapping("/{userId}")
    UserRequestDto getById(@PathVariable Long userId);

    @DeleteMapping("/{userId}")
    void delete(@PathVariable("userId") Long userId);
}
