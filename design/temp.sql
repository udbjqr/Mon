select * from machines;
select * from agent a;
update agent set ipadd = '192.168.10.127';
	 
INSERT INTO machines  (id,collid,agentid,mname,osname,remark,flag,collinterval,colldimension,collconfinfo,ispause,isonline)
     VALUES (5,205,2,'systemid_2','win','send system ',1,60,2,'',1,1);
	 
INSERT INTO machines  (id,collid,agentid,mname,osname,remark,flag,collinterval,colldimension,collconfinfo,ispause,isonline)
     VALUES (6,206,2,'systemid_3','win','three system ',1,60,2,'',1,1);
	
INSERT INTO machines  (id,collid,agentid,mname,osname,remark,flag,collinterval,colldimension,collconfinfo,ispause,isonline)
     VALUES (7,207,2,'systemid_4','win','four system ',1,60,2,'',1,1);
	 
INSERT INTO machines  (id,collid,agentid,mname,osname,remark,flag,collinterval,colldimension,collconfinfo,ispause,isonline)
     VALUES (8,208,2,'systemid_5','win','four system ',1,60,2,'',1,1);
	 
	 
	 
	 
SELECT * FROM t_uigw_sysinvokelog t;

INSERT INTO t_uigw_sysinvokelog(system_id,interface_id,cost_time,log_date) 
	values(205,22222,3,now());
	
INSERT INTO t_uigw_sysinvokelog(system_id,interface_id,cost_time,log_date) 
	values(206,2,3,now());

INSERT INTO t_uigw_sysinvokelog(system_id,interface_id,cost_time,log_date) 
	values(207,42,3,now());

INSERT INTO t_uigw_sysinvokelog(system_id,interface_id,cost_time,log_date) 
	values(208,262,3,now());
	
	
	
SELECT * FROM machines m;
select max(log_date) as gettime,ifnull(avg(cost_time),0) as cost_time,count(*) as callnum 
from t_uigw_sysinvokelog where log_date > '1900-01-01' and log_date < '2016-07-18 10:04:18'  and system_id = 207 ;

select * from t_uigw_sysinvokelog where system_id = 207 ;


select ifnull(min(log_date),'') as logdate from t_uigw_sysinvokelog  t where system_id = 207 and t.log_date > date_add(
(select min(log_date) from t_uigw_sysinvokelog where log_date > '1900-01-01' and system_id = 207 ), interval 60  second);



insert into interfaceinfo (collid, gettime, online, cost_time, callnum, lastcollid)  values(7,'null',1,0.000000,0,0);
update machines set isonline = 1 where id = 7 and isonline <> 1;

select max(log_date) as gettime,ifnull(avg(cost_time),0) as cost_time,count(*) as callnum from t_uigw_sysinvokelog where log_date > '1900-01-01' and log_date < '2016-07-11 18:16:10'  and interface_id = 10 ;



SELECT * FROM interfaceinfo i;

select  m.id,min(m.mname) as mname,sum(i.callnum) as callnum,avg(i.cost_time) as cost_time
 from machines m inner join interfaceinfo i on m.id = i.collid
  where m. colldimension = 2 and i.gettime between "" and ""  
group by m.id order by m.id;

select * from t_uigw_sysinvokelog t
order by id limit 14 offset 14;

