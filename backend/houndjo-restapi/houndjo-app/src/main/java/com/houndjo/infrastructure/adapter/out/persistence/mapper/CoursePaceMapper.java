package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.enumerations.PaceUnit;
import com.houndjo.domain.models.pace.CoursePace;
import com.houndjo.domain.models.pace.PaceFlow;
import com.houndjo.infrastructure.adapter.out.persistence.entity.CoursePaceEntity;
import java.math.BigDecimal;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link CoursePaceEntity}'s flat, per-flow columns and the nested {@link PaceFlow}
 * value objects. Kept as a plain mapper (not MapStruct) since sabak/sabqi/dhor are each
 * flattened into two columns.
 */
@Component
public class CoursePaceMapper {

    public CoursePace toDomain(CoursePaceEntity entity) {
        if (entity == null) {
            return null;
        }
        return CoursePace.rehydrate(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getCourseId(),
                entity.getUnit(),
                entity.getAmountPerSession(),
                entity.getSessionsPerWeek(),
                toFlow(entity.getSabakUnit(), entity.getSabakAmount()),
                toFlow(entity.getSabqiUnit(), entity.getSabqiAmount()),
                toFlow(entity.getDhorUnit(), entity.getDhorAmount()),
                entity.getDhorCycleDays(),
                entity.getCreationDate(),
                entity.getLastUpdateDate(),
                entity.getLastUpdatedBy());
    }

    public CoursePaceEntity toEntity(CoursePace domain) {
        CoursePaceEntity entity = new CoursePaceEntity(
                domain.getId(),
                domain.getOrganizationId(),
                domain.getCourseId(),
                domain.getUnit(),
                domain.getAmountPerSession(),
                domain.getSessionsPerWeek(),
                null,
                null,
                null,
                null,
                null,
                null,
                domain.getDhorCycleDays());
        applyFlow(domain.getSabak(), entity::setSabakUnit, entity::setSabakAmount);
        applyFlow(domain.getSabqi(), entity::setSabqiUnit, entity::setSabqiAmount);
        applyFlow(domain.getDhor(), entity::setDhorUnit, entity::setDhorAmount);
        return entity;
    }

    private PaceFlow toFlow(PaceUnit unit, BigDecimal amount) {
        return unit == null || amount == null ? null : new PaceFlow(unit, amount);
    }

    private void applyFlow(PaceFlow flow, Consumer<PaceUnit> unitSetter, Consumer<BigDecimal> amountSetter) {
        if (flow != null) {
            unitSetter.accept(flow.unit());
            amountSetter.accept(flow.amount());
        }
    }
}
