'use strict';

describe('The bottom bar', function () {
  var page;

  beforeEach(function () {
    browser.get('/index.html');
    page = require('./bottombar.po');
  });

  it('should have four buttons', function() {
    expect(page.buttons.count()).toBe(4);
  });

});
