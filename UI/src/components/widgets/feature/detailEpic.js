/**
 * Detail controller for the feature widget
 */
(function() {
	'use strict';

	angular.module(HygieiaConfig.module).controller(
			'EpicWidgetDetailController', EpicWidgetDetailController);

	EpicWidgetDetailController.$inject = [ '$scope', '$uibModalInstance',
			'epic', 'number', 'description' ];
	function EpicWidgetDetailController($scope, $uibModalInstance, epic,
			number, description) {
		var ctrl = this;

		ctrl.epic = epic;
		ctrl.number = number;
		ctrl.description = description;

		ctrl.close = close;

		function close() {
			$uibModalInstance.dismiss('close');
		}
	}
})();