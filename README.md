# Era Api
Sample loading of surveyed unit

## Extract from RP for family survey

### Concept

For a defined list of communes, each comumune beeing taged as 'male' or 'female'
, collect the list of units (Reponses internet ménage) that answered to RP within a period. 
And that have at least one adult of the correct gender.

Then beeing able to collect the detailed data of a survey unit found with previous call.

### Details

The program connects to 2 databases (ODIC and HOMER) of the RP.
The program takes a parametrage.properties file to configure the targeted communes and gender

ex:

`communes.hommes=12345`
`communes.femmes=56789`

Where 12456 is the INSEE Code of the commune.


`iris.femmes=12456-0001, 12456-0002`
`iris.femmes=12456-0003, 12456-0004`

Where 12456 is the INSEE Code of the commune.
and 000x is the iris code



### REST API
### Request

`GET /extraction-survey-unit/survey-units-for-period`
params : 
- startDate
- endDate

### Response

    [
        {
            id	integer($int64)
            internaute  string
            mail        string
            sexe        string
        }
    ]

- id : ID réponse internet ménage
- internaute : internet identifier
- mail        : The mail of the survey unit
- sexe  : The sexe of the Survey unit 1 for male ; 2 for female 

### Request

`GET /extraction-survey-unit/{id}`
params :
- id : the ID of the survey unit (RIM)
- idCampaign : an identifier of the campaign (used by colleman)

### Response

A json with 2 parts : 
Questionnaire and Pilotage
Designed to be sent to colleman

    {
    "questionnaire": {
    "id": <id>,
    "data": {
        "EXTERNAL": {
            "RPNBQUEST": 1,
            "RPTYPEQUEST": "femmes",
            "MailReferent": "toto@gmail.com",
            "RPLISTEPRENOMS": [
            "XXXX"
            ],
            "RPPRENOMENF1": [
            null
            ],
            "RPSEXENF1": [
            null
            ],
            "RPANAISENF1": [
            null
            ],
            "RPPRENOMENF2": [
            null
            ],
            "RPSEXENF2": [
            null
            ],
            "RPANAISENF2": [
            null
            ],
            "RPPRENOMENF3": [
            null
            ],
            "RPSEXENF3": [
            null
            ],
            "RPANAISENF3": [
            null
            ],
            "RPPRENOMENF4": [
            null
            ],
            "RPSEXENF4": [
            null
            ],
            "RPANAISENF4": [
            null
            ],
            "RPPRENOMENF5": [
            null
            ],
            "RPSEXENF5": [
            null
            ],
            "RPANAISENF5": [
            null
            ],
            "RPPRENOMENF6": [
            null
            ],
            "RPSEXENF6": [
            null
            ],
            "RPANAISENF6": [
            null
            ],
            "RPPRENOMENF7": [
            null
            ],
            "RPSEXENF7": [
            null
            ],
            "RPANAISENF7": [
            null
            ],
            "RPPRENOMENF8": [
            null
            ],
            "RPSEXENF8": [
            null
            ],
            "RPANAISENF8": [
            null
            ],
            "RPPRENOMENF9": [
            null
            ],
            "RPSEXENF9": [
            null
            ],
            "RPANAISENF9": [
            null
            ],
            "RPPRENOMENF10": [
            null
            ],
            "RPSEXENF10": [
            null
            ],
            "RPANAISENF10": [
            null
            ],
            "RPPRENOMENF11": [
            null
            ],
            "RPSEXENF11": [
            null
            ],
            "RPANAISENF11": [
            null
            ],
            "RPPRENOMENF12": [
            null
            ],
            "RPSEXENF12": [
            null
            ],
            "RPANAISENF12": [
            null
            ],
            "RPPRENOMCONJ": [
            "YYYY"
            ],
            "RPSEXCONJ": [
            "1"
            ],
            "RPANAISCONJ": [
            "1955"
            ],
            "RPPRENOMPAR1": [
            null
            ],
            "RPSEXPAR1": [
            null
            ],
            "RPANAISPAR1": [
            null
            ],
            "RPPRENOMPAR2": [
            null
            ],
            "RPSEXPAR2": [
            null
            ],
            "RPANAISPAR2": [
            null
            ]
        }
    },
    "questionnaireId": "IDCAMPAIGN",
    "personalization": {},
    "comment": {},
    "stateData": {}
    },
    "pilotage": {
        "address": "ADDRESS",
        "batchNumber": 1,
        "firstname": "",
        "lastname": "",
        "idCampaign": "<IDCAMPAIGN>",
        "idContact": "xxxx",
        "idSu": "<id>"
    }
    }

### Specification
#### Adults

An adult is a person who is 18yo before 1st junary of the current year.
Ex: for the 2023 test adults are persons born before 1st junary 2005

#### Maximums 

maximum 10 persons of the same sex in a SU 
maximum 12 children by person
 
#### Database inconsistencies

We discard links found in table lienindividus where there is no mirror link
ex: Parent -> Child must have a Child->Parent equivalent for the same persons

