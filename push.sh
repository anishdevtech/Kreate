#!/bin/bash

# Automatically generate a commit message summarizing staged changes

# Move to your project directory if needed
# cd /DATA/Anish/innersaavn

# Stage all changes
git add .

# Generate commit message from staged changes summary
commit_message=$(git diff --cached --stat --summary | head -n 5 | tr '\n' ' ' | sed 's/  */ /g')

# Fallback if no staged changes detected
if [ -z "$commit_message" ]; then
  commit_message="Auto commit: No staged changes detected"
else
  # Truncate to 72 characters to follow commit message line length conventions
  commit_message=$(echo "$commit_message" | cut -c1-72)
  # Prefix to indicate auto generation
  commit_message="Auto commit: $commit_message"
fi

echo "Committing with message:"
echo "$commit_message"

git commit -m "$commit_message"

# Push to default branch
git push

echo "Code has been pushed successfully."
