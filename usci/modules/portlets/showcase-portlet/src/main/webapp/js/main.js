var app = angular.module('showcaseApp', ['ngTable', 'ngResource']);
  app.filter('slice', function() {
    return function(arr, start, end) {
      return arr.slice(start, end);
    };
  });
  app.controller('ShowcaseController', ['$scope', '$http', '$timeout', '$resource', 'ngTableParams', function($scope, $http, $timeout, $resource, ngTableParams) {

    var Api = $resource(dataURL);
    $scope.columns = columns;

    $scope.tableParams = new ngTableParams({
            page: 1,            // show first page
            count: 10           // count per page
        }, {
            total: 0,
            getData: function($defer, params) {
                // ajax request to api
                Api.get(params.url(), function(data) {
                    $timeout(function() {
                        // update table params
                        params.total(data.total);
                        // set new data
                        $defer.resolve(data.result);
                    }, 500);
                });
            }
    });

    $scope.fields = [];

    $scope.addField = function() {
      $scope.fields.push({path:$scope.fieldPath, name:$scope.fieldName});
      $scope.fieldPath = '';
      $scope.fieldName = '';
    };

    $scope.remove = function(field) {
      $scope.fields.splice($scope.fields.indexOf(field), 1);
    };
  }]);

    angular.module('showcaseApp')
    .directive('loadingContainer', function () {
        return {
            restrict: 'A',
            scope: false,
            link: function(scope, element, attrs) {
                var loadingLayer = angular.element('<div class="loading"></div>');
                element.append(loadingLayer);
                element.addClass('loading-container');
                scope.$watch(attrs.loadingContainer, function(value) {
                    loadingLayer.toggleClass('ng-hide', !value);
                });
            }
        };
    });