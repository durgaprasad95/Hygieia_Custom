package com.capitalone.dashboard.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.capitalone.dashboard.model.Collector;
import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.model.Component;
import com.capitalone.dashboard.model.DataResponse;
import com.capitalone.dashboard.model.Feature;
import com.capitalone.dashboard.model.QFeature;
import com.capitalone.dashboard.model.QScopeOwner;
import com.capitalone.dashboard.model.SprintEstimate;
import com.capitalone.dashboard.repository.CollectorRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.FeatureRepository;
import com.capitalone.dashboard.util.FeatureCollectorConstants;
import com.mysema.query.BooleanBuilder;

/**
 * The feature service.
 * <p>
 * Features can currently belong to 2 sprint types: scrum and kanban. In order
 * to be considered part of the sprint the feature must not be deleted and must
 * have an "active" sprint asset state if the sprint is set. The following logic
 * also applies:
 * <p>
 * A feature is part of a scrum sprint if any of the following are true:
 * <ol>
 * <li>the feature has a sprint set that has start <= now <= end and end < EOT
 * (9999-12-31T59:59:59.999999)</li>
 * </ol>
 * <p>
 * A feature is part of a kanban sprint if any of the following are true:
 * <ol>
 * <li>the feature does not have a sprint set</li>
 * <li>the feature has a sprint set that does not have an end date</li>
 * <li>the feature has a sprint set that has an end date >= EOT
 * (9999-12-31T59:59:59.999999)</li>
 * </ol>
 */
@Service
public class FeatureServiceImpl implements FeatureService {
	private static final Log LOG = LogFactory.getLog(FeatureServiceImpl.class);

	private static String TEAM_ID;

	private final ComponentRepository componentRepository;
	private final FeatureRepository featureRepository;
	private final CollectorRepository collectorRepository;

	// For filtering the data by its value
	@Value("${release}")
	private String release;

	// Start and End Dates -- To know if a Feature is of this Sprint or some Other
	private static Map<String, Date> dates;

	/**
	 * Default autowired constructor for repositories
	 *
	 * @param componentRepository
	 *            Repository containing components used by the UI (populated by UI)
	 * @param collectorRepository
	 *            Repository containing all registered collectors
	 * @param featureRepository
	 *            Repository containing all features
	 */
	@SuppressWarnings("deprecation")
	@Autowired
	public FeatureServiceImpl(ComponentRepository componentRepository, CollectorRepository collectorRepository,
			FeatureRepository featureRepository) {
		this.componentRepository = componentRepository;
		this.featureRepository = featureRepository;
		this.collectorRepository = collectorRepository;
		dates = new HashMap<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			dates.put("S 0 S", sdf.parse("2017-11-06"));
			dates.put("S 0 E", sdf.parse("2017-11-14"));
			dates.put("S 1 S", sdf.parse("2017-11-15"));
			dates.put("S 1 E", sdf.parse("2017-12-05"));
			dates.put("S 2 S", sdf.parse("2017-12-06"));
			dates.put("S 2 E", sdf.parse("2017-12-26"));
			dates.put("S 3 S", sdf.parse("2017-12-27"));
			dates.put("S 3 E", sdf.parse("2018-01-19"));
			dates.put("FIT S", sdf.parse("2018-01-20"));
			dates.put("FIT E", sdf.parse("2018-01-22"));
			dates.put("IAT S", sdf.parse("2018-01-23"));
			dates.put("IAT E", sdf.parse("2018-02-09"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves a single story based on a back-end story number
	 *
	 * @param componentId
	 *            The ID of the related UI component that will reference collector
	 *            item content from this collector
	 * @param storyNumber
	 *            A back-end story ID used by a source system
	 * @return A data response list of type Feature containing a single story
	 */
	@Override
	public DataResponse<List<Feature>> getStory(ObjectId componentId, String storyNumber) {
		Component component = componentRepository.findOne(componentId);
		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils.isEmpty(component.getCollectorItems().get(CollectorType.AgileTool))
				|| (component.getCollectorItems().get(CollectorType.AgileTool).get(0) == null)) {
			return getEmptyLegacyDataResponse();
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.AgileTool).get(0);

		QScopeOwner team = new QScopeOwner("team");
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(team.collectorItemId.eq(item.getId()));

		// Get one story based on story number, based on component
		List<Feature> story = featureRepository.getStoryByNumber(storyNumber);
		Collector collector = collectorRepository.findOne(item.getCollectorId());
		return new DataResponse<>(story, collector.getLastExecuted());
	}

	/**
	 * Retrieves estimate total of all features in the current sprint and for the
	 * current team.
	 *
	 * @param componentId
	 *            The ID of the related UI component that will reference collector
	 *            item content from this collector
	 * @param teamId
	 *            A given scope-owner's source-system ID
	 * @return A data response list of type Feature containing the total estimate
	 *         number for all features
	 */
	@Override
	@Deprecated
	public DataResponse<List<Feature>> getTotalEstimate(ObjectId componentId, String teamId, Optional<String> agileType,
			Optional<String> estimateMetricType) {
		Component component = componentRepository.findOne(componentId);

		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils.isEmpty(component.getCollectorItems().get(CollectorType.AgileTool))
				|| (component.getCollectorItems().get(CollectorType.AgileTool).get(0) == null)) {
			return getEmptyLegacyDataResponse();
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.AgileTool).get(0);

		SprintEstimate estimate = getSprintEstimates(teamId, FeatureCollectorConstants.PROJECT_ID_ANY,
				item.getCollectorId(), agileType, estimateMetricType);

		List<Feature> list = Collections.singletonList(new Feature());
		list.get(0).setsEstimate(Integer.toString(estimate.getTotalEstimate()));

		Collector collector = collectorRepository.findOne(item.getCollectorId());
		return new DataResponse<>(list, collector.getLastExecuted());
	}

	/**
	 * Retrieves estimate in-progress of all features in the current sprint and for
	 * the current team.
	 *
	 * @param componentId
	 *            The ID of the related UI component that will reference collector
	 *            item content from this collector
	 * @param teamId
	 *            A given scope-owner's source-system ID
	 * @return A data response list of type Feature containing the in-progress
	 *         estimate number for all features
	 */
	@Override
	@Deprecated
	public DataResponse<List<Feature>> getInProgressEstimate(ObjectId componentId, String teamId,
			Optional<String> agileType, Optional<String> estimateMetricType) {
		Component component = componentRepository.findOne(componentId);

		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils.isEmpty(component.getCollectorItems().get(CollectorType.AgileTool))
				|| (component.getCollectorItems().get(CollectorType.AgileTool).get(0) == null)) {
			return getEmptyLegacyDataResponse();
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.AgileTool).get(0);

		SprintEstimate estimate = getSprintEstimates(teamId, FeatureCollectorConstants.PROJECT_ID_ANY,
				item.getCollectorId(), agileType, estimateMetricType);

		List<Feature> list = Collections.singletonList(new Feature());
		list.get(0).setsEstimate(Integer.toString(estimate.getInProgressEstimate()));

		Collector collector = collectorRepository.findOne(item.getCollectorId());
		return new DataResponse<>(list, collector.getLastExecuted());
	}

	/**
	 * Retrieves estimate done of all features in the current sprint and for the
	 * current team.
	 *
	 * @param componentId
	 *            The ID of the related UI component that will reference collector
	 *            item content from this collector
	 * @param teamId
	 *            A given scope-owner's source-system ID
	 * @return A data response list of type Feature containing the done estimate
	 *         number for all features
	 */
	@Override
	@Deprecated
	public DataResponse<List<Feature>> getDoneEstimate(ObjectId componentId, String teamId, Optional<String> agileType,
			Optional<String> estimateMetricType) {
		Component component = componentRepository.findOne(componentId);

		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils.isEmpty(component.getCollectorItems().get(CollectorType.AgileTool))
				|| (component.getCollectorItems().get(CollectorType.AgileTool).get(0) == null)) {
			return getEmptyLegacyDataResponse();
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.AgileTool).get(0);

		SprintEstimate estimate = getSprintEstimates(teamId, FeatureCollectorConstants.PROJECT_ID_ANY,
				item.getCollectorId(), agileType, estimateMetricType);

		List<Feature> list = Collections.singletonList(new Feature());
		list.get(0).setsEstimate(Integer.toString(estimate.getCompleteEstimate()));

		Collector collector = collectorRepository.findOne(item.getCollectorId());
		return new DataResponse<>(list, collector.getLastExecuted());
	}

	@SuppressWarnings("PMD.NPathComplexity")
	private SprintEstimate getSprintEstimates(String teamId, String projectId, ObjectId collectorId,
			Optional<String> agileType, Optional<String> estimateMetricType) {
		List<Feature> storyEstimates = getFeaturesForCurrentSprints(teamId, projectId, collectorId,
				agileType.isPresent() ? agileType.get() : null, true);

		int totalEstimate = 0;
		int wipEstimate = 0;
		int doneEstimate = 0;

		for (Feature tempRs : storyEstimates) {
			String tempStatus = tempRs.getsStatus() != null ? tempRs.getsStatus().toLowerCase() : null;

			// if estimateMetricType is hours accumulate time estimate in minutes for better
			// precision ... divide by 60 later
			int estimate = getEstimate(tempRs, estimateMetricType);

			totalEstimate += estimate;
			if (tempStatus != null) {
				switch (tempStatus) {
				case "in progress":
				case "waiting":
				case "impeded":
					wipEstimate += estimate;
					break;
				case "done":
				case "accepted":
					doneEstimate += estimate;
					break;
				}
			}
		}

		int openEstimate = totalEstimate - wipEstimate - doneEstimate;

		if (isEstimateTime(estimateMetricType)) {
			// time estimate is in minutes but we want to return in hours
			totalEstimate /= 60;
			openEstimate /= 60;
			wipEstimate /= 60;
			doneEstimate /= 60;
		}

		SprintEstimate response = new SprintEstimate();
		response.setOpenEstimate(openEstimate);
		response.setInProgressEstimate(wipEstimate);
		response.setCompleteEstimate(doneEstimate);
		response.setTotalEstimate(totalEstimate);

		return response;
	}

	/**
	 * Retrieves all stories for a given team and their current sprint
	 *
	 * @param componentId
	 *            The ID of the related UI component that will reference collector
	 *            item content from this collector
	 * @param teamId
	 *            A given scope-owner's source-system ID
	 * @return A data response list of type Feature containing all features for the
	 *         given team and current sprint
	 */
	@Override
	public DataResponse<List<Feature>> getRelevantStories(ObjectId componentId, String teamId,
			Optional<String> agileType) {
		Component component = componentRepository.findOne(componentId);
		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils.isEmpty(component.getCollectorItems().get(CollectorType.AgileTool))
				|| (component.getCollectorItems().get(CollectorType.AgileTool).get(0) == null)) {
			return getEmptyLegacyDataResponse();
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.AgileTool).get(0);

		QScopeOwner team = new QScopeOwner("team");
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(team.collectorItemId.eq(item.getId()));

		// Get teamId first from available collector item, based on component
		List<Feature> relevantStories = getFeaturesForCurrentSprints(teamId,
				agileType.isPresent() ? agileType.get() : null, false);

		Collector collector = collectorRepository.findOne(item.getCollectorId());

		return new DataResponse<>(relevantStories, collector.getLastExecuted());
	}

	@Override
	public DataResponse<Map<String, List<Feature>>> getStories(ObjectId componentId) {
		Component component = componentRepository.findOne(componentId);
		QFeature feature = new QFeature("Feature");
		Map<String, List<Feature>> featuresMap = new HashMap<String, List<Feature>>();
		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils.isEmpty(component.getCollectorItems().get(CollectorType.AgileTool))
				|| (component.getCollectorItems().get(CollectorType.AgileTool).get(0) == null)) {
			return new DataResponse<>(featuresMap, 0);
		}
		BooleanBuilder builder1 = new BooleanBuilder();
		builder1.and(feature.sTeamID.eq(FeatureServiceImpl.TEAM_ID));
		List<Feature> userStories = (List<Feature>) featureRepository
				.findAll(builder1.and(feature.sTypeName.eq("HierarchicalRequirement")));
		BooleanBuilder builder2 = new BooleanBuilder();
		builder2.and(feature.sTeamID.eq(FeatureServiceImpl.TEAM_ID));
		List<Feature> defects = (List<Feature>) featureRepository.findAll(builder2.and(feature.sTypeName.eq("Defect")));
		featuresMap.put("US", userStories);
		// Setting the new prop for defects which is
		// When it is raised
		defects = setPropCreationDate(defects);
		featuresMap.put("DE", defects);
		CollectorItem item = component.getCollectorItems().get(CollectorType.AgileTool).get(0);
		Collector collector = collectorRepository.findOne(item.getCollectorId());
		return new DataResponse<>(featuresMap, collector.getLastExecuted());
	}

	/**
	 * @return Default Data response that needs to be send as response to ajax call
	 *         at the time of empty result
	 */
	private DataResponse<List<Feature>> getEmptyLegacyDataResponse() {
		Feature f = new Feature();
		List<Feature> l = new ArrayList<>();
		l.add(f);
		return new DataResponse<>(l, 0);
	}

	/**
	 * Retrieves the current system time stamp in ISO date time format. Because this
	 * is not using SimpleTimeFormat, this should be thread safe.
	 *
	 * @return A string representation of the current date time stamp in ISO format
	 *         from the current time zone
	 */
	private String getCurrentISODateTime() {
		return DatatypeConverter.printDateTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
	}

	private boolean isEstimateTime(Optional<String> estimateMetricType) {
		return estimateMetricType.isPresent()
				&& FeatureCollectorConstants.STORY_HOURS_ESTIMATE.equalsIgnoreCase(estimateMetricType.get());
	}

	private boolean isEstimateCount(Optional<String> estimateMetricType) {
		return estimateMetricType.isPresent()
				&& FeatureCollectorConstants.STORY_COUNT_ESTIMATE.equalsIgnoreCase(estimateMetricType.get());
	}

	private List<Feature> getFeaturesForCurrentSprints(String teamId, String projectId, ObjectId collectorId,
			String agileType, boolean minimal) {
		List<Feature> rt = new ArrayList<Feature>();

		String now = getCurrentISODateTime();

		if (FeatureCollectorConstants.SPRINT_KANBAN.equalsIgnoreCase(agileType)) {
			/*
			 * A feature is part of a kanban sprint if any of the following are true: - the
			 * feature does not have a sprint set - the feature has a sprint set that does
			 * not have an end date - the feature has a sprint set that has an end date >=
			 * EOT (9999-12-31T59:59:59.999999)
			 */
			rt.addAll(featureRepository.findByNullSprints(teamId, projectId, collectorId, minimal));
			rt.addAll(featureRepository.findByUnendingSprints(teamId, projectId, collectorId, minimal));
		} else {
			// default to scrum
			/*
			 * A feature is part of a scrum sprint if any of the following are true: - the
			 * feature has a sprint set that has start <= now <= end and end < EOT
			 * (9999-12-31T59:59:59.999999)
			 */
			rt.addAll(featureRepository.findByActiveEndingSprints(teamId, projectId, collectorId, now, minimal));
		}

		return rt;
	}

	private int getEstimate(Feature feature, Optional<String> estimateMetricType) {
		int rt = 0;

		if (isEstimateTime(estimateMetricType)) {
			if (feature.getsEstimateTime() != null) {
				rt = feature.getsEstimateTime().intValue();
			}
		} else if (isEstimateCount(estimateMetricType)) {
			rt = 1;
		} else {
			// default to story points since that should be the most common use case
			if (!StringUtils.isEmpty(feature.getsEstimate())) {
				try {
					rt = (int) Float.parseFloat(feature.getsEstimate());
				} catch (NumberFormatException nfe) {
					rt = 0;
					LOG.error("Could not parse estimate for '" + feature.getsName() + "', number '"
							+ feature.getsNumber() + "', have estimate: " + feature.getsEstimate(), nfe);
				}
			}
		}

		return rt;
	}

	/**
	 * Retrieves all unique super features and their total sub feature estimates for
	 * a given team and their current sprint
	 *
	 * @param componentId
	 *            The ID of the related UI component that will reference collector
	 *            item content from this collector
	 * @param teamId
	 *            A given scope-owner's source-system ID
	 * @return A data response list of type Feature containing the unique features
	 *         plus their sub features' estimates associated to the current sprint
	 *         and team
	 */
	@Override
	public DataResponse<Collection<Feature>> getSuperStories(ObjectId componentId) {
		Map<String, Feature> superStoriesMap = new HashMap<String, Feature>();
		Component component = componentRepository.findOne(componentId);
		List<Feature> superStories = new ArrayList<Feature>();
		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils.isEmpty(component.getCollectorItems().get(CollectorType.AgileTool))
				|| (component.getCollectorItems().get(CollectorType.AgileTool).get(0) == null)) {
			return new DataResponse<>((superStoriesMap.values()), 0);
		}
		QFeature feature = new QFeature("Feature");
		for (Feature tempFeature : featureRepository.findAll(feature.sTeamID.eq(FeatureServiceImpl.TEAM_ID))) {
			if ((null != tempFeature.getsEpicUrl())) {
				Feature temp = new Feature();
				temp.setsName(tempFeature.getsEpicName());
				temp.setsNumber(tempFeature.getsEpicNumber());
				superStoriesMap.put(temp.getsNumber(), temp);
			}
		}
		CollectorItem item = component.getCollectorItems().get(CollectorType.AgileTool).get(0);
		Collector collector = collectorRepository.findOne(item.getCollectorId());
		return new DataResponse<>(superStoriesMap.values(), collector.getLastExecuted());
	}

	@Override
	public DataResponse<List<Feature>> getFeatureEpicEstimates(ObjectId componentId, String teamId,
			Optional<String> agileType, Optional<String> estimateMetricType) {
		Component component = componentRepository.findOne(componentId);

		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils.isEmpty(component.getCollectorItems().get(CollectorType.AgileTool))
				|| (component.getCollectorItems().get(CollectorType.AgileTool).get(0) == null)) {
			return getEmptyLegacyDataResponse();
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.AgileTool).get(0);

		List<Feature> relevantFeatureEstimates = featureRepository.findByActiveEndingSprints(teamId,
				getCurrentISODateTime());
		Map<String, Feature> epicIDToEpicFeatureMap = new HashMap<>();
		if (agileType.get().equals("scrum")) {

		} else {
			// epicID : epic information (in the form of a Feature object)

			for (Feature tempRs : relevantFeatureEstimates) {
				String sId = tempRs.getsId();

				if (StringUtils.isEmpty(sId))
					continue;
				/*
				 * if (!tempRs.getsStatus().equals("In-Progress")) { continue; }
				 */
				Feature feature = epicIDToEpicFeatureMap.get(sId);
				if (feature == null) {
					feature = new Feature();
					feature.setId(null);
					feature.setsEpicID(sId);
					feature.setsEpicNumber(tempRs.getsNumber());
					feature.setsEpicName(tempRs.getsName());
					feature.setsEstimate("0");
					epicIDToEpicFeatureMap.put(sId, feature);
				}

				// if estimateMetricType is hours accumulate time estimate in minutes for better
				// precision ... divide by 60 later
				int estimate = getEstimate(tempRs, estimateMetricType);

				feature.setsEstimate(String.valueOf(Integer.valueOf(feature.getsEstimate()) + estimate));
			}

			if (isEstimateTime(estimateMetricType)) {
				// time estimate is in minutes but we want to return in hours
				for (Feature f : epicIDToEpicFeatureMap.values()) {
					f.setsEstimate(String.valueOf(Integer.valueOf(f.getsEstimate()) / 60));
				}
			}
		}
		Collector collector = collectorRepository.findOne(item.getCollectorId());

		return new DataResponse<>(new ArrayList<>(epicIDToEpicFeatureMap.values()), collector.getLastExecuted());
	}

	@Override
	public DataResponse<SprintEstimate> getAggregatedSprintEstimates(ObjectId componentId, String teamId,
			Optional<String> agileType, Optional<String> estimateMetricType) {
		Component component = componentRepository.findOne(componentId);
		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils.isEmpty(component.getCollectorItems().get(CollectorType.AgileTool))
				|| (component.getCollectorItems().get(CollectorType.AgileTool).get(0) == null)) {
			return new DataResponse<SprintEstimate>(new SprintEstimate(), 0);
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.AgileTool).get(0);
		Collector collector = collectorRepository.findOne(item.getCollectorId());

		SprintEstimate estimate = getSprintEstimates(teamId, agileType, estimateMetricType);
		return new DataResponse<>(estimate, collector.getLastExecuted());
	}

	/**
	 * Retrieves the current sprint's detail for a given team.
	 *
	 * @param componentId
	 *            The ID of the related UI component that will reference collector
	 *            item content from this collector
	 * @param teamId
	 *            A given scope-owner's source-system ID
	 * @return A data response list of type Feature containing several relevant
	 *         sprint fields for the current team's sprint
	 */
	@Override
	public DataResponse<List<Feature>> getCurrentSprintDetail(ObjectId componentId, String teamId,
			Optional<String> agileType) {
		Component component = componentRepository.findOne(componentId);
		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils.isEmpty(component.getCollectorItems().get(CollectorType.ScopeOwner))
				|| (component.getCollectorItems().get(CollectorType.ScopeOwner).get(0) == null)) {
			return getEmptyLegacyDataResponse();
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.ScopeOwner).get(0);

		// Get teamId first from available collector item, based on component
		List<Feature> sprintResponse = getFeaturesForCurrentSprints(teamId,
				agileType.isPresent() ? agileType.get() : null, true);

		Collector collector = collectorRepository.findOne(item.getCollectorId());
		return new DataResponse<>(sprintResponse, collector.getLastExecuted());
	}

	private SprintEstimate getSprintEstimates(String teamId, Optional<String> agileType,
			Optional<String> estimateMetricType) {
		List<Feature> storyEstimates = getFeaturesForCurrentSprints(teamId,
				agileType.isPresent() ? agileType.get() : null, true);

		int totalEstimate = 0;
		int wipEstimate = 0;
		int doneEstimate = 0;

		for (Feature tempRs : storyEstimates) {
			String tempStatus = tempRs.getsStatus() != null ? tempRs.getsStatus().toLowerCase() : null;

			// if estimateMetricType is hours accumulate time estimate in minutes for better
			// precision ... divide by 60 later
			int estimate = getEstimate(tempRs, estimateMetricType);

			totalEstimate += estimate;
			if (tempStatus != null) {
				switch (tempStatus) {
				case "in-progress":
				case "defined":
				case "blocked":
					wipEstimate += estimate;
					break;
				case "completed":
				case "accepted":
					doneEstimate += estimate;
					break;
				}
			}
		}

		int openEstimate = totalEstimate - wipEstimate - doneEstimate;

		if (isEstimateTime(estimateMetricType)) {
			// time estimate is in minutes but we want to return in hours
			totalEstimate /= 60;
			openEstimate /= 60;
			wipEstimate /= 60;
			doneEstimate /= 60;
		}

		SprintEstimate response = new SprintEstimate();
		response.setOpenEstimate(openEstimate);
		response.setInProgressEstimate(wipEstimate);
		response.setCompleteEstimate(doneEstimate);
		response.setTotalEstimate(totalEstimate);

		return response;
	}

	/**
	 * Get the features that belong to the current sprints
	 * 
	 * @param teamId
	 *            the team id
	 * @param agileType
	 *            the agile type. Defaults to "scrum" if null
	 * @param minimal
	 *            if the resulting list of Features should be minimally populated
	 *            (see queries for fields)
	 * @return
	 */
	private List<Feature> getFeaturesForCurrentSprints(String teamId, String agileType, boolean minimal) {
		List<Feature> rt = new ArrayList<Feature>();

		String now = getCurrentISODateTime();

		if (FeatureCollectorConstants.SPRINT_KANBAN.equalsIgnoreCase(agileType)) {
			/*
			 * A feature is part of a kanban sprint if any of the following are true: - the
			 * feature does not have a sprint set - the feature has a sprint set that does
			 * not have an end date - the feature has a sprint set that has an end date >=
			 * EOT (9999-12-31T59:59:59.999999)
			 */
			if (minimal) {
				rt.addAll(featureRepository.findByNullSprintsMinimal(teamId));
				rt.addAll(featureRepository.findByUnendingSprintsMinimal(teamId));
			} else {
				rt.addAll(featureRepository.findByNullSprints(teamId));
				rt.addAll(featureRepository.findByUnendingSprints(teamId));
			}
		} else {
			// default to scrum
			/*
			 * A feature is part of a scrum sprint if any of the following are true: - the
			 * feature has a sprint set that has start <= now <= end and end < EOT
			 * (9999-12-31T59:59:59.999999)
			 */
			if (minimal) {
				rt.addAll(featureRepository.findByActiveEndingSprintsMinimal(teamId, now));
			} else {
				rt.addAll(featureRepository.findByActiveEndingSprints(teamId, now));
			}
		}

		return rt;
	}

	/**
	 * Calculates count of particular Features types for a team
	 * 
	 * @param componentId
	 *            The ID of the related UI component that will reference collector
	 *            item content from this collector
	 * @param agiletype
	 *            A given String assigned from UI
	 * 
	 * @return A data response list of type Feature containing feature Type and its
	 *         Count
	 */
	@Override
	public DataResponse<Map<String, Long>> getCount(ObjectId componentId, String agileType) {
		Component component = componentRepository.findOne(componentId);
		Map<String, Long> countMap = new HashMap<>();
		if ((component == null) || CollectionUtils.isEmpty(component.getCollectorItems())
				|| CollectionUtils.isEmpty(component.getCollectorItems().get(CollectorType.AgileTool))
				|| (component.getCollectorItems().get(CollectorType.AgileTool).get(0) == null)) {
			return new DataResponse<>(countMap, 0);
		}

		CollectorItem item = component.getCollectorItems().get(CollectorType.AgileTool).get(0);
		if (FeatureCollectorConstants.SPRINT_SCRUM.equals(agileType)) {
			QFeature feature = new QFeature("Feature");
			BooleanBuilder builder1 = new BooleanBuilder();
			builder1.and(feature.sTeamID.eq(FeatureServiceImpl.TEAM_ID));
			BooleanBuilder builder2 = new BooleanBuilder();
			builder2.and(feature.sTeamID.eq(FeatureServiceImpl.TEAM_ID));
			countMap.put("userStories", featureRepository
					.count(builder1.and(feature.sTypeName.equalsIgnoreCase("HierarchicalRequirement"))));
			countMap.put("defects",
					featureRepository.count(builder2.and(feature.sTypeName.equalsIgnoreCase("Defect"))));
		}
		Collector collector = collectorRepository.findOne(item.getCollectorId());
		return new DataResponse<>(countMap, collector.getLastExecuted());

	}

	/**
	 * Assigns Release by which we filter out Features for a team
	 * 
	 * @param componentId
	 *            The ID of the related UI component that will reference collector
	 *            item content from this collector
	 * @param teamId
	 *            A given scope-owner's source-system ID
	 * 
	 * @return A data response String which represents release version
	 */
	@Override
	public DataResponse<String> getRelease(ObjectId componentId, String teamId) {
		Component component = componentRepository.findOne(componentId);
		CollectorItem item = component.getCollectorItems().get(CollectorType.AgileTool).get(0);
		Collector collector = collectorRepository.findOne(item.getCollectorId());
		FeatureServiceImpl.TEAM_ID = teamId;
		return new DataResponse<>(release, collector.getLastExecuted());
	}

	/**
	 * Filtering all Features (Only defects) for a team to determine the one that
	 * are Defect Leakages
	 * 
	 * @param componentId
	 *            The ID of the related UI component that will reference collector
	 *            item content from this collector
	 * @param defets
	 *            A given list of defects
	 * 
	 * @return A data response List which are defect leakages
	 */
	@SuppressWarnings("deprecation")
	private List<Feature> setPropCreationDate(List<Feature> defects) {

		for (Feature defect : defects) {

			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Long defectDate = sdf.parse(defect.getChangeDate()).getTime();
				if (defectDate.compareTo(FeatureServiceImpl.dates.get("S 0 E").getTime()) <= 0) {
					defect.setChangeDate("Sprint 0");
				} else if (defectDate.compareTo(FeatureServiceImpl.dates.get("S 1 E").getTime()) <= 0) {
					defect.setChangeDate("Sprint 1");
				} else if (defectDate.compareTo(FeatureServiceImpl.dates.get("S 2 E").getTime()) <= 0) {
					defect.setChangeDate("Sprint 2");
				} else if (defectDate.compareTo(FeatureServiceImpl.dates.get("S 3 E").getTime()) <= 0) {
					defect.setChangeDate("Sprint 3");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return defects;
	}

}