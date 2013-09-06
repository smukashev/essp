@echo off

if not exist .\src\main mkdir .\src\main
if not exist .\src\main\resources mkdir .\src\main\resources

if not exist .\src\main\java mklink /j .\src\main\java .\docroot\WEB-INF\src
if not exist .\src\main\webapp mklink /j .\src\main\webapp .\docroot


