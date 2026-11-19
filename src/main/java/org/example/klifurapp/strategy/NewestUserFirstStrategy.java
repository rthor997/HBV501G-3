package org.example.klifurapp.strategy;

import org.springframework.data.domain.Sort;

public class NewestUserFirstStrategy implements SortingStrategy{
    @Override
    public String getName() {
        return "newestUser";
    }

    @Override
    public Sort getSort() {
        return Sort.by(
                Sort.Order.desc("id")
        );
    }
}
