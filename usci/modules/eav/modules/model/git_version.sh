#!/bin/sh

mkdir -p "$1"

branch=`git branch|grep \*|cut -d" " -f 2`
commit=`git describe`

echo "buildNumber=${branch}.${commit}" > "$1/$2"
