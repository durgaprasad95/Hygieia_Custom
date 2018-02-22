/**
 * Detail controller for the feature widget
 */
(function() {
	'use strict';

	angular.module(HygieiaConfig.module).controller(
			'StoryWidgetDetailController', StoryWidgetDetailController);

	StoryWidgetDetailController.$inject = [ '$scope', '$uibModalInstance',
			'stories' ];
	function StoryWidgetDetailController($scope, $uibModalInstance, stories) {
		var ctrl = this;

		ctrl.stories = stories;

		ctrl.close = close;

		function close() {
			$uibModalInstance.dismiss('close');
		}
	}
})();