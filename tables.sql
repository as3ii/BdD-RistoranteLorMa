-- *********************************************
-- * SQL MySQL generation
-- *--------------------------------------------
-- * DB-MAIN version: 11.0.2
-- * Generator date: Sep 20 2021
-- * Generation date: Thu Jun 19 20:24:39 2025
-- * LUN file: /home/as3ii/syncthing/UniBO/II anno/BasiDiDati/2024-25/project/schema-concettuale.lun
-- * Schema: APP RISTORANTI/logico
-- *********************************************


-- Database Section
-- ________________

create database APP_RISTORANTI;
use APP_RISTORANTI;


-- Tables Section
-- _____________

create table DETTAGLIO_ORDINI (
     codice_vivanda int not null,
     codice_ordine int not null,
     quantità int not null,
     constraint IDDETTAGLIO_ORDINI primary key (codice_ordine, codice_vivanda));

create table ORDINI (
     nome_attività varchar(64) not null,
     codice int not null AUTO_INCREMENT,
     data_ora datetime not null,
     stato enum('attesa', 'pronto', 'accettato', 'consegnato', 'annullato') not null,
     tariffa_spedizione decimal(5,2) not null,
     username_cliente varchar(32) not null,
     ora_accettazione datetime,
     ora_consegna datetime,
     username_fattorino varchar(32),
     constraint IDORDINI_1_ID primary key (codice),
     constraint IDORDINI unique (nome_attività, data_ora));

create table RECENSIONI (
     nome_attività varchar(64) not null,
     codice int not null AUTO_INCREMENT,
     data datetime not null,
     voto enum('1', '2', '3', '4', '5') not null,
     commento varchar(1024),
     username varchar(32) not null,
     constraint IDRECENSIONI unique (nome_attività, data),
     constraint IDRECENSIONI_1 primary key (codice));

create table RISTORANTI (
     username varchar(32) not null,
     nome_attività varchar(64) not null,
     p_iva char(11) not null,
     ora_apertura time not null,
     ora_chiusura time not null,
     constraint IDRISTORANTI primary key (nome_attività),
     constraint FKUSERNAME_ID unique (username)
     constraint PIVA unique (p_iva));

create table TIPO_VIVANDE (
     nome varchar(32) not null,
     tipologia enum('cibo', 'bevanda') not null,
     constraint IDTIPO_CUCINA primary key (nome));

create table UTENTI (
     nome varchar(32) not null,
     cognome varchar(32) not null,
     username varchar(32) not null,
     password varchar(128) not null,
     telefono varchar(13) not null,
     email varchar(64) not null,
     città varchar(32) not null,
     via varchar(32) not null,
     n_civico varchar(8) not null,
     credito decimal(5,2) null,
     ruolo enum('admin', 'ristorante', 'cliente', 'fattorino') not null,
     constraint IDUTENTI primary key (username));

create table VIVANDE (
     codice int not null AUTO_INCREMENT,
     nome varchar(32) not null,
     nome_attività varchar(32) not null,
     prezzo decimal(5,2) not null,
     tipologia varchar(32) not null,
     constraint IDVIVANDE unique (nome_attività, nome),
     constraint IDVIVANDE_1 primary key (codice));


-- Constraints Section
-- ___________________

alter table DETTAGLIO_ORDINI add constraint FKcodice_ordine
     foreign key (codice_ordine)
     references ORDINI (codice);

alter table DETTAGLIO_ORDINI add constraint FKcodice_vivanda
     foreign key (codice_vivanda)
     references VIVANDE (codice);

alter table ORDINI add constraint IDORDINI_1_CHK
    check(exists(select * from DETTAGLIO_ORDINI
                 where DETTAGLIO_ORDINI.codice_ordine = codice));

alter table ORDINI add constraint CHECK_STATO_ORDINI
    check(
        (stato IN ('attesa', 'pronto') AND ora_accettazione IS NULL AND username_fattorino IS NULL AND ora_consegna IS NULL) OR
        (stato = 'accettato' AND ora_accettazione IS NOT NULL AND username_fattorino IS NOT NULL AND ora_consegna IS NULL) OR
        (stato = 'consegnato' AND ora_accettazione IS NOT NULL AND username_fattorino IS NOT NULL AND ora_consegna IS NOT NULL)
    );

alter table ORDINI add constraint CHECK_SPEDIZIONE_ORDINI
    check(tariffa_spedizione > 0);

alter table DETTAGLIO_ORDINI add constraint CHECK_QUANTITA_DETTAGLIO_ORDINI
    check(quantità > 0);

alter table UTENTI add constraint CHECK_UTENTI
    check(ruolo = 'cliente' AND credito IS NOT NULL);

alter table VIVANDE add constraint CHECK_VIVANDE
    check(prezzo > 0);

alter table ORDINI add constraint FKcomanda
     foreign key (nome_attività)
     references RISTORANTI (nome_attività);

alter table ORDINI add constraint FKordinazione
     foreign key (username_cliente)
     references UTENTI (username);

alter table ORDINI add constraint FKconsegna
     foreign key (username_fattorino)
     references UTENTI (username);

alter table RECENSIONI add constraint FKscrittura
     foreign key (username)
     references UTENTI (username);

alter table RECENSIONI add constraint FKvalutazione
     foreign key (nome_attività)
     references RISTORANTI (nome_attività);

alter table RISTORANTI add constraint FKutente
     foreign key (username)
     references UTENTI (username);

alter table VIVANDE add constraint FKclassificazione
     foreign key (tipologia)
     references TIPO_VIVANDE (nome);

alter table VIVANDE add constraint FKcomposizione
     foreign key (nome_attività)
     references RISTORANTI (nome_attività);


-- Table Population
-- _____________

INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Acqua','bevanda');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Acqua Frizzante','bevanda');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Acqua Naturale','bevanda');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Antipasto','cibo');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Bevanda','bevanda');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Birra','bevanda');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Cocacola','bevanda');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Contorno','cibo');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Dolce','cibo');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Fanta','bevanda');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Primo','cibo');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Secondo','cibo');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Sprite','bevanda');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Vino','bevanda');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Vino Bianco','bevanda');
INSERT INTO tipo_vivande (nome, tipologia) VALUES ('Vino Rosso','bevanda');
