package org.example.klifurapp.strategy;

import org.springframework.data.domain.Sort;

public class ReverseAlphabeticalUserStrategy implements SortingStrategy {
    @Override
    public String getName() {
        return "reverseAlphabetical";
    }

    @Override
    public Sort getSort() {
        return Sort.by(
                Sort.Order.desc("username"),
                Sort.Order.desc("id")
        );
    }
}
