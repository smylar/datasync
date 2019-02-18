
create or replace function test.log_function()
    returns trigger
    as $function$
    declare
    	tname text;
    	log_query text;
    	id_column text;
    	id_clause text;
    begin
	    select '' into id_clause;
	    foreach id_column IN ARRAY TG_ARGV LOOP
        	select id_clause || format('and %s=$1.%s ', id_column, id_column) into id_clause;
    	end loop;
   
	    select format('%s.%s_log', TG_TABLE_SCHEMA, TG_TABLE_NAME) into tname;
	   	select format('insert into %s select clock_timestamp(), session_user, ''%s'', (select count(*) from %s where 1=1 %s), false, $1.*', tname, TG_OP, tname, id_clause) into log_query;
        
           			 
		if (TG_OP = 'INSERT' or TG_OP = 'UPDATE') then
           execute log_query using new;
           return new;
     	elsif (TG_OP = 'DELETE') then
           execute log_query using old;
           return old;
     	end if;
     
     	return null;
	end;
	$function$ language plpgsql;

    create table test.accounts_log(
    	change_time timestamp not null,
    	change_user varchar(30) not null,
	    change_action varchar(7) not null,
	    change_version int not null,
	    change_synced boolean not null,
	    account varchar(25) not null,
		description varchar(255),
		accountgroup varchar(255),
	    primary key (account, change_version)
    );
   
   create trigger log_accounts_trigger
    after insert or update or delete on test.accounts
    for each row
    execute procedure test.log_function('account');
    