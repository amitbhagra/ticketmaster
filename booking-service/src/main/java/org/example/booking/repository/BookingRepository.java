package org.example.booking.repository;

import org.example.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(UUID userId);
    List<Booking> findByUserIdOrderByCreateTsDesc(UUID userId);
}
