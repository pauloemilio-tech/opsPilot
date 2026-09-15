CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    industry VARCHAR(100),
    region VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    monthly_revenue NUMERIC(19, 2) NOT NULL,
    previous_month_revenue NUMERIC(19, 2) NOT NULL,
    engagement_score INTEGER NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_accounts_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ONBOARDING')),
    CONSTRAINT ck_accounts_monthly_revenue
        CHECK (monthly_revenue >= 0),
    CONSTRAINT ck_accounts_previous_month_revenue
        CHECK (previous_month_revenue >= 0),
    CONSTRAINT ck_accounts_engagement_score
        CHECK (engagement_score BETWEEN 0 AND 100)
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    order_number VARCHAR(50) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    ordered_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    expected_delivery_at TIMESTAMP(6) WITH TIME ZONE,
    delivered_at TIMESTAMP(6) WITH TIME ZONE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_orders_account
        FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT uk_orders_order_number
        UNIQUE (order_number),
    CONSTRAINT ck_orders_amount
        CHECK (amount >= 0),
    CONSTRAINT ck_orders_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'DELAYED', 'CANCELLED'))
);

CREATE TABLE support_tickets (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    subject VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    opened_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    resolved_at TIMESTAMP(6) WITH TIME ZONE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_support_tickets_account
        FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT ck_support_tickets_status
        CHECK (status IN ('OPEN', 'IN_PROGRESS', 'WAITING_CUSTOMER', 'RESOLVED', 'CLOSED')),
    CONSTRAINT ck_support_tickets_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

CREATE TABLE interactions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    type VARCHAR(20) NOT NULL,
    summary TEXT NOT NULL,
    occurred_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_interactions_account
        FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT ck_interactions_type
        CHECK (type IN ('CALL', 'EMAIL', 'MEETING', 'FOLLOW_UP', 'NOTE'))
);

CREATE INDEX idx_orders_account_id ON orders (account_id);
CREATE INDEX idx_orders_status ON orders (status);

CREATE INDEX idx_support_tickets_account_id ON support_tickets (account_id);
CREATE INDEX idx_support_tickets_status ON support_tickets (status);

CREATE INDEX idx_interactions_account_id ON interactions (account_id);
CREATE INDEX idx_interactions_occurred_at ON interactions (occurred_at);
