package org.example.klifurapp.service;

import org.example.klifurapp.DTO.responses.MuscleGroupResponseDTO;
import org.example.klifurapp.entity.MuscleGroup;
import org.example.klifurapp.exception.BadRequestException;
import org.example.klifurapp.repository.MuscleGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MuscleGroupService {

    private final MuscleGroupRepository muscleGroupRepository;

    public MuscleGroupService(MuscleGroupRepository muscleGroupRepository) {
        this.muscleGroupRepository = muscleGroupRepository;
    }

    @Transactional(readOnly = true)
    public List<MuscleGroupResponseDTO> getAll() {
        return muscleGroupRepository.findAllByOrderByIdAsc()
                .stream()
                .map(group -> new MuscleGroupResponseDTO(group.getId(), group.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<MuscleGroup> findAllByIds(Set<Long> ids) {
        Set<MuscleGroup> groups = new HashSet<>(muscleGroupRepository.findAllById(ids));

        if (groups.size() != ids.size()) {
            Set<Long> found = groups.stream().map(MuscleGroup::getId).collect(Collectors.toSet());
            Set<Long> missing = new HashSet<>(ids);
            missing.removeAll(found);
            throw new BadRequestException("Unknown muscle group id(s): " + missing);
        }

        return groups;
    }
}
