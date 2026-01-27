#!/bin/bash

# Base URL
URL="http://localhost:8080"

echo "Initializing Fixture Data..."

# 1. Create a Session
SESSION_ID=$(uuidgen)
echo "Session ID: $SESSION_ID"

# 2. Register Team A
# We'll parse the GUID from the response (assumed to be a raw string in quotes, e.g. "uuid")
# or simpler: just passed as raw body? The controller returns ResponseEntity<UUID>.
TEAM_A_NAME="The Avengers"
TEAM_A_RESP=$(curl -s -X POST "$URL/api/teams/register?name=${TEAM_A_NAME// /%20}&members=IronMan,Thor&sessionId=$SESSION_ID")
# Remove quotes if present
TEAM_A_ID=$(echo $TEAM_A_RESP | tr -d '"')
echo "Team A ID: $TEAM_A_ID"

# 3. Register Team B
TEAM_B_NAME="Justice League"
TEAM_B_RESP=$(curl -s -X POST "$URL/api/teams/register?name=${TEAM_B_NAME// /%20}&members=Batman,Superman&sessionId=$SESSION_ID")
TEAM_B_ID=$(echo $TEAM_B_RESP | tr -d '"')
echo "Team B ID: $TEAM_B_ID"

# 4. Create Round 1
echo "Creating Round 1..."
curl -s -X POST "$URL/api/rounds?sessionId=$SESSION_ID&roundNo=1"
echo ""

# 5. Submit Roster for Team A (Heroes 1-5)
echo "Submitting Roster for Team A..."
curl -s -X POST "$URL/api/rounds/1/submit?teamId=$TEAM_A_ID" \
  -H "Content-Type: application/json" \
  -d '{"heroIds":[1,2,3,4,5],"strategy":"AGGRESSIVE"}'
echo ""

# 6. Submit Roster for Team B (Heroes 6,7,8,10,11 - ID 9 is missing in data)
echo "Submitting Roster for Team B..."
curl -s -X POST "$URL/api/rounds/1/submit?teamId=$TEAM_B_ID" \
  -H "Content-Type: application/json" \
  -d '{"heroIds":[6,7,8,10,11],"strategy":"DEFENSIVE"}'
echo ""

# 7. Create Match
echo "Creating Match..."
MATCH_RESP=$(curl -s -X POST "$URL/api/matches/create?teamA=$TEAM_A_ID&teamB=$TEAM_B_ID&roundNo=1")
MATCH_ID=$(echo $MATCH_RESP | tr -d '"')
echo "Match ID: $MATCH_ID"

# 8. Run Match
echo "Running Match..."
curl -s -X POST "$URL/api/matches/$MATCH_ID/run"
echo ""

echo "Fixture Data Initialized."
echo "View Replay at: $URL/battle.html?matchId=$MATCH_ID"
