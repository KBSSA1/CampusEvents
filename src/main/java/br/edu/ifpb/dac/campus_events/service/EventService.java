package br.edu.ifpb.dac.campus_events.service;

import br.edu.ifpb.dac.campus_events.entity.Event;
import br.edu.ifpb.dac.campus_events.entity.Room;
import br.edu.ifpb.dac.campus_events.entity.User;
import br.edu.ifpb.dac.campus_events.repository.EventRepository;
import br.edu.ifpb.dac.campus_events.repository.RoomRepository;
import br.edu.ifpb.dac.campus_events.repository.UserRepository;
import br.edu.ifpb.dac.campus_events.web.DTO.EventCreateDTO;
import br.edu.ifpb.dac.campus_events.web.DTO.EventResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public EventService(EventRepository eventRepository,
                       UserRepository userRepository,
                       RoomRepository roomRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional
    public EventResponseDTO create(EventCreateDTO dto) {
        // Validar datas
        if (dto.getEndDate().isBefore(dto.getStartDate()) || 
            dto.getEndDate().isEqual(dto.getStartDate())) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }

        // Buscar usuário
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        // Buscar sala
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada"));

        // Verificar overlap
        List<Event> overlappingEvents = eventRepository.findOverlappingEvents(
                room.getId(), dto.getStartDate(), dto.getEndDate());

        if (!overlappingEvents.isEmpty()) {
            throw new IllegalStateException("Já existe um evento agendado para este horário na sala selecionada");
        }

        // Criar evento
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setUser(user);
        event.setRoom(room);
        event.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        event.setCreatedAt(LocalDateTime.now());

        event = eventRepository.save(event);

        return convertToDTO(event);
    }

    public List<EventResponseDTO> findAll() {
        return eventRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EventResponseDTO findById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado"));
        return convertToDTO(event);
    }

    private EventResponseDTO convertToDTO(Event event) {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setCreatedAt(event.getCreatedAt());
        dto.setStatus(event.getStatus());
        dto.setUserId(event.getUser().getId());
        dto.setUserName(event.getUser().getName());
        dto.setRoomId(event.getRoom().getId());
        dto.setRoomName(event.getRoom().getName());
        return dto;
    }
}

