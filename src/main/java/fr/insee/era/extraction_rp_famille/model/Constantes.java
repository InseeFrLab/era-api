package fr.insee.era.extraction_rp_famille.model;

import java.time.LocalDate;

public class Constantes {

        public static final Long LIEN_CONJOINT = 1l;
        public static final Long LIEN_PARENT = 2l;
        public static final Long LIEN_ENFANT = 3l;
        public static final int ANNEE_NAISSANCE_MAJEUR = LocalDate.now().getYear()-18;

        public static final int NB_MAX_PERSONNES_ENQUETEES = 10;
        public static final int NB_MAX_ENFANT_PAR_PERSONNE = 12;

        //TODO : A supprimer pour les perfs??
        public enum BI_SEXE {

                BI_SEXE_HOMME(1),
                BI_SEXE_FEMME(2);
                BI_SEXE(int i){ val =i;}
                private final int val;

                private  static final String SEXE_HOMMES = "hommes";
                private static final String SEXE_FEMMES = "femmes";

                @Override public String toString() {
                        return String.valueOf(val);
                }

                public String toFullString() {
                        if(val==1)
                                return SEXE_HOMMES;
                        else
                                return SEXE_FEMMES;
                }

                public static BI_SEXE fromString(String s){
                        return (s.compareTo("1")==0)?BI_SEXE_HOMME:BI_SEXE_FEMME;
                }

        }


}
