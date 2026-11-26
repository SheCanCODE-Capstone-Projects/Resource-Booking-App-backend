package com.reserveflow.service;

import com.reserveflow.dto.ReservationRequest;
import com.reserveflow.enums.ReservationStatus;
import com.reserveflow.model.Reservation;
import com.reserveflow.model.User;
import com.reserveflow.repository.ReservationRepository;
import com.reserveflow.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;


@Service
        public class ReservationService {

            private final ReservationRepository reservationRepository;
            private final ResourceService resourceService;
            private final UserRepository userRepo;

            public ReservationService(ReservationRepository reservationRepository, ResourceService resourceService, UserRepository userRepo) {
                this.resourceService =resourceService;
                this.userRepo = userRepo;
                this.reservationRepository= reservationRepository;
            }

        @Transactional
        public Reservation createReservation(int userId, int resourceId, LocalDateTime startTime, LocalDateTime endTime) {

            userRepo.findById((long) userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));

            resourceService.getById((long) resourceId);

            if (startTime == null || endTime == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time and end time are required");
            }
            if (!endTime.isAfter(startTime)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
            }
            if (startTime.isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time cannot be in the past");
            }

            List<Reservation> overlapping = reservationRepository.findOverlapping(resourceId, startTime, endTime);
            if (!overlapping.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This time is already booked");
            }

            // save (the database rule stops two users booking at the same moment)
            Reservation reservation = new Reservation(userId, resourceId, startTime, endTime);
            try {
                return reservationRepository.saveAndFlush(reservation);
            } catch (DataIntegrityViolationException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This time is already booked");
            }
        }

        public List<Reservation> getMyReservations(int userId) {
            userRepo.findById((long) userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));

            return reservationRepository.findByUserId(userId);
        }

        public Reservation getReservationById(int reservationId, User user) {
                Long id= user.getId();
            Reservation reservation = reservationRepository.findById((long) reservationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found with ID: " + reservationId));

            checkAccess(reservation, user);
            return reservation;
        }

        @Transactional
        public Reservation cancelReservation(int reservationId, User user) {
            Reservation reservation = reservationRepository.findById((long) reservationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found with ID: " + reservationId));

            checkAccess(reservation, user);

            if ("CANCELED".equals(reservation.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This reservation has already been canceled.");
            }

            reservation.setStatus(ReservationStatus.valueOf("CANCELED"));
            return reservationRepository.save(reservation);
        }


        private void checkAccess(Reservation reservation, User user) {
             userRepo.findById(user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + user.getId()));

            boolean isOwner = reservation.getId() == user.getId();
            boolean isAdmin = "ADMIN".equals(user.getRole());

            if (!isOwner && !isAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access this reservation.");
            }
        }
    }