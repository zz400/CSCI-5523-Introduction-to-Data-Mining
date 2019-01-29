#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
trainfile="$DIR/data/rep2/train.csv"
testfile="$DIR/data/rep2/test.csv"
writePath="$DIR/out/"


java rf 1 $trainfile $testfile $writePath
java rfmerge $writePath

java rf 5 $trainfile $testfile $writePath
java rfmerge $writePath

java rf 10 $trainfile $testfile $writePath
java rfmerge $writePath

java rf 20 $trainfile $testfile $writePath
java rfmerge $writePath
