package org.example.klifurapp.strategy;

import org.springframework.data.domain.Sort;

public class OldestUserFirstStrategy implements SortingStrategy {
    @Override
    public String getName() {
        return "oldestUser";
    }

    @Override
    public Sort getSort() {
        return Sort.by(
                Sort.Order.asc("id")
        );
    }
}
