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
                          rub_amount NUMERIC(10,2) ,
                          usd_amount NUMERIC(10,2),
                          status BOOLEAN,
                          create_date TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS outbox(

                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       user_id TEXT NOT NULL,
                       phone TEXT NOT NULL,
                       amount NUMERIC(10,2),
                       aggregate_id UUID,
                       status BOOLEAN,
                       create_date TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS debezium_heartbeat (
                                                  id INTEGER PRIMARY KEY,
                                                  updated_at TIMESTAMP NOT NULL
);



CREATE OR REPLACE FUNCTION public.tmp_create_pub() RETURNS void AS '
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_publication WHERE pubname = ''outbox_publication'') THEN
            CREATE PUBLICATION outbox_publication FOR TABLE public.outbox, public.debezium_heartbeat;
        END IF;
    END;
' LANGUAGE plpgsql;

SELECT public.tmp_create_pub();
DROP FUNCTION public.tmp_create_pub();

