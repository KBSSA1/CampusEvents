package br.edu.ifpb.dac.campus_events.web.controller;

import br.edu.ifpb.dac.campus_events.service.EventService;
import br.edu.ifpb.dac.campus_events.web.DTO.EventCreateDTO;
import br.edu.ifpb.dac.campus_events.web.DTO.EventResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponseDTO> create(@Valid @RequestBody EventCreateDTO dto) {
        EventResponseDTO event = eventService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> findAll() {
        List<EventResponseDTO> events = eventService.findAll();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> findById(@PathVariable Long id) {
        EventResponseDTO event = eventService.findById(id);
        return ResponseEntity.ok(event);
    }
}

