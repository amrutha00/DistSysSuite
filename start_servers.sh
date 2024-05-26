#!/bin/bash

# Maven build commands
echo "Cleaning and compiling the project..."
mvn clean compile
#echo "Skipping tests and compiling..."
#mvn compile #-Dmaven.test.skip=true

# Define the base classpath and the main class to execute
CLASSPATH="target/classes"
MAIN_CLASS="com.distributedsystems.logquerier.Server"

# Ports configuration
declare -a ports=("55551" "55552" "55553")

# Kill existing server processes and start new ones
for port in "${ports[@]}"; do
    echo "Checking for existing processes on port $port..."
    PID=$(lsof -ti:$port)
    if [ ! -z "$PID" ]; then
        echo "Killing existing process on port $port with PID $PID..."
        kill -9 $PID
        sleep 1  # Wait a bit to ensure the process has been killed
    fi
    echo "Starting server on port $port..."
    java -cp $CLASSPATH $MAIN_CLASS localhost $port &
    echo "Server started on port $port with new PID $!"
done

echo "All servers have been started."
