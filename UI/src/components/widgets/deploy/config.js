(function() {
	'use strict';

	angular.module(HygieiaConfig.module).controller('deployConfigController',
			deployConfigController);

	deployConfigController.$inject = [ 'modalData', 'collectorData',
			'$uibModalInstance', '$scope', 'deployData' ];

	function deployConfigController(modalData, collectorData,
			$uibModalInstance, $scope, deployData) {

		/* jshint validthis:true */
		var ctrl = this;

		var widgetConfig = modalData.widgetConfig;

		// public variables
		ctrl.submitted = false;
		ctrl.submit = submit;

		function submit(valid) {
			ctrl.submitted = true;
			console.log()
			if (valid) {
				var form = document.configForm;
				var postObj = {
					name : 'deploy',
					options : {
						id : widgetConfig.options.id,
					},
					componentId : modalData.dashboard.application.components[0].id
				};

				$uibModalInstance.close(postObj);
			}
			deployData.connectionData = ctrl.connectionDetails;
			console.log(deployData.connectionData);

		}
	}
})();