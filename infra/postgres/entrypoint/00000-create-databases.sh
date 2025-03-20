#!/bin/bash

set -e
set -u

DATE_PATTERN="%Y-%m-%dT%H:%M:%SZ"

function log_info() {
  echo -e "$(date -u +"$DATE_PATTERN") - \033[0;34m[INFO]\033[0m $1"
}

function log_success() {
  echo -e "$(date -u +"$DATE_PATTERN") - \033[0;32m[SUCCESS]\033[0m $1"
}

function log_error() {
  echo -e "$(date -u +"$DATE_PATTERN") - \033[0;31m[ERROR]\033[0m $1"
  return 1
}

function process() {
  local DB="$1"
  local SCHEMA="$2"
  local USER="$3"
  local PASS="$4"

  log_info "Processing... database: '$DB', schema: '$SCHEMA', user: '$USER'"

  # Check if the username doesn't exists
  if psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -tAc "SELECT 1 FROM pg_roles WHERE rolname='$USER'" | grep -q "1"; then
    log_info "User '$USER' already exists"
  else
    log_info "Creating user '$USER'..."
    if psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -tAc "CREATE USER \"$USER\" WITH PASSWORD '$PASS';"; then
      log_success "User '$USER' successfully created!"
    else
      log_error "Error creating user '$USER'"
      return 1
    fi
  fi

  # Check if database doesn't exists
  if psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -tAc "SELECT 1 FROM pg_database WHERE datname='$DB'" | grep -q "1"; then
    log_info "Database '$DB' already exists"
  else
    log_info "Creating database '$DB'..."
    if psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -c "CREATE DATABASE \"$DB\";"; then
      log_success "Database '$DB' successfully created!"
    else
      log_error "Error creating database '$DB'"
      return 1
    fi
  fi

  # Grants privileges to the user on the database
#  log_info "Granting privileges to user '$USER' on database '$DB'..."
  if psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -c "GRANT ALL PRIVILEGES ON DATABASE \"$DB\" TO \"$USER\";"; then
    log_success "Privilégios concedidos com sucesso"
  else
    log_error "Falha ao conceder privilégios ao usuário '$USER' no banco '$DB'"
    return 1
  fi

	# Create the schema and configure permissions
  if psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$DB" -c "CREATE SCHEMA IF NOT EXISTS \"$SCHEMA\" AUTHORIZATION \"$USER\";"; then
    log_success "Schema '$SCHEMA' successfully created!"
  else
    log_error "Error creating schema '$SCHEMA'"
    return 1
  fi

  # Set the schema as default for the user
  if psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$DB" -c "ALTER ROLE \"$USER\" SET search_path TO \"$SCHEMA\";"; then
    log_success "Search path configured for the user '$USER'"
  else
    log_error "Failed to set search path for user '$USER'"
    return 1
  fi

  log_success "Configuration completed for DB: $DB, Schema: $SCHEMA, User: $USER"
  return 0
}

function start() {
	log_info "Starting multiple database creation..."

	local PROCESSED_DBS=0
  local DATABASE_ENV_VAR

  local POSTGRES_MULTIPLE_DATABASES=$(compgen -e | grep '^POSTGRES_MULTIPLE_DATABASE_' | sort)
  local PROCESSED_DBS=0

	for DATABASE_ENV_VAR in $POSTGRES_MULTIPLE_DATABASES; do
		local VALUE="${!DATABASE_ENV_VAR}"

		if [ -z "$VALUE" ]; then
			log_info "Variable '$DATABASE_ENV_VAR' is empty, skipping..."
			continue
		fi

		log_info "Processing configuration '$VALUE'"
		IFS='/' read -r -a ARRAY <<< "$VALUE"

		if [ ${#ARRAY[@]} -ne 4 ]; then
			log_error "Invalid format for variable '$DATABASE_ENV_VAR' with value '$VALUE'. Use: DATABASE/SCHEMA/USER/PASSWORD"
			continue
		fi

		if process "${ARRAY[@]}"; then
			PROCESSED_DBS=$((PROCESSED_DBS + 1))
		else
			log_error "Error processing '$DATABASE_ENV_VAR' with value '$VALUE'"
		fi
	done

	log_info "Total of $PROCESSED_DBS database configuration processed!"
}

if [ -z "${POSTGRES_USER:-}" ]; then
	log_error "Variable 'POSTGRES_USER' is not defined!"
	exit 1
fi

start "$@"
