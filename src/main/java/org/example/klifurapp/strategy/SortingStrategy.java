package org.example.klifurapp.strategy;

import org.springframework.data.domain.Sort;

public interface SortingStrategy {
    String getName();
    Sort getSort();
}
