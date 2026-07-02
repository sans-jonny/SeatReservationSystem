package org.cleancoders.web.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.cleancoders.seatandroom.usecase.ManageRoomsUseCase;
import org.cleancoders.web.dto.admin.CreateRoomRequest;
import org.cleancoders.web.dto.common.ErrorResponse;
import org.cleancoders.web.dto.room.RoomResponse;
import org.cleancoders.web.presenter.WebApiAdminPresenter;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Admin", description = "管理员接口")
public class AdminResource
{

    @Inject
    ManageRoomsUseCase manageRoomsUseCase;

    @Inject
    WebApiAdminPresenter presenter;

    @POST
    @Path("/rooms")
    @Operation(summary = "创建自习室 (UC-06)", description = "管理员创建一个新的自习室，状态默认为 OPEN。")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "创建成功",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RoomResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token 无效或已过期",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "权限不足（非管理员角色）",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "自习室名称已存在")
    })
    public Response createRoom(
            @CookieParam("Authorization") String authCookie,
            CreateRoomRequest input)
    {
        manageRoomsUseCase.execute(new ManageRoomsUseCase.Request(
                authCookie, input.name(), input.location(), input.capacity()));
        return presenter.getResponse();
    }
}