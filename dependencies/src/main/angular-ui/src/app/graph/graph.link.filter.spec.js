(function() {
  'use strict';

describe("Test graph link filter", function() {

  beforeEach(module('microServicesGui'));

  it('should have a link filter', inject(function($filter) {
    expect($filter('linkFilter')).toBeDefined();
  }));

  it('should have a link filter that produces an array of links',
    inject(function($filter) {

    var node1 = {id:'ordering-loyalty'};
    var node2 = {id:'ngrpGetEligibleAwards'};
    var node3 = {id:'rheHandleOrderHistory'};
    var nodesList = [node1, node2, node3];

    var link1 = {source:node1, target:node2};
    var link2 = {source:node1, target:node3};
    var linksList = [link1, link2];

    var links = $filter('linkFilter')(linksList, nodesList);

    expect(links.length).toEqual(2);
    expect(links[0]).toEqual(link1);
    expect(links[1]).toEqual(link2);
  }));

  it('should have a link filter that produces an array of links without nodes nont in the node list',
      inject(function($filter) {

      var node1 = {id:'ordering-loyalty'};
      var node2 = {id:'ngrpGetEligibleAwards'};
      var node3 = {id:'rheHandleOrderHistory'};
      var nodesList = [node1, node2];

      var link1 = {source:node1, target:node2};
      var link2 = {source:node1, target:node3};
      var linksList = [link1, link2];

      var links = $filter('linkFilter')(linksList, nodesList);

      expect(links.length).toEqual(1);
      expect(links[0]).toEqual(link1);
      expect(links[1]).toBeUndefined();
    }));

  it('should return [] when nothing is set',
    inject(function($filter) {

    var links = $filter('linkFilter')([], []);
    expect(links).toBeDefined();
    expect(links.length).toEqual(0);
  }));

});
})();
