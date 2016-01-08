(function() {
  'use strict';

  describe('NodeModalController', function(){

    beforeEach(module('microServicesGui'), function($provide) {
      $provide.value('GraphService', mockGraphService);
      $provide.value('NodeService', mockNodeService);
      $provide.value('SetService', mockSetService);
    });

    var NodeModalController, scope, filter, modalInstance;

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
      NodeModalController = $controller('NodeModalController', {
        $scope: scope,
        $filter: filter,
        $modalInstance: modalInstance
      });
    }));

    it('should have a newNode instance', function() {
      expect(scope.newNode).toBeDefined();
    });

    it('should have 3 states', function() {
      expect(angular.isArray(scope.states)).toBeTruthy();
      expect(scope.states.length === 3).toBeTruthy();
    });

    it('should have the correct configuration for the modalInstance', function() {
      expect(scope.nodesList).toBeDefined();
      expect(scope.nodesList.placeholder).toMatch('node-placeholder');
      expect(scope.nodesList.connectWith).toMatch('.links-container');
    });

    it('should close the modal if the ok function is triggered', function() {
      scope.ok();
      expect(modalInstance.close).toHaveBeenCalledWith(scope.newNode);
    });

    it('should dismiss the modal if the cancel function is triggered', function() {
      scope.cancel();
      expect(modalInstance.dismiss).toHaveBeenCalledWith('cancel');
    });
  });
})();
