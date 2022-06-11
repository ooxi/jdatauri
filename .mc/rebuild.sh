#!/bin/bash

# @see https://stackoverflow.com/a/61705002
export JAVA_HOME=$(update-alternatives --query javadoc | grep Value: | head -n1 | sed 's/Value: //' | sed 's@bin/javadoc$@@')

mvn clean install

