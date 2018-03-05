/**
 * Gets feature related data
 */
(function() {
    'use strict';

    angular.module(HygieiaConfig.module + '.core').factory('featureData',
	    featureData);

    function featureData($http) {

	var param = '?component=';
	var agileType = {
	    kanban : "&agileType=kanban",
	    scrum : "&agileType=scrum",
	};
	var estimateMetricTypeParam = "&estimateMetricType=";
	var agileTypeParam = "&agileType=";
	var listTypeParam = "&listType=";

	var testAggregateSprintEstimates = 'test-data/feature-aggregate-sprint-estimates.json';
	var buildAggregateSprintEstimates = '/api/feature/estimates/aggregatedsprints';

	var testFeatureWip = 'test-data/feature-super.json';
	var buildFeatureWip = '/api/feature/estimates/super';

	var testSprint = 'test-data/feature-iteration.json';
	var buildSprint = '/api/iteration';

	var testProjectsRoute = 'test-data/projects.json';
	var buildProjectsRoute = '/api/scope';

	var testProjectsByCollectorId = 'test-data/teams.json';
	var buildProjectsByCollectorId = '/api/scopecollector/';
	var buildProjectsByCollectorIdPage = '/api/scopecollector/page/';

	var testTeamsRoute = 'test-data/teams.json';
	var buildTeamsRoute = '/api/team';

	var testTeamsByCollectorId = 'test-data/teams.json';
	var buildTeamsByCollectorId = '/api/teamcollector/';
	var buildTeamsByCollectorIdPage = '/api/teamcollector/page/';

	var testTeams = 'test-data/collector_type-scopeowner.json';
	var buildTeams = '/api/collector/item/type/ScopeOwner';

	var testTeamByCollectorItemId = 'test-data/collector_item-scopeowner.json';
	var buildTeamByCollectorItemId = '/api/collector/item/';

	var countStories = '/api/feature/count';
	var releaseNumber = '/api/release';
	var superFeaturesRoute = '/api/feature/parent';
	var featuresRoute = '/api/features';

	return {
	    sprintMetrics : aggregateSprintEstimates,
	    featureWip : featureWip,
	    sprint : sprint,
	    count : count,
	    releaseNumber : release,
	    superFeatures : superFeatures,
	    features : features,
	    teams : teams,
	    teamsByCollectorId : teamsByCollectorId,
	    projects : projects,
	    projectsByCollectorId : projectsByCollectorId,
	    projectsByCollectorIdPaginated : projectsByCollectorIdPaginated,
	    teamsByCollectorIdPaginated : teamsByCollectorIdPaginated
	};

	/**
	 * Retrieves current team's sprint detail
	 * 
	 * @param componentId
	 * @param filterTeamId
	 */

	function aggregateSprintEstimates(componentId, filterTeamId, estimateMetricType, agileType) {
	    return $http
		    .get(
			    HygieiaConfig.local ? testAggregateSprintEstimates
				    : buildAggregateSprintEstimates
					    + "/"
					    + filterTeamId
					    + param
					    + componentId
					    + (estimateMetricType != null ? estimateMetricTypeParam
						    + estimateMetricType
						    : "")
					    + (agileType != null ? agileTypeParam
						    + agileType
						    : "")).then(
			    function(response) {
				return response.data;
			    });
	}

	/**
	 * Retrieves current super features and their total in progress
	 * estimates for a given sprint and team
	 * 
	 * @param componentId
	 * @param filterTeamId
	 */
	function featureWip(componentId, filterTeamId, estimateMetricType, agileType) {
	    return $http
		    .get(
			    HygieiaConfig.local ? testFeatureWip
				    : buildFeatureWip
					    + "/"
					    + filterTeamId
					    + param
					    + componentId
					    + (estimateMetricType != null ? estimateMetricTypeParam
						    + estimateMetricType
						    : "")
					    + (agileType != null ? agileTypeParam
						    + agileType
						    : "")).then(
			    function(response) {
				return response.data;
			    });
	}

	/**
	 * Retrieves UserStories (inclusive Defects) by component ID
	 * 
	 * @param componentId
	 */
	function features(componentId) {
	    return $http.get(featuresRoute + param + componentId).then(
		    function(response) {
			return response.data;
		    });
	}

	/**
	 * Retrieves UserStories which have child US (inclusive Defects) by
	 * component ID
	 * 
	 * @param componentId
	 */
	function superFeatures(componentId) {
	    return $http.get(superFeaturesRoute + param + componentId).then(
		    function(response) {
			return response.data;
		    });
	}

	/**
	 * Calculates count of US or Defects or DefectLeakages by component ID
	 * and agileType (US || Defects || DefectLeakage)
	 * 
	 * @param componentId
	 * @param agileType
	 */
	function count(componentId, agileType) {
	    return $http.get(
		    countStories
			    + param
			    + componentId
			    + (agileType != null ? agileTypeParam + agileType
				    : "")).then(function(response) {
		return response.data;
	    }

	    );
	}

	/**
	 * Assigns release's value mentioned in backend to display in UI by
	 * component ID and team ID
	 * 
	 * @param componentId
	 * @param teamId
	 */
	function release(componentId, teamId) {
	    return $http.get(
		    releaseNumber + param + componentId + "&teamNumber="
			    + teamId).then(function(response) {
		return response.data;
	    })
	}

	/**
	 * Retrieves current team's sprint detail
	 * 
	 * @param componentId
	 * @param filterTeamId
	 */
	function sprint(componentId, filterTeamId, agileType) {
	    return $http.get(
		    HygieiaConfig.local ? testSprint : buildSprint
			    + "/"
			    + filterTeamId
			    + param
			    + componentId
			    + (agileType != null ? agileTypeParam + agileType
				    : "")).then(function(response) {
		return response.data;
	    });
	}

	/**
	 * Retrieves projects by collector ID
	 * 
	 * @param collectorId
	 */
	function projectsByCollectorId(collectorId) {
	    return $http.get(
		    HygieiaConfig.local ? testProjectsByCollectorId
			    : buildProjectsByCollectorId + collectorId).then(
		    function(response) {
			return response.data;
		    });
	}

	/**
	 * Retrieves projects by collector ID
	 * 
	 * @param collectorId
	 */
	function projectsByCollectorIdPaginated(collectorId, params) {
	    return $http.get(
		    HygieiaConfig.local ? testProjectsByCollectorId
			    : buildProjectsByCollectorIdPage + collectorId, {
			params : params
		    }).then(function(response) {
		return response.data;
	    });
	}

	/**
	 * Retrieves teams by collector ID
	 * 
	 * @param collectorId
	 */
	function teamsByCollectorId(collectorId) {
	    return $http.get(
		    HygieiaConfig.local ? testTeamsByCollectorId
			    : buildTeamsByCollectorId + collectorId).then(
		    function(response) {
			return response.data;
		    });
	}

	/**
	 * Retrieves teams by collector ID
	 * 
	 * @param collectorId
	 */
	function teamsByCollectorIdPaginated(collectorId, params) {
	    return $http.get(
		    HygieiaConfig.local ? testTeamsByCollectorId
			    : buildTeamsByCollectorIdPage + collectorId, {
			params : params
		    }).then(function(response) {
		return response.data;
	    });
	}

	/**
	 * Retrieves all projects
	 */
	function projects() {
	    return $http.get(
		    HygieiaConfig.local ? testProjectsRoute
			    : (buildProjectsRoute)).then(function(response) {
		return response.data;
	    });
	}

	/**
	 * Retrieves all teams For Rally-Feature Collector, Both Projects and
	 * Teams are same
	 */
	function teams() {
	    return $http.get(
		    HygieiaConfig.local ? testTeamsRoute : (buildTeamsRoute))
		    .then(function(response) {
			return response.data;
		    });
	}
    }
})();