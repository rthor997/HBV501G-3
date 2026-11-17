package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.AdminUpdateRouteDTO;
import org.example.klifurapp.DTO.requests.CreateRouteRequestDTO;
import org.example.klifurapp.DTO.responses.RouteDetailsResponseDTO;
import org.example.klifurapp.DTO.responses.RouteResponseDTO;
import org.example.klifurapp.entity.*;
import org.example.klifurapp.exception.ResourceNotFoundException;
import org.example.klifurapp.repository.ClimbRepository;
import org.example.klifurapp.repository.RouteRepository;
import org.example.klifurapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceImplementationTest {

    @Mock RouteRepository routeRepository;
    @Mock ClimbRepository climbRepository;
    @Mock UserRepository userRepository;
    @InjectMocks RouteServiceImplementation service;

    @Test
    void createRouteSavesAndReturnsResponse() {

        CreateRouteRequestDTO dto =
                new CreateRouteRequestDTO(
                        "Moon Board",
                        GradingSystem.V_SCALE,
                        "V5",
                        "Klifurhúsið",
                        ClimbingStyle.BOULDERING,
                        WallIncline.OVERHANG,
                        true,
                        "Technical"
                );

        when(routeRepository.save(any(Route.class)))
                .thenAnswer(invocation -> {
                    Route route = invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            route,
                            "id",
                            5L
                    );

                    return route;
                });

        RouteResponseDTO result =
                service.createRoute(dto);

        assertEquals(5L, result.id());
        assertEquals("Moon Board", result.name());
        assertEquals("V5", result.difficultyGrade());

        verify(routeRepository).save(any(Route.class));
    }

    @Test
    void createRouteSetsActiveTrueAndMapsAllFields() {

        CreateRouteRequestDTO dto =
                new CreateRouteRequestDTO(
                        "Moon Board",
                        GradingSystem.V_SCALE,
                        "V5",
                        "Klifurhúsið",
                        ClimbingStyle.BOULDERING,
                        WallIncline.OVERHANG,
                        true,
                        "Technical"
                );

        when(routeRepository.save(any(Route.class)))
                .thenAnswer(invocation -> {
                    Route route = invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            route,
                            "id",
                            5L
                    );

                    return route;
                });

        RouteResponseDTO result =
                service.createRoute(dto);

        assertEquals(5L, result.id());
        assertEquals("Moon Board", result.name());
        assertEquals("V5", result.difficultyGrade());
        assertEquals("V_SCALE", result.gradingSystem());
        assertEquals("BOULDERING", result.climbingStyle());
        assertEquals("OVERHANG", result.wallIncline());
        assertEquals("Technical", result.characteristics());
        assertTrue(result.indoor());
        assertEquals("Klifurhúsið", result.location());

        verify(routeRepository).save(argThat(route ->
                route.isActive()
                        && route.getName().equals("Moon Board")
                        && route.getDifficultyGrade().equals("V5")
        ));
    }

    @Test
    void createRouteAllowsNullWallIncline() {

        CreateRouteRequestDTO dto =
                new CreateRouteRequestDTO(
                        "Vertical Route",
                        GradingSystem.V_SCALE,
                        "V2",
                        "Gym",
                        ClimbingStyle.BOULDERING,
                        null,
                        true,
                        null
                );

        when(routeRepository.save(any(Route.class)))
                .thenAnswer(invocation -> {
                    Route route = invocation.getArgument(0);
                    ReflectionTestUtils.setField(route, "id", 7L);
                    return route;
                });

        RouteResponseDTO result =
                service.createRoute(dto);

        assertNull(result.wallIncline());
        assertNull(result.characteristics());
    }





    @Test
    void getRouteDetailsReturnsRouteWithoutFriends() {
        Route route = route();
        User currentUser = user(10L, "anna");
        when(routeRepository.findByIdAndActiveTrue(5L)).thenReturn(Optional.of(route));
        when(userRepository.findById(10L)).thenReturn(Optional.of(currentUser));

        RouteDetailsResponseDTO result = service.getRouteDetails(5L, 10L);

        assertEquals("Blue Wall", result.name());
        assertEquals("V3", result.difficultyGrade());
        assertEquals("V_SCALE", result.gradingSystem());
        assertTrue(result.friendsWhoCompleted().isEmpty());
        verify(climbRepository, never()).findByRouteIdAndUserIdIn(anyLong(), anyList());
    }

    @Test
    void getRouteDetailsIncludesFriendWhoCompletedIt() {
        Route route = route();
        User currentUser = user(10L, "anna");
        User friend = user(20L, "bob");
        currentUser.setFriends(List.of(friend));
        Climb completed = new Climb();
        completed.setUser(friend);

        when(routeRepository.findByIdAndActiveTrue(5L)).thenReturn(Optional.of(route));
        when(userRepository.findById(10L)).thenReturn(Optional.of(currentUser));
        when(climbRepository.findByRouteIdAndUserIdIn(5L, List.of(20L)))
                .thenReturn(List.of(completed));

        RouteDetailsResponseDTO result = service.getRouteDetails(5L, 10L);

        assertEquals(1, result.friendsWhoCompleted().size());
        assertEquals("bob", result.friendsWhoCompleted().get(0).username());
        assertEquals(20L, result.friendsWhoCompleted().get(0).id());
    }

    @Test
    void getMissingRouteReturns404() {
        when(routeRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () -> service.getRouteDetails(99L, 10L));

        assertEquals("Route not found", exception.getMessage());
    }

    @Test
    void getRouteDetailsWithMissingCurrentUserReturns404() {
        when(routeRepository.findByIdAndActiveTrue(5L)).thenReturn(Optional.of(route()));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () -> service.getRouteDetails(5L, 10L));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void adminUpdateChangesProvidedFieldsOnly() {
        Route route = route();
        when(routeRepository.findByIdAndActiveTrue(5L)).thenReturn(Optional.of(route));
        when(routeRepository.save(route)).thenReturn(route);

        AdminUpdateRouteDTO dto = new AdminUpdateRouteDTO(
                "New Name", null, null, "Reykjavik",
                null, null, null, null);
        Route updated = service.updateRouteAsAdmin(5L, dto);

        assertEquals("New Name", updated.getName());
        assertEquals("Reykjavik", updated.getLocation());
        assertEquals("V3", updated.getDifficultyGrade());
        assertEquals(GradingSystem.V_SCALE, updated.getGradingSystem());
        verify(routeRepository).save(route);
    }

    @Test
    void adminUpdateMissingRouteReturns404AndDoesNotSave() {
        when(routeRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateRouteAsAdmin(99L,
                        new AdminUpdateRouteDTO("New", null, null, null,
                                null, null, null, null)));

        assertEquals("Route not found", exception.getMessage());
        verify(routeRepository, never()).save(any(Route.class));
    }


    private static User user(Long id, String username) {
        User user = new User();
        user.setUsername(username);
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static Route route() {
        Route route = new Route();
        route.setName("Blue Wall");
        route.setDifficultyGrade("V3");
        route.setGradingSystem(GradingSystem.V_SCALE);
        route.setClimbingStyle(ClimbingStyle.BOULDERING);
        route.setLocation("Gym");
        route.setActive(true);
        return route;
    }

    // UC18 service tests
    @Test
    void deleteRouteAsAdminSoftDeletesRoute() {
        Route route = route();

        when(routeRepository.findByIdAndActiveTrue(5L))
                .thenReturn(Optional.of(route));

        service.deleteRouteAsAdmin(5L);

        assertFalse(route.isActive());
        verify(routeRepository).save(route);
    }
    @Test
    void deleteMissingRouteReturns404() {
        when(routeRepository.findByIdAndActiveTrue(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.deleteRouteAsAdmin(99L)
        );

        assertEquals("Route not found", exception.getMessage());

        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    void deletingAlreadyInactiveRouteReturns404() {
        when(routeRepository.findByIdAndActiveTrue(5L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.deleteRouteAsAdmin(5L)
        );

        assertEquals("Route not found", exception.getMessage());

        verify(routeRepository, never()).save(any());
    }

    @Test
    void deleteRouteDoesNotDeleteClimbs() {
        Route route = route();

        when(routeRepository.findByIdAndActiveTrue(5L))
                .thenReturn(Optional.of(route));

        service.deleteRouteAsAdmin(5L);

        verifyNoInteractions(climbRepository);
    }


}
