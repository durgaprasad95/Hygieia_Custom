package com.capitalone.dashboard.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

import com.capitalone.dashboard.model.Feature;

/**
 * Repository for {@link FeatureCollector}.
 */
public interface FeatureRepository
		extends CrudRepository<Feature, ObjectId>, QueryDslPredicateExecutor<Feature>, FeatureRepositoryCustom {
	/**
	 * This essentially returns the max change date from the collection, based on
	 * the last change date (or default delta change date property) available
	 * 
	 * @param collectorId
	 *            Collector ID of source system collector
	 * @param changeDate
	 *            Last available change date or delta begin date property
	 * @return A single Change Date value that is the maximum value of the existing
	 *         collection
	 */
	@Query
	List<Feature> findTopByCollectorIdAndChangeDateGreaterThanOrderByChangeDateDesc(ObjectId collectorId,
			String changeDate);

	@Query(value = "{'sId' : ?0}", fields = "{'sId' : 1}")
	List<Feature> getFeatureIdById(String sId);

	@Query(value = " {'sNumber' : ?0 }")
	List<Feature> getStoryByNumber(String sNumber);

	/**
	 * Find all features with active sprints that are between the provided date and
	 * that have an end date < 9999-12-31 EOD
	 * 
	 * @param sTeamId
	 * @param currentISODateTime
	 * @return
	 */
	@Query(value = "{'sTeamID' : ?0 , 'isDeleted' : 'False', $and : [{'sSprintID' : {$ne : null}} , {'sSprintID' : {$ne : ''}} , {'sSprintAssetState': { $regex: '^active$', $options: 'i' } } , {'sSprintBeginDate' : {$lte : ?1}} , {'sSprintEndDate' : {$gte : ?1}} , {'sSprintEndDate' : {$lt : '9999-12-31T59:59:59.999999'}} ] }, $orderby: { 'sStatus' :-1 }")
	List<Feature> findByActiveEndingSprints(String sTeamId, String currentISODateTime);

	/**
	 * Find all features that have sprints set but without an end date (or an end
	 * date >= 9999-12-31 EOD)
	 * 
	 * @param sTeamId
	 * @return
	 */
	@Query(value = "{'sTeamID' : ?0 , 'isDeleted' : 'False', $and : [{'sSprintID' : {$ne : null}} , {'sSprintID' : {$ne : \"\"}} , {'sSprintAssetState': { $regex: '^active$', $options: 'i' } } , { $or : [{'sSprintEndDate' : {$eq : null}} , {'sSprintEndDate' : {$eq : ''}} , {'sSprintEndDate' : {$gte : '9999-12-31T59:59:59.999999'}}] } ] }, $orderby: { 'sStatus' :-1 }")
	List<Feature> findByUnendingSprints(String sTeamId);

	/**
	 * Find all features without sprints set
	 * 
	 * @param sTeamId
	 * @return
	 */
	@Query(value = "{'sTeamID' : ?0 , 'isDeleted' : 'False', $or : [{'sSprintID' : {$eq : null}}, {'sSprintID' : {$eq : \"\"}}] }, $orderby: { 'sStatus' :-1 }")
	List<Feature> findByNullSprints(String sSteamId);

	/**
	 * @see #findByActiveEndingSprints(String, String)
	 */
	@Query(value = "{'sTeamID' : ?0 , 'isDeleted' : 'False', $and : [{'sSprintID' : {$ne : null}} , {'sSprintID' : {$ne : ''}} , {'sSprintAssetState': { $regex: '^active$', $options: 'i' } } , {'sSprintBeginDate' : {$lte : ?1}} , {'sSprintEndDate' : {$gte : ?1}} , {'sSprintEndDate' : {$lt : '9999-12-31T59:59:59.999999'}} ] }, $orderby: { 'sStatus' :-1 }", fields = "{'sStatus': 1, 'sNumber': 1, 'sSprintID': 1, 'sSprintName': 1, 'sSprintBeginDate': 1, 'sSprintEndDate': 1, 'sEpicID' : 1,'sEpicNumber' : 1, 'sEpicName' : 1, 'sEstimate': 1, 'sEstimateTime': 1}")
	List<Feature> findByActiveEndingSprintsMinimal(String sTeamId, String currentISODateTime);

	/**
	 * @see #findByUnendingSprints(String)
	 */
	@Query(value = "{'sTeamID' : ?0 , 'isDeleted' : 'False', $and : [{'sSprintID' : {$ne : null}} , {'sSprintID' : {$ne : \"\"}} , {'sSprintAssetState': { $regex: '^active$', $options: 'i' } } , { $or : [{'sSprintEndDate' : {$eq : null}} , {'sSprintEndDate' : {$eq : ''}} , {'sSprintEndDate' : {$gte : '9999-12-31T59:59:59.999999'}}] } ] }, $orderby: { 'sStatus' :-1 }", fields = "{'sStatus': 1, 'sNumber': 1, 'sSprintID': 1, 'sSprintName': 1, 'sSprintBeginDate': 1, 'sSprintEndDate': 1, 'sEpicID' : 1,'sEpicNumber' : 1, 'sEpicName' : 1, 'sEstimate': 1, 'sEstimateTime': 1}")
	List<Feature> findByUnendingSprintsMinimal(String sTeamId);

	/**
	 * @see #findByNullSprints(String)
	 */
	@Query(value = "{'sTeamID' : ?0 , 'isDeleted' : 'False', $or : [{'sSprintID' : {$eq : null}}, {'sSprintID' : {$eq : \"\"}}] }, $orderby: { 'sStatus' :-1 }", fields = "{'sStatus': 1, 'sNumber': 1, 'sSprintID': 1, 'sSprintName': 1, 'sSprintBeginDate': 1, 'sSprintEndDate': 1, 'sEpicID' : 1,'sEpicNumber' : 1, 'sEpicName' : 1, 'sEstimate': 1, 'sEstimateTime': 1}")
	List<Feature> findByNullSprintsMinimal(String sTeamId);
}