package br.edu.ifpb.dac.campus_events.repository;

import br.edu.ifpb.dac.campus_events.entity.Event;
import br.edu.ifpb.dac.campus_events.entity.Room;
import br.edu.ifpb.dac.campus_events.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    private User user;
    private Room room1;
    private Room room2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(br.edu.ifpb.dac.campus_events.enums.Role.STUDENT);
        user = entityManager.persistAndFlush(user);

        // Create a temporary event for Room to satisfy the ManyToOne constraint
        Event tempEvent = new Event();
        tempEvent.setUser(user);
        tempEvent.setTitle("Temp Event");
        tempEvent.setDescription("Temp Description");
        tempEvent.setStartDate(LocalDateTime.now());
        tempEvent.setEndDate(LocalDateTime.now().plusHours(1));
        tempEvent.setCreatedAt(LocalDateTime.now());
        tempEvent.setStatus("ACTIVE");
        tempEvent = entityManager.persistAndFlush(tempEvent);

        room1 = new Room();
        room1.setName("Room 1");
        room1.setLocation("Location 1");
        room1.setResources("Projector");
        room1.setCapacity("50");
        room1.setEvent(tempEvent);
        room1 = entityManager.persistAndFlush(room1);

        room2 = new Room();
        room2.setName("Room 2");
        room2.setLocation("Location 2");
        room2.setResources("Whiteboard");
        room2.setCapacity("30");
        room2.setEvent(tempEvent);
        room2 = entityManager.persistAndFlush(room2);
    }

    @Test
    void findOverlappingEvents_ShouldReturnEventsWithOverlap() {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        
        Event existingEvent = createEvent(room1, baseTime, baseTime.plusHours(2), "ACTIVE");
        entityManager.persistAndFlush(existingEvent);

        LocalDateTime newStartDate = baseTime.plusHours(1);
        LocalDateTime newEndDate = baseTime.plusHours(3);

        // Act
        List<Event> overlappingEvents = eventRepository.findOverlappingEvents(
                room1.getId(), newStartDate, newEndDate);

        // Assert
        assertThat(overlappingEvents).hasSize(1);
        assertThat(overlappingEvents.get(0).getId()).isEqualTo(existingEvent.getId());
    }

    @Test
    void findOverlappingEvents_ShouldReturnEmptyWhenNoOverlap() {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        
        Event existingEvent = createEvent(room1, baseTime, baseTime.plusHours(2), "ACTIVE");
        entityManager.persistAndFlush(existingEvent);

        LocalDateTime newStartDate = baseTime.plusHours(3);
        LocalDateTime newEndDate = baseTime.plusHours(4);

        // Act
        List<Event> overlappingEvents = eventRepository.findOverlappingEvents(
                room1.getId(), newStartDate, newEndDate);

        // Assert
        assertThat(overlappingEvents).isEmpty();
    }

    @Test
    void findOverlappingEvents_ShouldReturnEmptyForDifferentRoom() {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        
        Event existingEvent = createEvent(room1, baseTime, baseTime.plusHours(2), "ACTIVE");
        entityManager.persistAndFlush(existingEvent);

        LocalDateTime newStartDate = baseTime.plusHours(1);
        LocalDateTime newEndDate = baseTime.plusHours(3);

        // Act
        List<Event> overlappingEvents = eventRepository.findOverlappingEvents(
                room2.getId(), newStartDate, newEndDate);

        // Assert
        assertThat(overlappingEvents).isEmpty();
    }

    @Test
    void findOverlappingEvents_ShouldExcludeCancelledEvents() {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        
        Event cancelledEvent = createEvent(room1, baseTime, baseTime.plusHours(2), "CANCELLED");
        entityManager.persistAndFlush(cancelledEvent);

        LocalDateTime newStartDate = baseTime.plusHours(1);
        LocalDateTime newEndDate = baseTime.plusHours(3);

        // Act
        List<Event> overlappingEvents = eventRepository.findOverlappingEvents(
                room1.getId(), newStartDate, newEndDate);

        // Assert
        assertThat(overlappingEvents).isEmpty();
    }

    @Test
    void findOverlappingEventsExcludingEvent_ShouldExcludeSpecificEvent() {
        // Arrange
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        
        Event event1 = createEvent(room1, baseTime, baseTime.plusHours(2), "ACTIVE");
        event1 = entityManager.persistAndFlush(event1);
        
        Event event2 = createEvent(room1, baseTime.plusHours(3), baseTime.plusHours(5), "ACTIVE");
        event2 = entityManager.persistAndFlush(event2);

        LocalDateTime newStartDate = baseTime.plusHours(1);
        LocalDateTime newEndDate = baseTime.plusHours(4);

        // Act
        List<Event> overlappingEvents = eventRepository.findOverlappingEventsExcludingEvent(
                room1.getId(), event1.getId(), newStartDate, newEndDate);

        // Assert
        assertThat(overlappingEvents).hasSize(1);
        assertThat(overlappingEvents.get(0).getId()).isEqualTo(event2.getId());
    }

    private Event createEvent(Room room, LocalDateTime startDate, LocalDateTime endDate, String status) {
        Event event = new Event();
        event.setUser(user);
        event.setRoom(room);
        event.setTitle("Test Event");
        event.setDescription("Test Description");
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setCreatedAt(LocalDateTime.now());
        event.setStatus(status);
        return event;
    }
}

