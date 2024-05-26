#!/bin/bash

java -cp target/classes com.distributedsystems.logquerier.Client "ERROR -c"
java -cp target/classes com.distributedsystems.logquerier.Client "ERROR -i"
java -cp target/classes com.distributedsystems.logquerier.Client "ERROR -v"
java -cp target/classes com.distributedsystems.logquerier.Client "ERROR -w"
java -cp target/classes com.distributedsystems.logquerier.Client "ERROR -l"
java -cp target/classes com.distributedsystems.logquerier.Client "INFO -L"
java -cp target/classes com.distributedsystems.logquerier.Client "ERROR"
java -cp target/classes com.distributedsystems.logquerier.Client "ERROR -l -c"
java -cp target/classes com.distributedsystems.logquerier.Client "ERROR -w -v"
java -cp target/classes com.distributedsystems.logquerier.Client " .*"
java -cp target/classes com.distributedsystems.logquerier.Client "2023-05-21 .* ERROR"