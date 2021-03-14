-- Drop table

-- DROP TABLE public.discord_user;

CREATE TABLE public.discord_user (
	discord_id text NOT NULL,
	username text NOT NULL,
	"admin" bool NOT NULL DEFAULT false,
	karma_value int8 NOT NULL DEFAULT 0,
	karma_date timestamp(0) NOT NULL DEFAULT now(),
	created_at timestamp(0) NOT NULL DEFAULT now(),
	updated_at timestamp(0) NOT NULL DEFAULT now(),
	CONSTRAINT discord_user_pk PRIMARY KEY (discord_id),
	CONSTRAINT discord_user_un UNIQUE (discord_id)
);
