


insert into serie(id,nomcourt,libelle,typeunites) values
                                                      (nextval('seq_serie'),'S1','SerieTEST1','LOGEMENT'),
                                                      (nextval('seq_serie'),'S2','SerieTEST2','INDIVIDU');

insert into operation(id,nomcourt,libelle,datedernieremodification,serie_id)
values
    (nextval('seq_operation'),'O11','OPERATIONTEST1',current_timestamp,1),
    (nextval('seq_operation'),'O12','OPERATIONTEST2',current_timestamp,1),
    (nextval('seq_operation'),'O21','OPERATIONTEST3',current_timestamp,2);


select id from operation o ;

insert into campagne(id,nomcourt,libelle,datedernieremodification,operation_id)
values
    (nextval('seq_campagne'),'C111','CAMPAGNE_TEST_1',current_timestamp,1),
    (nextval('seq_campagne'),'C112','CAMPAGNE_TEST_2',current_timestamp,1),
    (nextval('seq_campagne'),'C211','CAMPAGNE_TEST_3',current_timestamp,3);

insert into unite_echantillon (id,x,y)
values(nextval('seq_unite_echantillon'),10,10),
      (nextval('seq_unite_echantillon'),20,10),
      (nextval('seq_unite_echantillon'),30,10),
      (nextval('seq_unite_echantillon'),30,10);



select * from unite_echantillon;

insert into campagnes_ues (ue_id,campagne_id,datecreation)
values (1,1,now()),
       (101,1,now()),
       (101,2,now()),
       (201,1,now()),
       (201,2,now()),
       (201,3,now()),
       (301,3,now())
;

select * from campagnes_ues;


insert into occupant (id, unite_echantillon_id, identind ,telListes) values
                                                                         (nextval('seq_occupant'),1,10,'[
                                                                           { "source":"SOURCE_INCONNUE" ,
                                                                             "tellist" : [
                                                                               {"favoris":false,"numero":"00000"} ,
                                                                               {"favoris":false,"numero":"00001"}
                                                                             ]
                                                                           }
                                                                         ]'),
                                                                         (nextval('seq_occupant'),101,10,'[
                                                                           { "source":"SOURCE_INCONNUE" ,
                                                                             "tellist" : [
                                                                               {"favoris":false,"numero":"00000"} ,
                                                                               {"favoris":false,"numero":"00001"}
                                                                             ]
                                                                           } ,
                                                                           { "source":"SOURCE_INCONNUE" ,
                                                                             "tellist" : [
                                                                               {"favoris":false,"numero":"00000"} ,
                                                                               {"favoris":false,"numero":"00001"}
                                                                             ]
                                                                           }
                                                                         ]')
;

select id,x,y from unite_echantillon ue ;

select id, unite_echantillon_id, identind ,tellistes  from occupant;


select * from campagnes_ues;

select * from unite_echantillon;

select * from campagne;

select * from serie;

select * from operation o ;
