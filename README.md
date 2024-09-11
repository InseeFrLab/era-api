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

`GET /census-extraction-controller/census-respondents-by-period-and-gender`
params : 
- startDate : yyyy-mm-dd
- endDate : yyyy-mm-dd
- gender : MALE/FEMALE

JSON Extraction

### Request

`/census-extraction/census-respondents-by-period-and-gender/csv-download`
params :
- startDate : yyyy-mm-dd
- endDate : yyyy-mm-dd
- gender : MALE/FEMALE

CSV Extraction


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

#### Emails

blank or empty mails are filtered
Any white space in an email adresse is removed (ex aa bb@toto.org becomes aabb@toto.org)