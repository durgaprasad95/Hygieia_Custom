package com.capitalone.dashboard.collector;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import com.capitalone.dashboard.datafactory.rally.RallyDataFactory;
import com.capitalone.dashboard.model.Feature;
import com.capitalone.dashboard.repository.FeatureCollectorRepository;
import com.capitalone.dashboard.repository.FeatureRepository;

public class StoryDataClient {
	private final RallyDataFactory rallyData;
	private final FeatureRepository featureRepository;
	private final FeatureCollectorRepository featureCollectorRepository;

	@Autowired
	public StoryDataClient(RallyDataFactory rallyData, FeatureCollectorRepository featureCollectorRepository,
			FeatureRepository featureRepository) {
		this.rallyData = rallyData;
		this.featureRepository = featureRepository;
		this.featureCollectorRepository = featureCollectorRepository;
	}

	public void updateMongoinfo() throws IOException, URISyntaxException, DuplicateKeyException {
		List<Feature> stories = new ArrayList<>();

		stories.addAll(rallyData.getStories("scrum", "story"));
		stories.addAll(rallyData.getStories("scrum", "defect"));
		// stories.addAll(rallyData.getStories("kanban","story"));
		deleteAllStories();
		for (Feature story : stories) {
			story.setCollectorId(featureCollectorRepository.findByName("Rally").getId());
			cleanExistingStory(story);
			featureRepository.save(story);
		}
	}

	public void cleanExistingStory(Feature story) {
		featureRepository.delete(featureRepository.getFeatureIdById(story.getsId()));
	}

	public void deleteAllStories() {
		featureRepository.deleteAll();
	}

}