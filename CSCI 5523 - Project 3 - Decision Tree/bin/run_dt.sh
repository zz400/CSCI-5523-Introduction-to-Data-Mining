#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
trainfile="$DIR/data/rep1/train.csv"
testfile="$DIR/data/rep1/test.csv"
modelfile="$DIR/modelfile.txt"
predictions="$DIR/predictions.txt"

java dtinduce $trainfile 1 $modelfile
java dtclassify $modelfile $testfile $predictions
java showconfmatrix $predictions

java dtinduce $trainfile 5 $modelfile
java dtclassify $modelfile $testfile $predictions
java showconfmatrix $predictions

java dtinduce $trainfile 10 $modelfile
java dtclassify $modelfile $testfile $predictions
java showconfmatrix $predictions

java dtinduce $trainfile 20 $modelfile
java dtclassify $modelfile $testfile $predictions
java showconfmatrix $predictions



trainfile="$DIR/data/rep2/train.csv"
testfile="$DIR/data/rep2/test.csv"

java dtinduce $trainfile 1 $modelfile
java dtclassify $modelfile $testfile $predictions
java showconfmatrix $predictions

java dtinduce $trainfile 5 $modelfile
java dtclassify $modelfile $testfile $predictions
java showconfmatrix $predictions

java dtinduce $trainfile 10 $modelfile
java dtclassify $modelfile $testfile $predictions
java showconfmatrix $predictions

java dtinduce $trainfile 20 $modelfile
java dtclassify $modelfile $testfile $predictions
java showconfmatrix $predictions
