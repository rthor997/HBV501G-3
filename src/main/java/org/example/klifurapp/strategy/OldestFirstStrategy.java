package org.example.klifurapp.strategy;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class OldestFirstStrategy implements ClimbSortingStrategy {

    @Override
    public String getName() {
        return "oldest";
    }

    @Override
    public Sort getSort() {
        return Sort.by(
                Sort.Order.asc("date"),
                Sort.Order.asc("id")
        );
    }
}
