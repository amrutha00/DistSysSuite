import os
import random

# Define the directories and file naming patterns
log_dirs = {
    "log_dir_1": "log1",
    "log_dir_2": "log2",
    "log_dir_3": "log3"
}

# Function to create directories if they don't exist
def create_directories():
    for log_dir in log_dirs.keys():
        os.makedirs(log_dir, exist_ok=True)

# Function to generate empty logs
def generate_empty_logs():
    for log_dir, log_name in log_dirs.items():
        for i in range(1, 3):  # Create 3 empty log files for each directory
            file_path = os.path.join(log_dir, f"{log_name}{i}.log")
            open(file_path, 'w').close()

# Function to generate diverse log lines
def generate_log_line():
    levels = ["INFO", "ERROR", "DEBUG", "WARN", "TRACE"]
    messages = [
        "Starting server",
        "Failed to start service",
        "Service started successfully",
        "Connection timed out",
        "Client connected",
        "Low disk space",
        "Failed to read configuration file",
        "Retrying connection",
        "Client disconnected unexpectedly",
        "Out of memory",
        "Shutting down server"
    ]
    return f"2023-05-21 10:{random.randint(10, 59)}:{random.randint(10, 59)} {random.choice(levels)} {random.choice(messages)}\n"

# Function to generate large logs
def generate_large_logs():
    for log_dir, log_name in log_dirs.items():
        for i in range(3, 6):  # Create 3 large log files for each directory
            file_path = os.path.join(log_dir, f"{log_name}{i}.log")
            with open(file_path, 'w') as f:
                for _ in range(10000):  # 10000 lines of log
                    f.write(generate_log_line())

# Function to generate logs with invalid patterns
def generate_invalid_pattern_logs():
    invalid_log_content = [
        "Invalid log entry without timestamp or level\n",
        "Another invalid entry\n",
        "Random text without any structure\n"
    ]
    for log_dir, log_name in log_dirs.items():
        for i in range(6, 8):  # Create 3 invalid pattern log files for each directory
            file_path = os.path.join(log_dir, f"{log_name}{i}.log")
            with open(file_path, 'w') as f:
                for _ in range(100):  # 100 lines of invalid log content
                    f.write(random.choice(invalid_log_content))

# Main function to generate all types of logs
def main():
    create_directories()
    generate_empty_logs()
    generate_large_logs()
    generate_invalid_pattern_logs()
    print("Log files generated successfully.")

if __name__ == "__main__":
    main()
