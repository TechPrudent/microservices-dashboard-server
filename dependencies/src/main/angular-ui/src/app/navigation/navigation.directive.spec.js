(function() {
  'use strict';

  describe('NavigationDirective', function(){

    var $scope,$compile,element,template;

    beforeEach(module('microServicesGui'));

    beforeEach(inject(function($rootScope,_$compile_){
      $scope = $rootScope.$new();
      $compile = _$compile_;
    }));

    it('Should initialize the scope for the navigation directive',function() {
      givenTemplate();
      whenDirectiveUsed();
      thenScopeIsDefined();
    });

    function givenTemplate(){
      template='<div data-msg-navigation></div>';
    }

    function whenDirectiveUsed(){
      element = $compile(template)($scope);
      $scope.$digest();
    }

    function thenScopeIsDefined(){
      expect($scope).toBeDefined();
    }

  });
})();
