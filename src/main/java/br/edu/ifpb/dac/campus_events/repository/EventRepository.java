package br.edu.ifpb.dac.campus_events.repository;

import br.edu.ifpb.dac.campus_events.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.room.id = :roomId " +
           "AND e.status != 'CANCELLED' " +
           "AND ((e.startDate < :endDate AND e.endDate > :startDate))")
    List<Event> findOverlappingEvents(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT e FROM Event e WHERE e.room.id = :roomId " +
           "AND e.id != :eventId " +
           "AND e.status != 'CANCELLED' " +
           "AND ((e.startDate < :endDate AND e.endDate > :startDate))")
    List<Event> findOverlappingEventsExcludingEvent(
            @Param("roomId") Long roomId,
            @Param("eventId") Long eventId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}

