package com.example.STOLOTO.DTO.recommendation;

import java.util.List;

import com.example.STOLOTO.DTO.event.EventShortResponse;

/** Рекомендованное событие: карточка, итоговый балл и причины по убыванию вклада. */
public record RecommendationResponse(EventShortResponse event, double score, List<RecommendationReason> reasons) {
}
