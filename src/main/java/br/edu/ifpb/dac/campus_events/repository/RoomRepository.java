package br.edu.ifpb.dac.campus_events.repository;

import br.edu.ifpb.dac.campus_events.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}

