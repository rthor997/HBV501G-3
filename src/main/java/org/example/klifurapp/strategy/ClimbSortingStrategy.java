package org.example.klifurapp.strategy;

import org.springframework.data.domain.Sort;

public interface ClimbSortingStrategy {
    String getName();
    Sort getSort();
}
