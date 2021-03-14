-- Drop table

-- DROP TABLE public.store_request;

CREATE TABLE public.store_request (
	id uuid NOT NULL,
	discord_user_id text NOT NULL,
	request_message_id text NOT NULL,
	"content" text NOT NULL,
	tags tsvector NOT NULL,
	readable_tags tsvector NOT NULL,
	created_at timestamp(0) NOT NULL DEFAULT now(),
	CONSTRAINT store_request_pk PRIMARY KEY (id),
	CONSTRAINT store_request_un UNIQUE (discord_user_id, request_message_id),
	CONSTRAINT store_request_fk FOREIGN KEY (discord_user_id) REFERENCES discord_user(discord_id)
);
