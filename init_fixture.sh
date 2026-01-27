#!/bin/bash

# Base URL
URL="http://localhost:8080"

# Check if jbang is installed
if ! command -v jbang &> /dev/null; then
    echo "Error: jbang is required but not installed."
    echo "Install it from: https://www.jbang.dev/download/"
    exit 1
fi


echo "========================================="
echo "Initializing Enhanced Fixture Data"
echo "========================================="

# 1. Create a Session
SESSION_ID=$(uuidgen)
echo "Session ID: $SESSION_ID"
echo ""

# Define 20 creative team names with 2-5 members each
declare -a TEAM_NAMES=(
    "Cosmic Defenders:StarLord,Gamora,Drax"
    "Shadow Warriors:Nightwing,Raven,BeastBoy"
    "Quantum Squad:Flash,Quicksilver,Zoom"
    "Mystic Alliance:DoctorStrange,Scarlet Witch,Zatanna"
    "Tech Titans:IronMan,Cyborg,BlueBeetle"
    "Emerald Knights:GreenLantern,GreenArrow,Hulk"
    "Thunder Legion:Thor,Storm,Electro,StaticShock"
    "Phantom Force:Ghost Rider,Deadman,Spectre"
    "Velocity Vanguard:Superman,Supergirl,PowerGirl"
    "Crimson Crusaders:Daredevil,RedHood,Hellboy"
    "Arctic Avengers:Iceman,MrFreeze,CaptainCold"
    "Inferno Brigade:HumanTorch,Firestorm,Pyro"
    "Stealth Syndicate:BlackWidow,BlackPanther,Batman"
    "Psionic Protectors:ProfessorX,JeanGrey,MartianManhunter"
    "Aquatic Armada:Aquaman,Namor,Mera"
    "Savage Squad:Wolverine,Sabretooth,Beast,X23"
    "Celestial Champions:CaptainMarvel,Shazam,Thor"
    "Dark Dimension:Thanos,Darkseid,Apocalypse"
    "Speedster Alliance:Flash,Quicksilver,Sonic,Dash"
    "Ultimate Warriors:CaptainAmerica,WonderWoman,Gladiator"
)

# Array to store team IDs
declare -a TEAM_IDS=()

# 2. Register 20 Teams
echo "========================================="
echo "Registering 20 Teams..."
echo "========================================="

for i in "${!TEAM_NAMES[@]}"; do
    IFS=':' read -r TEAM_NAME MEMBERS <<< "${TEAM_NAMES[$i]}"
    
    echo "[$((i+1))/20] Registering: $TEAM_NAME"
    
    # URL encode the team name and members
    ENCODED_NAME=$(echo "$TEAM_NAME" | sed 's/ /%20/g')
    ENCODED_MEMBERS=$(echo "$MEMBERS" | sed 's/ /%20/g')
    
    TEAM_RESP=$(curl -s -X POST "$URL/api/teams/register?name=$ENCODED_NAME&members=$ENCODED_MEMBERS&sessionId=$SESSION_ID")
    TEAM_ID=$(echo $TEAM_RESP | tr -d '"')
    TEAM_IDS+=("$TEAM_ID")
    
    echo "   Team ID: $TEAM_ID"
done

echo ""
echo "All 20 teams registered successfully!"
echo ""

# 3. Create Round 1
echo "========================================="
echo "Creating Round 1..."
echo "========================================="
curl -s -X POST "$URL/api/rounds?sessionId=$SESSION_ID&roundNo=1"
echo ""
echo "Round 1 created!"
echo ""

# 4. Submit Squad Formations for all 20 teams
echo "========================================="
echo "Submitting Squad Formations..."
echo "========================================="

# Get hero IDs from JSON file using JBang
# We'll select 100 heroes (5 per team for 20 teams)
HERO_IDS=$(jbang extract-heroes.java src/main/resources/all-superheroes.json 100)

# Convert to bash array
HERO_ARRAY=($HERO_IDS)

# Strategies to rotate through
STRATEGIES=("AGGRESSIVE" "DEFENSIVE" "BALANCED")

# Submit roster for each team
for i in "${!TEAM_IDS[@]}"; do
    TEAM_ID="${TEAM_IDS[$i]}"
    
    # Calculate hero indices for this team (5 heroes per team)
    START_IDX=$((i * 5))
    
    # Get 5 hero IDs for this team
    HERO_SET=(
        "${HERO_ARRAY[$START_IDX]}"
        "${HERO_ARRAY[$((START_IDX + 1))]}"
        "${HERO_ARRAY[$((START_IDX + 2))]}"
        "${HERO_ARRAY[$((START_IDX + 3))]}"
        "${HERO_ARRAY[$((START_IDX + 4))]}"
    )
    
    # Select strategy (rotate through the 3 strategies)
    STRATEGY="${STRATEGIES[$((i % 3))]}"
    
    echo "[$((i+1))/20] Submitting roster for Team ${TEAM_IDS[$i]}"
    echo "   Heroes: ${HERO_SET[*]}"
    echo "   Strategy: $STRATEGY"
    
    # Submit the roster
    curl -s -X POST "$URL/api/rounds/1/submit?teamId=$TEAM_ID" \
      -H "Content-Type: application/json" \
      -d "{\"heroIds\":[${HERO_SET[0]},${HERO_SET[1]},${HERO_SET[2]},${HERO_SET[3]},${HERO_SET[4]}],\"strategy\":\"$STRATEGY\"}"
    
    echo ""
done

echo ""
echo "========================================="
echo "Fixture Data Initialized Successfully!"
echo "========================================="
echo "Session ID: $SESSION_ID"
echo "Total Teams: 20"
echo "Round 1: Created with 20 squad submissions"
echo ""
echo "You can now create matches between any of the 20 teams!"
echo "========================================="
