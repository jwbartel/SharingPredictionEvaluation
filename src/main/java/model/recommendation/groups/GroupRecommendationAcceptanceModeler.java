package model.recommendation.groups;

import java.util.Collection;

import metrics.MetricResult;

public interface GroupRecommendationAcceptanceModeler {

	public Collection<MetricResult> modelRecommendationAcceptance();
}
