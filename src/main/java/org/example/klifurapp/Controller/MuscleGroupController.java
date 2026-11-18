package org.example.klifurapp.Controller;

import org.example.klifurapp.DTO.responses.MuscleGroupResponseDTO;
import org.example.klifurapp.service.MuscleGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/muscle-groups")
public class MuscleGroupController {

    private final MuscleGroupService muscleGroupService;

    public MuscleGroupController(MuscleGroupService muscleGroupService) {
        this.muscleGroupService = muscleGroupService;
    }

    @GetMapping
    public ResponseEntity<List<MuscleGroupResponseDTO>> getMuscleGroups() {
        return ResponseEntity.ok(muscleGroupService.getAll());
    }
}
