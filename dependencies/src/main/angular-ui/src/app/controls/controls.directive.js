(function () {
  'use strict';

  angular
    .module('microServicesGui')
    .directive('msgControls', ['GraphService', ControlsDirective]);

  /** @ngInject */
  function ControlsDirective(GraphService) {
    return {
      templateUrl: 'app/controls/controls.html',
      controller: 'GraphController',
      link: function (scope, elem, attrs, graphController) {
        scope.$watchGroup(['nodeSearch.id', 'nodeSearch.status', 'nodeSearch.type', 'nodeSearch.group'], function () {
          graphController.filterNodes(scope.nodeSearch);
        })
        ;

        scope.states = GraphService.getStates();
        scope.types = GraphService.getTypes();
        scope.groups = GraphService.getGroups();

        scope.$watch('graphData', function () {

          if (scope.graphData != undefined){

            scope.states = GraphService.getStates();

            var i = scope.states.length;
            while (i--) {
              var state = scope.states[i];
              if (countState(state) === 0) {
                scope.states.splice(i, 1);
              }
            }

            scope.states.forEach(function (state, i) {
              scope.states[i].value = state.value + " - " + scope.graphData.nodes.filter(function (d) {
                  if (state.value === 'ALL') {
                    return true;
                  }
                  if (d.details !== undefined) {
                    return d.details.status === state.value;
                  }
                  return false;

                }).length;
            });

            scope.types = GraphService.getTypes();

            i = scope.types.length;
            while (i--) {
              var type = scope.types[i];
              if (countType(type) === 0) {
                scope.types.splice(i, 1);
              }
            }

            scope.types.forEach(function (type, i) {
              scope.types[i].value = type.value + " - " + scope.graphData.nodes.filter(function (d) {
                    if (type.value === 'ALL') {
                      return true;
                    }
                    if (d.details !== undefined) {
                      return d.details.type === type.value;
                    }
                    return false;
                  }
                ).length;
            });

            scope.groups = GraphService.getGroups();

            i = scope.groups.length;
            while (i--) {
              var group = scope.groups[i];
              if (countGroup(group) === 0) {
                scope.groups.splice(i, 1);
              }
            }

            scope.groups.forEach(function (group, i) {
              scope.groups[i].value = group.value + " - " + scope.graphData.nodes.filter(function (d) {
                  if (group.value === 'ALL') {
                    return true;
                  }
                  if (d.details !== undefined) {
                    return d.details.group === group.value;
                  }
                  return false;

                }).length;
            });
        }});

        scope.clearFilter = function () {

          scope.nodeSearch.id = "";

          scope.nodeSearch.status = null;
          scope.nodeSearch.type = null;
          scope.nodeSearch.group = null;
        };

        function countState(state) {
          return scope.graphData.nodes.filter(function (d) {
            if (d.details !== undefined) {
              if (state.key === d.details.status) {
                return true;
              }
            }
            return false;
          }).length;
        }

        function countType(type) {
          return scope.graphData.nodes.filter(function (d) {
            if (d.details !== undefined) {
              if (type.key === d.details.type) {
                return true;
              }
            }
            return false;
          }).length;
        }

        function countGroup(group) {
          return scope.graphData.nodes.filter(function (d) {
            if (d.details !== undefined) {
              if (group.key === d.details.group) {
                return true;
              }
            }
            return false;
          }).length;
        }
      }
    };
  }


})();
