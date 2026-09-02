CREATE TABLE IF NOT EXISTS outbox(

                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     user_id TEXT NOT NULL,
                                     phone TEXT NOT NULL,
                                     amount NUMERIC(10,2),
                                     aggregate_id UUID,
                                     status BOOLEAN,
                                     create_date TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE public.outbox REPLICA IDENTITY FULL;


CREATE TABLE IF NOT EXISTS debezium_heartbeat (
                                                  id INTEGER PRIMARY KEY,
                                                  updated_at TIMESTAMP NOT NULL
);


INSERT INTO public.debezium_heartbeat (id, updated_at)
VALUES (1, NOW())
ON CONFLICT (id) DO NOTHING;

CREATE OR REPLACE FUNCTION public.tmp_create_pub() RETURNS void AS '
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_publication WHERE pubname = ''outbox_publication'') THEN
            CREATE PUBLICATION outbox_publication FOR TABLE public.outbox, public.debezium_heartbeat;
        END IF;
    END;
' LANGUAGE plpgsql;

SELECT public.tmp_create_pub();
DROP FUNCTION public.tmp_create_pub();

