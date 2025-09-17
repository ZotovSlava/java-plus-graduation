package ru.practicum;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationRequestDto;
import ru.practicum.service.CompilationService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/compilations")
public class CompilationPublicController {
    private final CompilationService compilationService;

    @GetMapping
    public ResponseEntity<List<CompilationRequestDto>> get(
            @RequestParam(defaultValue = "false") Boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        List<CompilationRequestDto> compilations = compilationService.get(pinned, from, size);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(compilations);
    }

    @GetMapping("/{compId}")
    public ResponseEntity<CompilationRequestDto> getById(@PathVariable Long compId) {
        CompilationRequestDto compilationRequestDto = compilationService.getById(compId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(compilationRequestDto);
    }
}
