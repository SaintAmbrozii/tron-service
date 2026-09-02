CREATE TABLE IF NOT EXISTS users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       user_id TEXT NOT NULL UNIQUE,
                       name TEXT NOT NULL,
                       last_name TEXT NOT NULL ,
                       email TEXT NOT NULL ,
                       phone TEXT NOT NULL ,
                       create_date TIMESTAMPTZ DEFAULT now()
);