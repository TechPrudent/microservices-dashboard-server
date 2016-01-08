'use strict';

describe('The control bar', function () {
  var page;

  beforeEach(function () {
    browser.get('/index.html');
    page = require('./controls.po');
  });

  it('should include a clear filter button', function() {
    expect(page.clearButton.getText()).toBe('Clear filter');
  });

  it('should have a drop down list for states with the first option containing ALL', function() {
    var statesList = element.all(by.options('state.value for state in states'));
    expect(statesList.first().getText()).toContain('ALL');
  });

  it('should have a drop down list for type with the first option containing ALL', function() {
    var statesList = element.all(by.options('type.value for type in types'));
    expect(statesList.first().getText()).toContain('ALL');
  });

  it('should have a drop down list for groups with the first option containing ALL', function() {
    var statesList = element.all(by.options('group.value for group in groups'));
    expect(statesList.first().getText()).toContain('ALL');
  });

  it('should have an edit field to filter on text', function() {
    expect(page.searchNode.getAttribute('placeholder')).toBe('Search node');
  });

});
