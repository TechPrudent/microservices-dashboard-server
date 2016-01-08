(function() {
  'use strict';

  var elm, scope;

  beforeEach(module('msgGraph'));

  beforeEach(inject(function($rootScope, $compile){

    // define the directive
    elm = angular.element(
      '<my-scatter-chart class="chart" data="data">' +
      '</my-scatter-chart>'
    );

    // define the data on the scope
    scope= $rootScope.$new();
    scope.data=[];

    $compile(elm)(scope);
    scope.$digest();

  }));

  describe('my-scatter-chart', function(){
    it('should create svg parent', function(){
      var svg = elm.find('svg');
      expect(svg.length).toBe(1);
    });
});


})();
