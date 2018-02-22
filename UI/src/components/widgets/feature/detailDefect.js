/**
 * Detail controller for the feature widget
 */
(function() {
	'use strict';

	angular.module(HygieiaConfig.module).controller(
			'DefectWidgetDetailController', DefectWidgetDetailController);

	DefectWidgetDetailController.$inject = [ '$scope', '$uibModalInstance',
			'defects' ];
	function DefectWidgetDetailController($scope, $uibModalInstance, defects) {
		var ctrl = this;

		ctrl.defects = defects;

		ctrl.close = close;

		function close() {
			$uibModalInstance.dismiss('close');
		}
	}
})();