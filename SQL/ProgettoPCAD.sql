create schema PCAD;
set search_path to PCAD;

set datestyle to "DMY";

CREATE TABLE CLIENT(
ClientName varchar(30) PRIMARY KEY NOT NULL,
Password varchar(30) NOT NULL
);

CREATE TABLE ROOM (
RoomName varchar(30) PRIMARY KEY NOT NULL,
RoomDescription varchar(500) NOT NULL,
RoomVote integer CHECK (RoomVote between 0 and 10),
RoomTime integer CHECK (roomTime>=0)
);

CREATE TABLE LINKAGE(
RoomStart varchar(30) NOT NULL REFERENCES ROOM (RoomName) ON UPDATE CASCADE ON DELETE CASCADE,
RoomEnd varchar(30) NOT NULL REFERENCES ROOM (RoomName) ON UPDATE CASCADE ON DELETE CASCADE,

PRIMARY KEY (RoomStart, RoomEnd)
);

/*CLIENT*/
INSERT INTO CLIENT VALUES ('User1', 'Pass1');
INSERT INTO CLIENT VALUES ('User2', 'Pass2');

/*ROOM*/
INSERT INTO ROOM VALUES ('Ingresso', 'Informazioni acquario', '5', 0);
INSERT INTO ROOM VALUES ('Delfini', 'Informazioni Delfini ', '5', 0);
INSERT INTO ROOM VALUES ('Squali', 'Informazioni Squali', '5', 0);
INSERT INTO ROOM VALUES ('Foche', 'Informazioni Foche', '5', 0);
INSERT INTO ROOM VALUES ('Granchi', 'Informazioni Granchi', '5', 0);
INSERT INTO ROOM VALUES ('Orche', 'Informazioni Orche', '5', 0);
INSERT INTO ROOM VALUES ('Pinguini', 'Informazioni Pinguini', '5', 0);
INSERT INTO ROOM VALUES ('Piranha', 'Informazioni Piranha', '5', 0);
INSERT INTO ROOM VALUES ('Polpi', 'Informazioni Polpo', '5', 0);
INSERT INTO ROOM VALUES ('Uscita', 'Tornate a trovarci', '5', 0);

/*LINKAGE*/
INSERT INTO LINKAGE VALUES ('Ingresso', 'Delfini');
INSERT INTO LINKAGE VALUES ('Ingresso', 'Orche');
INSERT INTO LINKAGE VALUES ('Delfini', 'Granchi');
INSERT INTO LINKAGE VALUES ('Granchi', 'Delfini');
INSERT INTO LINKAGE VALUES ('Delfini', 'Squali');
INSERT INTO LINKAGE VALUES ('Squali', 'Delfini');
INSERT INTO LINKAGE VALUES ('Squali', 'Foche');
INSERT INTO LINKAGE VALUES ('Foche', 'Squali');
INSERT INTO LINKAGE VALUES ('Orche', 'Pinguini');
INSERT INTO LINKAGE VALUES ('Pinguini', 'Orche');
INSERT INTO LINKAGE VALUES ('Orche', 'Granchi');
INSERT INTO LINKAGE VALUES ('Granchi', 'Orche');
INSERT INTO LINKAGE VALUES ('Granchi', 'Piranha');
INSERT INTO LINKAGE VALUES ('Piranha', 'Granchi');
INSERT INTO LINKAGE VALUES ('Granchi', 'Foche');
INSERT INTO LINKAGE VALUES ('Foche', 'Granchi');
INSERT INTO LINKAGE VALUES ('Piranha', 'Uscita');
INSERT INTO LINKAGE VALUES ('Piranha', 'Polpi');
INSERT INTO LINKAGE VALUES ('Polpi', 'Piranha');
INSERT INTO LINKAGE VALUES ('Pinguini', 'Piranha');
INSERT INTO LINKAGE VALUES ('Piranha', 'Pinguini');
INSERT INTO LINKAGE VALUES ('Polpi', 'Foche');
INSERT INTO LINKAGE VALUES ('Foche', 'Polpi');
