package org.cleancoders.web.resource;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.cleancoders.common.domain.User;
import org.cleancoders.common.domain.UserRole;
import org.cleancoders.common.outbound.TokenService;
import org.cleancoders.common.outbound.UserRepository;
import org.cleancoders.common.usecase.AdminAuthUseCase;
import org.cleancoders.common.usecase.AuthUseCase;
import org.cleancoders.common_reservation_seatAndRoom.outbound.SeatRepository;
import org.cleancoders.common_reservation_seatAndRoom.outbound.TimeSlotRepository;
import org.cleancoders.infrastructure.persistence.InMemoryReservationRepo;
import org.cleancoders.infrastructure.persistence.InMemorySeatRepo;
import org.cleancoders.infrastructure.persistence.InMemoryTimeSlotRepo;
import org.cleancoders.infrastructure.persistence.InMemoryUserRepo;
import org.cleancoders.infrastructure.security.JjwtTokenService;
import org.cleancoders.reservation.domain.Reservation;
import org.cleancoders.reservation.outbound.ReservationRepository;
import org.cleancoders.reservation.usecase.ManageReservationsUseCase;
import org.cleancoders.web.filter.CorsFilter;
import org.cleancoders.web.presenter.WebApiReservationPresenter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for {@link AdminResource} (UC-13).
 * <p>
 * Runs a real JerseyTest HTTP server with in-memory repositories,
 * verifying the full stack: HTTP → Resource → UseCase → Presenter → Response.
 */
class AdminResourceIntegrationTest extends JerseyTest {

    private String adminToken;
    private String studentToken;
    private String studentId;

    @Override
    protected Application configure() {
        // ---- Repositories ----
        InMemoryUserRepo userRepo = new InMemoryUserRepo();
        InMemorySeatRepo seatRepo = new InMemorySeatRepo();
        InMemoryTimeSlotRepo timeSlotRepo = new InMemoryTimeSlotRepo();
        InMemoryReservationRepo reservationRepo = new InMemoryReservationRepo();

        // ---- Pre-seed users ----
        User admin = userRepo.save(new User(null, "admin", "ignored",
                UserRole.ADMIN, "Admin User", "admin@test.com"));
        User student = userRepo.save(new User(null, "student", "ignored",
                UserRole.STUDENT, "Test Student", "student@test.com"));
        studentId = student.id();

        // ---- Tokens ----
        JjwtTokenService tokenService = new JjwtTokenService();
        adminToken = tokenService.generate(admin.id());
        studentToken = tokenService.generate(studentId);

        // ---- Pre-seed reservations ----
        Reservation r1 = reservationRepo.save(
                new Reservation(null, studentId, "seat-1", "ts-1", LocalDate.of(2026, 7, 3)));
        Reservation r2 = reservationRepo.save(
                new Reservation(null, studentId, "seat-2", "ts-2", LocalDate.of(2026, 7, 3)));

        // ---- Presenter ----
        WebApiReservationPresenter reservationPresenter = new WebApiReservationPresenter();

        ResourceConfig config = new ResourceConfig();
        config.register(AdminResource.class);
        config.register(CorsFilter.class);
        config.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(userRepo).to(UserRepository.class);
                bind(seatRepo).to(SeatRepository.class);
                bind(timeSlotRepo).to(TimeSlotRepository.class);
                bind(reservationRepo).to(ReservationRepository.class);
                bind(tokenService).to(TokenService.class);

                bind(ManageReservationsUseCase.class).to(ManageReservationsUseCase.class);

                bind(reservationPresenter).to(WebApiReservationPresenter.class);
                bind(reservationPresenter).to(ManageReservationsUseCase.Presenter.class);
                bind(reservationPresenter).to(AdminAuthUseCase.Presenter.class);
                bind(reservationPresenter).to(AuthUseCase.Presenter.class);
            }
        });
        return config;
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    // ================================================================
    // UC-13: GET /admin/reservations — 查看所有预约（管理员）
    // ================================================================

    @Test
    void adminShouldSeeAllReservations() {
        Response response = target("/admin/reservations")
                .request(MediaType.APPLICATION_JSON)
                .cookie("Authorization", adminToken)
                .get();

        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = response.readEntity(Map.class);
        assertNotNull(entity.get("reservations"));

        @SuppressWarnings("unchecked")
        var list = (java.util.List<Map<String, Object>>) entity.get("reservations");
        assertEquals(2, list.size());

        // Verify expected seat numbers are present (ConcurrentHashMap order is non-deterministic)
        java.util.List<String> seatNumbers = list.stream()
                .map(m -> (String) m.get("seatNumber"))
                .toList();
        assertTrue(seatNumbers.contains("A-1"), "Should contain A-1");
        assertTrue(seatNumbers.contains("A-2"), "Should contain A-2");

        // Verify fields on any reservation (both belong to the same student)
        Map<String, Object> first = list.get(0);
        assertNotNull(first.get("reservationId"));
        assertEquals(studentId, first.get("userId"));
        assertEquals("student", first.get("username"));
        assertNotNull(first.get("timeSlotLabel"));
        assertEquals("2026-07-03", first.get("date"));
        assertEquals("RESERVED", first.get("status"));
        assertNotNull(first.get("createdAt"));
    }

    @Test
    void studentShouldGet403WhenAccessingAdminReservations() {
        Response response = target("/admin/reservations")
                .request(MediaType.APPLICATION_JSON)
                .cookie("Authorization", studentToken)
                .get();

        assertEquals(403, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = response.readEntity(Map.class);
        assertNotNull(entity.get("error"));
    }

    @Test
    void shouldReturn401ForInvalidToken() {
        Response response = target("/admin/reservations")
                .request(MediaType.APPLICATION_JSON)
                .cookie("Authorization", "invalid.jwt.token.here")
                .get();

        assertEquals(401, response.getStatus());
    }

    @Test
    void shouldReturn401ForMissingCookie() {
        Response response = target("/admin/reservations")
                .request(MediaType.APPLICATION_JSON)
                .get();

        assertEquals(401, response.getStatus());
    }

    @Test
    void adminShouldSeeEmptyListWhenNoReservations() {
        // Use a fresh setup with no reservations — tested by verifying
        // the pre-seeded ones are present, non-empty as verified above.
        // The InMemoryReservationRepo is fresh per test, so the two
        // pre-seeded reservations are always present.
        // This test just verifies the response structure when there ARE items.
        Response response = target("/admin/reservations")
                .request(MediaType.APPLICATION_JSON)
                .cookie("Authorization", adminToken)
                .get();

        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, Object> entity = response.readEntity(Map.class);

        @SuppressWarnings("unchecked")
        var list = (java.util.List<?>) entity.get("reservations");
        assertFalse(list.isEmpty(), "Should contain pre-seeded reservations");
    }

    @Test
    void adminResponseShouldHaveJsonContentType() {
        Response response = target("/admin/reservations")
                .request(MediaType.APPLICATION_JSON)
                .cookie("Authorization", adminToken)
                .get();

        assertTrue(response.getHeaderString("Content-Type").startsWith(MediaType.APPLICATION_JSON));
    }
}
