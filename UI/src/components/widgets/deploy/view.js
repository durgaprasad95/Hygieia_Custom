(function() {
    'use strict';

    angular.module(HygieiaConfig.module).controller('deployViewController',
	    deployViewController);

    deployViewController.$inject = [ '$scope', 'DashStatus', 'deployData',
	    'DisplayState', '$q', '$uibModal' ];
    function deployViewController($scope, DashStatus, deployData, DisplayState, $q, $uibModal) {

	var ctrl = this;

	// public variables
	ctrl.deployments = [];
	
	if ($scope.widgetConfig.options.ignoreRegex !== undefined
		&& $scope.widgetConfig.options.ignoreRegex !== null
		&& $scope.widgetConfig.options.ignoreRegex !== '') {
	    ctrl.ignoreEnvironmentFailuresRegex = new RegExp(
		    $scope.widgetConfig.options.ignoreRegex.replace(/^"(.*)"$/,
			    '$1'));
	}

	// public methods
	ctrl.load = load;

	// getting data from rest call to JS and mapping data format according
	// to our needs
	function load() {
	    deployData.details($scope.widgetConfig.componentId).then(
		    function(data) {
			data = _.map(_.sortBy(_.filter(data, function(x) {
			    return (new Date() - x.deployDate) <= 604800000;
			}), function(x) {
			    return new Date() - x.deployDate;
			}), function(x) {
			    return {
				farm : x.farm,
				deployDate : x.deployDate,
				appVersion : x.appVersion,
				stages : [ {
				    0 : x.blJenkins
				}, {
				    1 : x.blDeploy
				}, {
				    2 : x.blSmokeTest
				}, {
				    3 : x.blValidation
				} ]
			    };
			});
			processResponse(data);
		    });
	}

	// dynamically allocating classes to the elements
	ctrl.dynamicClass = function(val) {
	    if (val.charAt(0) == 'U') {
		return 'warning';
	    } else if (val.charAt(0) == 'F') {
		return 'failed';
	    } else if (val.charAt(0) == 'S') {
		return 'success';
	    } else if (val.charAt(0) == 'N') {
		return 'not-triggered';
	    }
	}

	// getting titles allocated to the elements
	ctrl.tooltipFunc = function(param) {
	    if (param == 0) {
		return "Jenkins";
	    }
	    if (param == 1) {
		return "Deployment";
	    }
	    if (param == 2) {
		return "SmokeTest";
	    }
	    if (param == 3) {
		return "Validation";
	    }
	}

	// this assigns the data from ajax call to variable so we can display in
	// UI
	function processResponse(data) {
	    ctrl.deployments = [];
	    _.each(data, function(x) {
		var stagesData = x.stages;
		for (var i = 0; i < 4; i++) {
		    if (stagesData[i][i] == "FAILED") {
			for (var j = 0; j < i; j++) {
			    stagesData[j][j] = "SUCCESS";
			}
			for (var j = i + 1; j < 4; j++) {
			    stagesData[j][j] = "NOT_TRIGGERED";
			}
			break;
		    }
		    if (i == 3) {
			for (var j = 0; j < 4; j++) {
			    stagesData[j][j] = "SUCCESS";
			}
		    }
		}
	    });
	    ctrl.deployments = data;
	    console.log(ctrl.deployments);
	}
    }
})();