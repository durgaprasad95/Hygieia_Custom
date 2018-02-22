/**
 * Gets deploy related data
 */
(function() {
	'use strict';

	angular.module(HygieiaConfig.module + '.core').factory('deployData',
			deployData);

	function deployData($http) {
		var testDetailRoute = 'test-data/deploy_detail.json';
		var deployDetailRoute = '/api/deploy/status/';
		var bladeLogicDeploys = '/api/deploy/bladelogic/collect';
		var connectionData = "";

		return {
			connectionData : connectionData,
			details : details
		};

		function details(componentId) {
			return $http.get(bladeLogicDeploys).then(function(response) {
				return response.data;
			});
		}
	}
})();