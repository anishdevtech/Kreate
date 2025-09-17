#!/bin/bash

# Simple script to push code to the remote server

# Move to your project directory if needed
# cd /DATA/Anish/innersaavn

# Stage all changes
git add .

# Commit with a message passed as an argument or a default message
commit_message="Auto commit:PUSH.SH"
if [ "$1" ]; then
    commit_message="$1"
fi

git commit -m "$commit_message"

# Push to the default branch
git push

echo "Code has been pushed successfully."