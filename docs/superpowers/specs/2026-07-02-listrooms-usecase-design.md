# UC-04 ListRoomsUseCase 实现设计

> 端点:`GET /api/rooms` — 获取所有 OPEN 状态的自习室
> 权限:公开(所有用户,无需登录)
> 模块归属:SeatAndRoom

## 背景与现状

- `StudyRoom` 当前为 record,`status` 字段为 `String`,无任何生产代码引用(尚无 RoomRepo/UseCase/Resource)。
- 缺 `RoomRepository` 接口、`InMemoryRoomRepo` 实现、`ListRoomsUseCase`、`RoomResource`、对应 Presenter 与 DTO。
- 类职责清单将 `StudyRoom`、`RoomRepository`、`ListRoomsUseCase` 归属 SeatAndRoom 模块。
- 权限为"所有用户",对应 webapi 清单的"公开"——不继承 `AuthUseCase`。

## 设计

### 1. Domain 层

**新增 `RoomStatus` 枚举**(SeatAndRoom `domain` 包):

```java
public enum RoomStatus { OPEN, CLOSED, MAINTENANCE }
```

**修改 `StudyRoom`**:`status` 字段 `String` → `RoomStatus`。

> 破坏性改动,但当前 `StudyRoom` 无生产引用,波及面为零。富领域模型约定——与 `SeatStatus`/`ReservationStatus` 一致,后续 UC-06 开关状态、状态校验更安全。

### 2. Outbound 层 — 新增 `RoomRepository`

SeatAndRoom `outbound` 包:

```java
public interface RoomRepository {
    List<StudyRoom> findByStatus(RoomStatus status);
    Optional<StudyRoom> findById(String id);
    StudyRoom save(StudyRoom room);
}
```

- `findByStatus` — UC-04 的 OPEN 过滤。
- `findById` / `save` — UC-05 / UC-06 必将使用,接口契约一次成型,避免反复改接口。

### 3. UseCase 层 — 新增 `ListRoomsUseCase`

SeatAndRoom `usecase` 包。**公开用例,不继承 `AuthUseCase`**(无认证):

```java
public class ListRoomsUseCase {
    @Inject RoomRepository roomRepo;
    @Inject Presenter presenter;

    public Output execute(Request req) {
        var rooms = roomRepo.findByStatus(RoomStatus.OPEN);
        presenter.presentRooms(rooms);
        return new Output(rooms);
    }

    public record Request() {}
    public record Output(List<StudyRoom> rooms) {}
    public interface Presenter { void presentRooms(List<StudyRoom> rooms); }
}
```

- **Request 保留为空 record**:无入参,但保持与其它 UseCase 一致的可扩展形态(未来可能加分页/筛选参数)。
- 无错误分支:空列表返回空(200 + `[]`),不设 404。

### 4. Infrastructure — 新增 `InMemoryRoomRepo`

Infrastructure `persistence` 包,`@Singleton`,实现 `RoomRepository`,内部 `ConcurrentHashMap`。

### 5. WebApi 层

- **`RoomResource`**(WebApi `resource`):`@Path("/rooms")`,`GET /` → 200 + 房间列表。公开,无 Authorization 头。沿用现有 DTO + Swagger 注解风格。
- **`WebApiRoomPresenter`**(WebApi `presenter`):`@Singleton` + `ThreadLocal<Response>`,实现 `ListRoomsUseCase.Presenter`,把 room 列表映射为 DTO 列表写入 200 响应。
- **DTO**(WebApi `dto/room`):`RoomResponse`(id / name / location / capacity / status)。

### 6. AppBinder 绑定

追加:
- `ListRoomsUseCase`
- `WebApiRoomPresenter` → `ListRoomsUseCase.Presenter`
- `InMemoryRoomRepo` → `RoomRepository`(Singleton)

### 7. 测试

- **`ListRoomsUseCaseTest`**(SeatAndRoom 模块):Stub `RoomRepository`,验证:
  - 只返回 OPEN 房间(混入 CLOSED/MAINTENANCE 时被过滤)
  - presenter 收到正确的 OPEN 列表
  - 无 OPEN 房间时返回空列表(不报错)
- **`InMemoryRoomRepoTest`**(Infrastructure 模块):验证 `findByStatus` 过滤正确、`findById`/`save` 行为。

## 文件清单

```
SeatAndRoom/src/main/java/org/cleancoders/seatandroom/
  domain/StudyRoom.java        ← 改 status 类型
  domain/RoomStatus.java       ← 新增
  outbound/RoomRepository.java ← 新增
  usecase/ListRoomsUseCase.java ← 新增
SeatAndRoom/src/test/java/.../usecase/ListRoomsUseCaseTest.java ← 新增
Infrastructure/src/main/java/.../persistence/InMemoryRoomRepo.java ← 新增
Infrastructure/src/test/java/.../persistence/InMemoryRoomRepoTest.java ← 新增
WebApi/src/main/java/org/cleancoders/web/
  resource/RoomResource.java        ← 新增
  presenter/WebApiRoomPresenter.java ← 新增
  dto/room/RoomResponse.java        ← 新增
  binder/AppBinder.java             ← 追加绑定
```

## 依赖链

```
RoomResource
  └─→ ListRoomsUseCase
        ├─→ RoomRepository (Outbound) ←── InMemoryRoomRepo (Infrastructure)
        ├─→ Presenter (内嵌) ←── WebApiRoomPresenter (WebApi)
        └─→ StudyRoom / RoomStatus (Domain)
```
