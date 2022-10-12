package fr.insee.extract_rp_famille.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)

public class ExtractionServiceTest {

//        @Mock private OmerDAO omerDAO;
//        @Mock private OdicDAO odicDAO;
//        @InjectMocks ExtractionServiceCSV extractionServiceCSV;
//
//        final static Date PERIOD_1_DEBUT = Date.valueOf("2022-01-01");
//        final static Date PERIOD_1_FIN = Date.valueOf("2022-01-02");
//        final static RIMEntity RIM1_ODIC =
//            RIMEntity.builder().id(0).
//                bdd_source(BDDSource.ODIC)
//                /*.dateEnvoi(Timestamp.valueOf("2022-01-01 05:05:00"))*/.build();
//
//        final static RIMEntity RIM1_OMER =
//            RIMEntity.builder().id(1).
//                bdd_source(BDDSource.OMER)
//                /*.dateEnvoi(Timestamp.valueOf("2022-01-01 05:05:00"))*/.build();
//
//        final static RIMEntity RIM_999 =
//            RIMEntity.builder().id(999).
//                bdd_source(BDDSource.OMER)
//                /*.dateEnvoi(Timestamp.valueOf("2022-01-01 05:05:00"))*/.build();

/*
        @Test
        void whenExtraireRIM2BDD_andOMERConnexionFails_shouldThrow(){
                when(omerDAO.getAllRIM(PERIOD_1_DEBUT,PERIOD_1_FIN)).thenThrow(new DataAccessResourceFailureException("TEST"));
                assertThrows(DataAccessException.class, () -> extractionService.extraireRIM2BDD(PERIOD_1_DEBUT,PERIOD_1_FIN));
        }
        //TODO:  a refaire marcher

        @Test
        void whenGetBIEntityForRim_andOMERConnexionFails_shouldThrow(){
                List<Long> rimsId = Arrays.asList(999l);
                when(omerDAO.getBiEtLiensForRims(rimsId,null,null)).thenThrow(new DataAccessResourceFailureException("TEST"));
                assertThrows(DataAccessException.class, () -> extractionService.getBIEntityForRim(rimsId));
        }

        @Test
        void whenGetBIEntityForRim_andODICConnexionFails_shouldThrow(){
                List<Long> rimsId = Arrays.asList(999l);
                when(odicDAO.getBiEtLiensForRims(rimsId,null,null)).thenThrow(new DataAccessResourceFailureException("TEST"));
         assertThrows(DataAccessException.class, () -> extractionService.getBIEntityForRim(rimsId));
        }

        @Test
        void whenExtraireRIM2BDD_andODICConnexionFails_shouldThrow(){
                when(odicDAO.getAllRIM(PERIOD_1_DEBUT,PERIOD_1_FIN)).thenThrow(new DataAccessResourceFailureException("TEST"));
                assertThrows(DataAccessException.class, () -> extractionService.extraireRIM2BDD(PERIOD_1_DEBUT,PERIOD_1_FIN));
        }

        @Test
        void whenExtraireRIM2BDD_andNoResult_shouldReturnEmpty(){
                when(omerDAO.getAllRIM(PERIOD_1_DEBUT,PERIOD_1_FIN)).thenReturn(new ArrayList<>());
                when(odicDAO.getAllRIM(PERIOD_1_DEBUT,PERIOD_1_FIN)).thenReturn(new ArrayList<>());
                var rims = extractionService.extraireRIM2BDD(PERIOD_1_DEBUT,PERIOD_1_FIN);
                Assertions.assertTrue(rims.isEmpty());
        }

        @Test
        void whenExtraireRIM2BDD_andHaveResult_shouldReturnResults(){
                when(omerDAO.getAllRIM(PERIOD_1_DEBUT,PERIOD_1_FIN)).thenReturn( Arrays.asList(RIM1_OMER));
                when(odicDAO.getAllRIM(PERIOD_1_DEBUT,PERIOD_1_FIN)).thenReturn(Arrays.asList(RIM1_ODIC));
                var rims = extractionService.extraireRIM2BDD(PERIOD_1_DEBUT,PERIOD_1_FIN);

                //ON a les deux bonnes RIMS
                Assertions.assertEquals(2,rims.size());
                Assertions.assertTrue(rims.values().containsAll(Arrays.asList(RIM1_OMER,RIM1_ODIC)));
                //Mais pas une qui n'est pas censée y être
                Assertions.assertFalse(rims.values().contains(RIM_999));
        }
*/
}