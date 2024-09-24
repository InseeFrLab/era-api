package fr.insee.era.extraction_rp_famille.model;

import java.time.LocalDate;

public class Constantes {

        public static final Long LIEN_CONJOINT = 1L;
        public static final Long LIEN_PARENT = 2L;
        public static final Long LIEN_ENFANT = 3L;
        //TODO : En fait le métier voudrait aussi garder les jeunes nés le 1er janvier de l'année suivante
        //ex: pour l'enquête 2023 ils voudraient tous les gens nés jusqu'au 1er janvier 2005 inclus
        public static final int ANNEE_NAISSANCE_MAJEUR = LocalDate.now().getYear()-19;

        public static final int NB_MAX_PERSONNES_ENQUETEES = 10;
        public static final int NB_MAX_ENFANT_PAR_PERSONNE = 8;

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
