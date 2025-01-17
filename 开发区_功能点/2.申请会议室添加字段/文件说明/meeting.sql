--在meeting_room_app表中添加三个字段，如下：
alter table meeting_room_app add (SQRDH VARCHAR2(50));
alter table meeting_room_app add (SFYGWHLDCJ number(2));
alter table meeting_room_app add (HCYQ VARCHAR2(4000));
alter table meeting_room_app add (LDID VARCHAR2(300));
alter table meeting_room_app add (LDNAME VARCHAR2(300));




CREATE TABLE "MEETING_ROOM_APP_HISTORY" (
"ID" NUMBER NOT NULL ,
"MEETINGROOMID" NUMBER NOT NULL ,
"PERID" NUMBER NOT NULL ,
"DEPARTMENTID" NUMBER NOT NULL ,
"STARTDATETIME" DATE NOT NULL ,
"ENDDATETIME" DATE NOT NULL ,
"MEETINGID" NUMBER NULL ,
"DESCRIPTION" VARCHAR2(255 BYTE) NULL ,
"STATUS" NUMBER(4) NOT NULL ,
"APPDATETIME" DATE NULL ,
"AUDITING_ID" NUMBER NULL ,
"TEMPLATE_ID" NUMBER NULL ,
"PERIODICITY_ID" NUMBER NULL ,
"USED_STATUS" NUMBER(4) NULL ,
"TIME_DIFF" NUMBER NULL ,
"ACCOUNT_ID" NUMBER NULL ,
"SQRDH" VARCHAR2(50 BYTE) NULL ,
"SFYGWHLDCJ" NUMBER(2) NULL ,
"HCYQ" VARCHAR2(4000 BYTE) NULL ,
"LDID" VARCHAR2(300 BYTE) NULL ,
"LDNAME" VARCHAR2(300 BYTE) NULL
)
