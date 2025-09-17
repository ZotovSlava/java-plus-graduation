package ru.practicum.feign.compilation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationCreateDto;
import ru.practicum.dto.compilation.CompilationRequestDto;
import ru.practicum.dto.compilation.CompilationUpdateDto;

//@FeignClient(name = "compilation",   contextId = "compilationAdminClient", path = "/admin/compilations")
public interface CompilationAdminClient {
    @PostMapping("/admin/compilations")
    CompilationRequestDto create(@RequestBody CompilationCreateDto compilationCreateDto);

    @PatchMapping("/admin/compilations/{compId}")
    CompilationRequestDto update(@RequestBody CompilationUpdateDto compilationUpdateDto,
                                 @PathVariable("compId") Long compId);

    @DeleteMapping("/admin/compilations/{compId}")
    void delete(@PathVariable("compId") Long compId);
}
