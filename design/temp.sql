insert into machines (id, mname, osname, remark, flag, agentid, collinterval, ispause) 
values(1,"this","windows","12345678",0,0,10,1);

select * from machinesinfo m;

select * from machines;


insert into machinesinfo (id, gettime, online, totalmem, freemem, maxmem, totalphymem, freephymem, usedphymem, totalthread, cpuratio, totaldisk, 
useddisk,freedisk)  values(1,'2016-07-05 16:43:13.072',1,61,49,892,4013,1117,4013,8,28.0,114470,91556,91556);


select mi.id,mi.cpuratio,mi.freedisk,mi.useddisk,mi.freemem,mi.freephymem,mi.maxmem,ifnull(mi.online,0) as online,mi.gettime,mi.totaldisk,mi.totalmem,mi.totalphymem,mi.totalthread,mi.usedphymem,m.mname,m.osname from machines m left join (select * from machinesinfo mi where not exists (select 1 from machinesinfo  where mi.id = id and  mi.gettime <gettime)) mi on m.id = mi.id and m.flag = 0;
