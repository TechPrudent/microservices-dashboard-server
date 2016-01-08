(function() {
  'use strict';

describe("Test graph node filter", function() {

  beforeEach(module('microServicesGui'));

  it('should have a node filter', inject(function($filter) {
    expect($filter('nodeFilter')).toBeDefined();
  }));

  it('should have a node filter that produces an array of nodes',
    inject(function($filter) {

    var nodeSearch={id:'grp'};
    var node1 = {id:'ngrpGetEligibleAwards'};
    var node2 = {id:'notNode'};
    var node3 = {id:'grp2'};
    var nodesList = [node1, node2, node3];

    var nodes = $filter('nodeFilter')(nodesList, nodeSearch);

    expect(nodes.length).toEqual(2);
    expect(nodes[0]).toEqual(node1);
    expect(nodes[1]).toEqual(node3);
  }));

  it('should return [] when nothing is set',
    inject(function($filter) {

    var nodes = $filter('nodeFilter')([], '');
    expect(nodes).toBeDefined();
    expect(nodes.length).toEqual(0);
  }));

});
})();
