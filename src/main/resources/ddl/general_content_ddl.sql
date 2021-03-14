-- DROP TABLE public.general_content;

CREATE TABLE public.general_content (
	id uuid NOT NULL,
	discord_user_id text NOT NULL,
	"content" text NOT NULL,
	created_at timestamp(0) NOT NULL DEFAULT now(),
	updated_at timestamp(0) NOT NULL DEFAULT now(),
	tags tsvector NOT NULL,
	readable_tags tsvector NOT NULL,
	CONSTRAINT general_content_pk PRIMARY KEY (id),
	CONSTRAINT general_content_un UNIQUE (id),
	CONSTRAINT general_content_fk FOREIGN KEY (discord_user_id) REFERENCES discord_user(discord_id)
);
CREATE INDEX general_content_tags_gin_idx ON public.general_content USING gin (tags);