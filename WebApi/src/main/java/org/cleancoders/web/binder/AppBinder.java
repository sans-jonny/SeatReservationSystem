package org.cleancoders.web.binder;

import jakarta.inject.Singleton;
import org.cleancoders.common.outbound.TokenService;
import org.cleancoders.common.outbound.UserRepository;
import org.cleancoders.common.usecase.AdminAuthUseCase;
import org.cleancoders.common.usecase.AuthUseCase;
import org.cleancoders.common.usecase.StudentAuthUseCase;
import org.cleancoders.common_reservation_seatAndRoom.outbound.SeatRepository;
import org.cleancoders.common_reservation_seatAndRoom.outbound.TimeSlotRepository;
import org.cleancoders.infrastructure.persistence.testdata.*;
import org.cleancoders.infrastructure.security.BCryptPasswordEncoder;
import org.cleancoders.infrastructure.security.JjwtTokenService;
import org.cleancoders.reservation.outbound.ReservationRepository;
import org.cleancoders.reservation.usecase.CheckInUseCase;
import org.cleancoders.reservation.usecase.ReserveUseCase;
import org.cleancoders.seatandroom.outbound.RoomRepository;
import org.cleancoders.seatandroom.usecase.ListRoomsUseCase;
import org.cleancoders.seatandroom.usecase.ListSeatsUseCase;
import org.cleancoders.seatandroom.usecase.ManageRoomsUseCase;
import org.cleancoders.userandauth.outbound.PasswordEncoder;
import org.cleancoders.userandauth.usecase.GetMeUseCase;
import org.cleancoders.userandauth.usecase.LoginUseCase;
import org.cleancoders.userandauth.usecase.RegisterUseCase;
import org.cleancoders.web.presenter.WebApiAuthPresenter;
import org.cleancoders.web.presenter.WebApiAdminPresenter;
import org.cleancoders.web.presenter.WebApiCommonPresenter;
import org.cleancoders.web.presenter.WebApiReservationPresenter;
import org.cleancoders.web.presenter.WebApiRoomPresenter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * HK2 dependency injection binder.
 * Binds outbound interface implementations from Infrastructure module
 * to their corresponding interfaces defined in business modules.
 * <p>
 * Binding rules:
 * - bind(Implementation.class).to(Interface.class); — create new instance per injection (PerLookup)
 * - bind(instance).to(Contract.class); — share the same instance across contracts
 */
public class AppBinder extends AbstractBinder
{

    @Override
    protected void configure()
    {
        // === UserAndAuth ===
        // === UseCases ===
        bind(LoginUseCase.class).to(LoginUseCase.class);
        bind(RegisterUseCase.class).to(RegisterUseCase.class);
        bind(GetMeUseCase.class).to(GetMeUseCase.class);
        // === Presenters ===
        WebApiAuthPresenter authPresenter = new WebApiAuthPresenter();
        bind(authPresenter).to(WebApiAuthPresenter.class);
        bind(authPresenter).to(LoginUseCase.Presenter.class);
        bind(authPresenter).to(RegisterUseCase.Presenter.class);
        bind(authPresenter).to(GetMeUseCase.Presenter.class);
        WebApiCommonPresenter commonPresenter = new WebApiAuthPresenter();
        bind(commonPresenter).to(WebApiCommonPresenter.class);
        bind(commonPresenter).to(StudentAuthUseCase.Presenter.class);
        bind(commonPresenter).to(AdminAuthUseCase.Presenter.class);
        bind(commonPresenter).to(AuthUseCase.Presenter.class);
        // === Infrastructure ===
        bind(TestDataUserRepo.class).to(UserRepository.class).in(Singleton.class);
        bind(BCryptPasswordEncoder.class).to(PasswordEncoder.class).in(Singleton.class);
        bind(JjwtTokenService.class).to(TokenService.class).in(Singleton.class);

        // === SeatAndRoom ===
        // === UseCases ===
        bind(ListRoomsUseCase.class).to(ListRoomsUseCase.class);
        bind(ListSeatsUseCase.class).to(ListSeatsUseCase.class);
        bind(ManageRoomsUseCase.class).to(ManageRoomsUseCase.class);
        // === Presenters ===
        WebApiRoomPresenter roomPresenterInstance = new WebApiRoomPresenter();
        bind(roomPresenterInstance).to(WebApiRoomPresenter.class);
        bind(roomPresenterInstance).to(ListRoomsUseCase.Presenter.class);
        bind(roomPresenterInstance).to(ListSeatsUseCase.Presenter.class);
        // === Infrastructure ===
        bind(TestDataSeatRepo.class).to(SeatRepository.class).in(Singleton.class);
        bind(TestDataTimeSlotRepo.class).to(TimeSlotRepository.class).in(Singleton.class);
        bind(TestDataRoomRepo.class).to(RoomRepository.class).in(Singleton.class);

        // === Admin ===
        // === Presenters ===
        WebApiAdminPresenter adminPresenterInstance = new WebApiAdminPresenter();
        bind(adminPresenterInstance).to(WebApiAdminPresenter.class);
        bind(adminPresenterInstance).to(ManageRoomsUseCase.Presenter.class);

        // === Reservation ===
        // === UseCases ===
        bind(ReserveUseCase.class).to(ReserveUseCase.class);
        bind(CheckInUseCase.class).to(CheckInUseCase.class);
        // === Presenters ===
        WebApiReservationPresenter reservationPresenterInstance = new WebApiReservationPresenter();
        bind(reservationPresenterInstance).to(WebApiReservationPresenter.class);
        bind(reservationPresenterInstance).to(ReserveUseCase.Presenter.class);
        bind(reservationPresenterInstance).to(CheckInUseCase.Presenter.class);
        // === Infrastructure ===
        bind(TestDataReservationRepo.class).to(ReservationRepository.class).in(Singleton.class);

        // === SystemTask ===
        // bind(InMemoryTaskRepo.class).to(TaskRepository.class);
    }
}
