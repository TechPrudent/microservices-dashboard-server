'use strict';

describe('The nav bar', function () {
  var page;

  beforeEach(function () {
    browser.get('/index.html');
    page = require('./nav.po');
  });

  it('should include a title with the correct text', function() {
    expect(page.h1El.getText()).toBe('Microservice GUI');
  });

});
