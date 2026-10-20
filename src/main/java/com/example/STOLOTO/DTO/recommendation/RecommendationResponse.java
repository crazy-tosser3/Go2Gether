package com.example.STOLOTO.DTO.recommendation;

import java.util.List;

import com.example.STOLOTO.DTO.event.EventShortResponse;

public record RecommendationResponse(EventShortResponse event, double score, List<RecommendationReason> reasons) {
}
