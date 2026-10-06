-- ============================================================================
-- ORAZAKA — Local DB bootstrap · 50 — AUTOMATION CONTEXT
-- ----------------------------------------------------------------------------
-- Owner: Automation service (ex worker-integrations). Third-party connector
-- credentials (encrypted), execution audit log, and the service's own dedup
-- ledger — all in its OWN database (orazaka_automation_db) under the service's
-- own role since Phase 3a. user_id columns are OPAQUE ActorIds — no FK into
-- the identity context.
-- ============================================================================

-- The password is NOT set here. psql 15 cannot read the environment (\getenv is 16+)
-- and ERR-125 bans a shell script, so `orazaka start` applies ALTER ROLE from
-- AUTOMATION_DB_PASSWORD once the container is healthy. A role created without a
-- password cannot authenticate, so a skipped step fails closed rather than leaving a
-- guessable one — which is what the committed literal was (ADR-035, audit #5).
CREATE ROLE orazaka_automation LOGIN;
CREATE DATABASE orazaka_automation_db OWNER orazaka_automation;
\c orazaka_automation_db
SET ROLE orazaka_automation;

-- Consumer-side idempotency (AGENTS.md §6) — this service's own copy of the
-- dedup ledger, in its own database (contract-copy doctrine, no shared table).
CREATE TABLE processed_messages (
    consumer VARCHAR(100) NOT NULL,
    message_id VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (consumer, message_id)
);
CREATE TABLE connector_credentials (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         VARCHAR(255) NOT NULL,
    connector_type  VARCHAR(50)  NOT NULL,
    credential_key  VARCHAR(255) NOT NULL,
    encrypted_value TEXT         NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_connector_credentials_user_type_key UNIQUE (user_id, connector_type, credential_key)
);
CREATE INDEX idx_connector_credentials_user_type ON connector_credentials(user_id, connector_type);

CREATE TABLE automation_job_execution_log (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id           VARCHAR(255) NOT NULL,
    user_id          VARCHAR(255) NOT NULL,
    connector_type   VARCHAR(50)  NOT NULL,
    action           VARCHAR(255) NOT NULL,
    status           VARCHAR(50)  NOT NULL,
    payload          JSONB,
    result           JSONB,
    error_message    TEXT,
    started_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    completed_at     TIMESTAMP,
    duration_ms      BIGINT
);
CREATE INDEX idx_job_exec_log_job_id ON automation_job_execution_log(job_id);
CREATE INDEX idx_job_exec_log_user_status ON automation_job_execution_log(user_id, status);
CREATE INDEX idx_job_exec_log_started_at ON automation_job_execution_log(started_at);

-- ── Quartz (JDBC job store of the automation scheduler) ─────────────────────
-- Internal Quartz FKs are same-context and stay.

