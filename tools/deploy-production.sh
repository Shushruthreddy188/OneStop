#!/usr/bin/env bash
set -euo pipefail

REPO_DIR="${ONESTOP_REPO_DIR:-/home/ubuntu/OneStop}"
ENV_FILE="$REPO_DIR/.env.production"
COMPOSE_FILE="$REPO_DIR/compose.prod.yml"

exec 9>/tmp/onestop-production-deploy.lock
flock -n 9 || { echo "Another OneStop deployment is already running" >&2; exit 1; }

cd "$REPO_DIR"
[[ -r "$ENV_FILE" ]] || { echo "Missing $ENV_FILE" >&2; exit 1; }

if ! git diff --quiet || ! git diff --cached --quiet; then
  echo "Tracked production files contain uncommitted changes; refusing to deploy" >&2
  exit 1
fi

echo "Creating pre-deployment database backup..."
"$REPO_DIR/tools/backup-production.sh"

echo "Fetching origin/main..."
git fetch origin main
git merge --ff-only origin/main
revision="$(git rev-parse --short HEAD)"

echo "Validating production Compose configuration..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" config --quiet

echo "Building revision $revision sequentially..."
COMPOSE_PARALLEL_LIMIT=1 docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" build

echo "Starting revision $revision..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d --remove-orphans

check_url() {
  local url="$1"
  local label="$2"
  for attempt in $(seq 1 60); do
    if curl --fail --silent --show-error --max-time 10 "$url" >/dev/null; then
      echo "$label is healthy"
      return 0
    fi
    echo "Waiting for $label ($attempt/60)..."
    sleep 10
  done
  echo "$label did not recover after 10 minutes" >&2
  return 1
}

check_url "https://onestop.linkdrop.live/" "OneStop website"
check_url "https://onestop.linkdrop.live/api/products?size=1" "OneStop catalog API"

echo "Successfully deployed OneStop revision $revision"
