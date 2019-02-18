DROP SCHEMA IF EXISTS test;
CREATE SCHEMA test;

CREATE TABLE test.accounts (
	account varchar(25) NOT NULL,
	description varchar(255) NOT NULL,
	accountgroup varchar(255) NULL
);

CREATE TABLE test.accounts_log (
	change_time timestamp NOT NULL,
	change_user varchar(30) NOT NULL,
	change_action varchar(7) NOT NULL,
	change_version int NOT NULL,
	change_synced int NOT NULL,
	account varchar(25) NOT NULL,
	description varchar(255) NULL,
	accountgroup varchar(255) NULL,
	CONSTRAINT accounts_log_pkey PRIMARY KEY (account, change_version)
);

CREATE TABLE test.analysis_codes (
	analysis_code varchar(25) NOT NULL,
	description varchar(255) NOT NULL
);

CREATE TABLE test.analysis_codes_log (
	change_time timestamp NOT NULL,
	change_user varchar(30) NOT NULL,
	change_action varchar(7) NOT NULL,
	change_version int NOT NULL,
	change_synced int NOT NULL,
	analysis_code varchar(25) NOT NULL,
	description varchar(255) NULL,
	CONSTRAINT analysis_codes_log_pkey PRIMARY KEY (analysis_code, change_version)
);

INSERT INTO test.accounts
(account, description, accountgroup)
VALUES('2', 'testing', 'testing');

INSERT INTO test.accounts_log
(change_time, change_user, change_action, change_version, change_synced, account, description, accountgroup)
VALUES(current_timestamp, 'testuser', 'INSERT', 0, 0, '2', 'testing', 'testing');

INSERT INTO test.analysis_codes
(analysis_code, description)
VALUES('a', 'test - updated oracle');

INSERT INTO test.analysis_codes_log
(change_time, change_user, change_action, change_version, change_synced, analysis_code, description)
VALUES(sysdate, 'testuser', 'UPDATE', 1, 0, 'a', 'test - updated oracle');

