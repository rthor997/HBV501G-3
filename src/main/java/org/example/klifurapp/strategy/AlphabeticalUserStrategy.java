package org.example.klifurapp.strategy;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class AlphabeticalUserStrategy implements SortingStrategy {

    @Override
    public String getName() {
        return "alphabetical";
    }

    @Override
    public Sort getSort() {
        return Sort.by(
                Sort.Order.asc("username"),
                Sort.Order.asc("id")
        );
    }
}