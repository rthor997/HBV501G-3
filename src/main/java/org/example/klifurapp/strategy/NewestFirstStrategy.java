package org.example.klifurapp.strategy;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class NewestFirstStrategy implements ClimbSortingStrategy {

    @Override
    public String getName() {
        return "newest";
    }

    @Override
    public Sort getSort() {
        return Sort.by(
                Sort.Order.desc("date"),
                Sort.Order.desc("id")
        );
    }
}
