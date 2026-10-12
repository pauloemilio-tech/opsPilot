CREATE TABLE ai_usage_quotas (
    id UUID PRIMARY KEY,
    scope VARCHAR(20) NOT NULL,
    visitor_hash VARCHAR(128),
    usage_date DATE NOT NULL,
    request_count INTEGER NOT NULL DEFAULT 0,
    cooldown_until TIMESTAMP(6) WITH TIME ZONE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_ai_usage_quotas_scope CHECK (scope IN ('VISITOR', 'GLOBAL')),
    CONSTRAINT uk_ai_usage_quotas_scope_visitor_date UNIQUE (scope, visitor_hash, usage_date),
    CONSTRAINT ck_ai_usage_quotas_request_count CHECK (request_count >= 0)
);

CREATE INDEX idx_ai_usage_quotas_usage_date ON ai_usage_quotas (usage_date);
CREATE INDEX idx_ai_usage_quotas_scope_visitor ON ai_usage_quotas (scope, visitor_hash, usage_date);
