
select a.*,b.mname from (select id,min(gettime) as mintime,max(gettime) as maxtime,avg(cost_time) as cost_time,sum(callnum) 
				FROM interfaceinfo where gettime between '1900-01-01 13:00:00' and '2017-01-01 00:00:01' group by id,gettime div (60 * 60)) a
				 inner join machines b on a.id = b.id