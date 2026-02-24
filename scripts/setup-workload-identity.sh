#!/bin/bash
export PROJECT_ID="talent-arena-26"
export GITHUB_REPO="anyulled/superhero-battle-arena"
export SERVICE_ACCOUNT_NAME="github-actions"

SERVICE_ACCOUNT_EMAIL="${SERVICE_ACCOUNT_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

echo "=========================================================="
echo "Starting Workload Identity Federation Setup"
echo "Project: $PROJECT_ID"
echo "Repo: $GITHUB_REPO"
echo "=========================================================="

# 1. Ensure required APIs are enabled
echo "[1] Enabling required APIs..."
gcloud services enable iamcredentials.googleapis.com \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com \
  --project="${PROJECT_ID}"

# 2. Check and Create Service Account
echo "[2] Setting up Service Account..."
if gcloud iam service-accounts describe "${SERVICE_ACCOUNT_EMAIL}" --project="${PROJECT_ID}" >/dev/null 2>&1; then
  echo "    Service Account '${SERVICE_ACCOUNT_NAME}' already exists."
else
  echo "    Creating Service Account '${SERVICE_ACCOUNT_NAME}'..."
  gcloud iam service-accounts create "${SERVICE_ACCOUNT_NAME}" \
    --project="${PROJECT_ID}" \
    --display-name="GitHub Actions Service Account"
  # Wait for IAM propagation
  echo "    Waiting for IAM propagation..."
  sleep 10
fi

# 3. Grant required roles to the Service Account
echo "[3] Granting roles to Service Account..."
ROLES=(
  "roles/run.admin"
  "roles/iam.serviceAccountUser"
  "roles/cloudbuild.builds.editor"
  "roles/storage.admin"
  "roles/artifactregistry.admin"
  "roles/serviceusage.serviceUsageConsumer"
)

for role in "${ROLES[@]}"; do
  # Determine if binding already exists
  if gcloud projects get-iam-policy "${PROJECT_ID}" --flatten="bindings[].members" --format="json" | grep -A 10 "${role}" | grep -q "serviceAccount:${SERVICE_ACCOUNT_EMAIL}"; then
    echo "    Role '${role}' already bound."
  else
    echo "    Binding role '${role}'..."
    gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
      --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
      --role="${role}" > /dev/null
  fi
done

# 4. Check and Create Workload Identity Pool
export POOL_NAME="github-actions-pool"
echo "[4] Setting up Workload Identity Pool..."
if gcloud iam workload-identity-pools describe "${POOL_NAME}" --project="${PROJECT_ID}" --location="global" >/dev/null 2>&1; then
  echo "    Pool '${POOL_NAME}' already exists."
else
  echo "    Creating Pool '${POOL_NAME}'..."
  gcloud iam workload-identity-pools create "${POOL_NAME}" \
    --project="${PROJECT_ID}" \
    --location="global" \
    --display-name="GitHub Actions Pool"
fi

export WORKLOAD_IDENTITY_POOL_ID=$(gcloud iam workload-identity-pools describe "${POOL_NAME}" \
  --project="${PROJECT_ID}" \
  --location="global" \
  --format="value(name)")

# 5. Check and Create Workload Identity Provider
export PROVIDER_NAME="github-provider"
echo "[5] Setting up Workload Identity Provider..."
export EXISTING_PROVIDER=$(gcloud iam workload-identity-pools providers describe "${PROVIDER_NAME}" \
  --project="${PROJECT_ID}" \
  --location="global" \
  --workload-identity-pool="${POOL_NAME}" \
  --format="value(name)" 2>/dev/null)

if [ -n "$EXISTING_PROVIDER" ]; then
  echo "    Provider '${PROVIDER_NAME}' already exists."
  export WORKLOAD_IDENTITY_PROVIDER="$EXISTING_PROVIDER"
else
  echo "    Creating Provider '${PROVIDER_NAME}'..."
  # Notice the added condition to reference the provider's claims and fix the error
  gcloud iam workload-identity-pools providers create-oidc "${PROVIDER_NAME}" \
    --project="${PROJECT_ID}" \
    --location="global" \
    --workload-identity-pool="${POOL_NAME}" \
    --display-name="GitHub Provider" \
    --attribute-mapping="google.subject=assertion.sub,attribute.actor=assertion.actor,attribute.repository=assertion.repository" \
    --attribute-condition="assertion.repository == '${GITHUB_REPO}'" \
    --issuer-uri="https://token.actions.githubusercontent.com"
  
  export WORKLOAD_IDENTITY_PROVIDER=$(gcloud iam workload-identity-pools providers describe "${PROVIDER_NAME}" \
    --project="${PROJECT_ID}" \
    --location="global" \
    --workload-identity-pool="${POOL_NAME}" \
    --format="value(name)")
fi

# 6. Bind the Service Account to the Workload Identity Pool
echo "[6] Binding Service Account to Identity Pool..."
BINDING_MEMBER="principalSet://iam.googleapis.com/${WORKLOAD_IDENTITY_POOL_ID}/attribute.repository/${GITHUB_REPO}"

if gcloud iam service-accounts get-iam-policy "${SERVICE_ACCOUNT_EMAIL}" --project="${PROJECT_ID}" --flatten="bindings[].members" --format="json" | grep -A 10 "roles/iam.workloadIdentityUser" | grep -q "${BINDING_MEMBER}"; then
  echo "    Service Account is already bound to Identity Pool."
else
  echo "    Binding Service Account to Identity Pool..."
  gcloud iam service-accounts add-iam-policy-binding "${SERVICE_ACCOUNT_EMAIL}" \
    --project="${PROJECT_ID}" \
    --role="roles/iam.workloadIdentityUser" \
    --member="${BINDING_MEMBER}" > /dev/null
fi

echo ""
echo "=========================================================="
echo "WORKLOAD IDENTITY SETUP COMPLETE"
echo "=========================================================="
echo "Use the following value for GCP_WORKLOAD_IDENTITY_PROVIDER in GitHub Secrets:"
echo "${WORKLOAD_IDENTITY_PROVIDER}"
echo ""
echo "Use the following value for GCP_SERVICE_ACCOUNT in GitHub Secrets:"
echo "${SERVICE_ACCOUNT_EMAIL}"
echo "=========================================================="
