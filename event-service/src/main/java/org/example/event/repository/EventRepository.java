package org.example.event.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import org.example.event.domain.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

}
