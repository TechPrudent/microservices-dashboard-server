/*
(function() {
  'use strict';

  describe('GraphController', function(){

    beforeEach(module('microServicesGui'), function($provide) {
          $provide.value('GraphService', mockGraphService);
          $provide.value('NodeService', mockNodeService);
          $provide.value('SetService', mockSetService);
        });

    var GraphController, scope, filter, modalInstance;

    var mockGraphService = {
          getGraph: function () {
            return [];
          },
          getStates: function () {
            return ['State1', 'State2', 'State3'];
          },
          getTypes: function () {
            return [];
          },
          getGroups: function () {
            return [];
          }
        };

        var mockNodeService = {
          pushNode: function() {

          },
          getNode: function() {
            return {};
          },
          setNode: function() {

          }
        };

        var mockSetService = {
          add: function() {

          },
          has: function() {
            return true;
          }
        };

        beforeEach(inject(function($rootScope, $controller, _$filter_) {
              scope = $rootScope.$new();
              filter = _$filter_;
              modalInstance = {                    // Create a mock object using spies
                close: jasmine.createSpy('modalInstance.close'),
                dismiss: jasmine.createSpy('modalInstance.dismiss'),
                result: {
                  then: jasmine.createSpy('modalInstance.result.then')
                }
              };
              GraphController = $controller('GraphController', {
                $scope: scope,
                $filter: filter,
                $modalInstance: modalInstance
              });
            }));

    it('should define more than 5 awesome things', inject(function($controller) {
      var vm = $controller('GraphController');



//      expect(angular.isArray(vm.awesomeThings)).toBeTruthy();
//      expect(vm.awesomeThings.length > 5).toBeTruthy();
    }));
  });
})();
*/
