CREATE TABLE IF NOT EXISTS wallets(id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    userid TEXT NOT NULL,
                    address TEXT NOT NULL UNIQUE ,
                    privat_key TEXT NOT NULL UNIQUE ,
                    amount NUMERIC(10,2) ,
                    create_date TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS exchanges(

                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          user_id VARCHAR NOT NULL,
                          user_wallet VARCHAR NOT NULL ,
                          status VARCHAR(50) NOT NULL,
                          retry_count INT NOT NULL DEFAULT 0,
                          tx_id VARCHAR(255),                    --
                          fail_reason TEXT,
                          rub_amount NUMERIC(10,2) ,
                          usd_amount NUMERIC(10,2),
                          create_date TIMESTAMPTZ DEFAULT now(),

                          CONSTRAINT chk_status
                              CHECK (status IN ('CREATED','PROCESSING', 'COMPLETED', 'PENDING_RETRY', 'FAILED'))

);

CREATE INDEX IF NOT EXISTS idx_exchanges_retry_status
    ON exchanges (status)
    WHERE status = 'PENDING_RETRY';

CREATE TABLE IF NOT EXISTS outbox(

                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       user_id TEXT NOT NULL,
                       phone TEXT NOT NULL,
                       amount NUMERIC(10,2),
                       aggregate_id UUID,
                       status BOOLEAN,
                       create_date TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS shedlock (
                          name VARCHAR(64) NOT NULL,
                          lock_until TIMESTAMP NOT NULL,
                          locked_at TIMESTAMP NOT NULL,
                          value VARCHAR(255) NOT NULL,
                          CONSTRAINT pk_shedlock PRIMARY KEY (name)
);



