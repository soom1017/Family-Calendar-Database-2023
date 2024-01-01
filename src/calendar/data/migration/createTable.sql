CREATE TABLE family (
    family_id SERIAL PRIMARY KEY,
    family_code VARCHAR(255) NOT NULL
);

CREATE TABLE caluser (
    user_id SERIAL PRIMARY KEY,
    user_uid Varchar(20) UNIQUE NOT NULL,
    user_name Varchar(20) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    notification_channel_id INT NOT NULL DEFAULT 0,
    family_id INT,
    CONSTRAINT fk_family FOREIGN KEY (family_id) REFERENCES family (family_id)
);

CREATE TABLE event (
    event_id SERIAL PRIMARY KEY,
    event_name Varchar(20) NOT NULL,
    host_id INT,
    description VARCHAR(255),
    start_at TIMESTAMP,
    end_at TIMESTAMP,
    all_day BOOLEAN,
    interval INT NOT NULL DEFAULT 0, -- 0/15/30/45/60
    time_frame INT,
    CONSTRAINT fk_user FOREIGN KEY (host_id) REFERENCES caluser (user_id)
);

CREATE TABLE event_user (
    event_id INT,
    user_id INT,
    PRIMARY KEY (event_id, user_id),
    CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES event (event_id) ON DELETE CASCADE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES caluser (user_id)
);

CREATE TABLE rsvp_form (
    form_id SERIAL PRIMARY KEY,
    sender_id INT,
    event_name Varchar(20) NOT NULL,
    event_start_at TIMESTAMP,
    event_end_at TIMESTAMP,
    expires_at TIMESTAMP,
    CONSTRAINT fk_sender FOREIGN KEY (sender_id) REFERENCES caluser (user_id)
);

CREATE TABLE rsvp (
    form_id INT,
    recipient_id INT,
    status INT DEFAULT 0, -- 0: PENDING, 1: ACCEPTED, 2: DECLINED
    PRIMARY KEY (form_id, recipient_id),
    CONSTRAINT fk_form FOREIGN KEY (form_id) REFERENCES rsvp_form (form_id),
    CONSTRAINT fk_recipient FOREIGN KEY (recipient_id) REFERENCES caluser (user_id)
);