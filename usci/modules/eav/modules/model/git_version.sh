#!/bin/sh

branch=`git branch|grep \*|cut -d" " -f 2`
commit=`git id`

echo "buildNumber=${branch}.${commit}" > "$1"
