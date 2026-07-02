package org.cleancoders.seatandroom.usecase;

import org.cleancoders.seatandroom.domain.RoomStatus;
import org.cleancoders.seatandroom.domain.StudyRoom;
import org.cleancoders.seatandroom.outbound.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ListRoomsUseCaseTest
{

    private ListRoomsUseCase useCase;
    private StubRoomRepo roomRepo;
    private StubPresenter presenter;

    @BeforeEach
    void setUp()
    {
        roomRepo = new StubRoomRepo();
        presenter = new StubPresenter();
        useCase = new ListRoomsUseCase();
        useCase.roomRepo = roomRepo;
        useCase.presenter = presenter;
    }

    @Test
    void shouldReturnOnlyOpenRoomsAndPresentThem()
    {
        StudyRoom open1 = new StudyRoom("r1", "A", "L1", 10, RoomStatus.OPEN);
        StudyRoom closed = new StudyRoom("r2", "B", "L2", 10, RoomStatus.CLOSED);
        StudyRoom maint = new StudyRoom("r3", "C", "L3", 10, RoomStatus.MAINTENANCE);
        StudyRoom open2 = new StudyRoom("r4", "D", "L4", 10, RoomStatus.OPEN);
        roomRepo.add(open1, closed, maint, open2);

        var output = useCase.execute(new ListRoomsUseCase.Request());

        assertEquals(List.of(open1, open2), output.rooms());
        assertEquals(List.of(open1, open2), presenter.presentedRooms.get());
    }

    @Test
    void shouldReturnEmptyListWhenNoOpenRooms()
    {
        roomRepo.add(new StudyRoom("r1", "A", "L1", 10, RoomStatus.CLOSED));

        var output = useCase.execute(new ListRoomsUseCase.Request());

        assertTrue(output.rooms().isEmpty());
        assertTrue(presenter.presentedRooms.get().isEmpty());
    }

    @Test
    void shouldQueryRepoWithOpenStatusOnly()
    {
        roomRepo.add(new StudyRoom("r1", "A", "L1", 10, RoomStatus.OPEN));

        useCase.execute(new ListRoomsUseCase.Request());

        assertEquals(RoomStatus.OPEN, roomRepo.lastQueriedStatus.get());
    }

    // --- Stubs ---

    static class StubRoomRepo implements RoomRepository
    {
        private final java.util.Map<String, StudyRoom> rooms = new java.util.LinkedHashMap<>();
        final AtomicReference<RoomStatus> lastQueriedStatus = new AtomicReference<>();

        void add(StudyRoom... toAdd)
        {
            for (StudyRoom r : toAdd)
            {
                rooms.put(r.id(), r);
            }
        }

        @Override
        public List<StudyRoom> findByStatus(RoomStatus status)
        {
            lastQueriedStatus.set(status);
            return rooms.values().stream()
                    .filter(r -> r.status() == status)
                    .toList();
        }

        @Override
        public Optional<StudyRoom> findById(String id)
        {
            return Optional.ofNullable(rooms.get(id));
        }

        @Override
        public StudyRoom save(StudyRoom room)
        {
            rooms.put(room.id(), room);
            return room;
        }
    }

    static class StubPresenter implements ListRoomsUseCase.Presenter
    {
        final AtomicReference<List<StudyRoom>> presentedRooms = new AtomicReference<>();

        @Override
        public void presentRooms(List<StudyRoom> rooms)
        {
            presentedRooms.set(rooms);
        }
    }
}
