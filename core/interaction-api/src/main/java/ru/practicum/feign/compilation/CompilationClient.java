package ru.practicum.feign.compilation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationCreateDto;
import ru.practicum.dto.compilation.CompilationRequestDto;
import ru.practicum.dto.compilation.CompilationUpdateDto;

import java.util.List;

@FeignClient(name = "compilation")
public interface CompilationClient {
    @GetMapping("/compilations")
    List<CompilationRequestDto> get(
            @RequestParam(defaultValue = "false") Boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/compilations/compilations/{compId}")
    CompilationRequestDto getById(@PathVariable("compId") Long compId);

    @PostMapping("/compilations/compilations/admin/compilations")
    CompilationRequestDto create(@RequestBody CompilationCreateDto compilationCreateDto);

    @PatchMapping("/admin/compilations/{compId}")
    CompilationRequestDto update(@RequestBody CompilationUpdateDto compilationUpdateDto,
                                 @PathVariable("compId") Long compId);

    @DeleteMapping("/admin/compilations/{compId}")
    void delete(@PathVariable("compId") Long compId);
}
