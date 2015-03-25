# Introduction #

There are many researches that require an appropriate Resource Description Framework (RDF) dataset for their experiments. However, we still lack such a good dataset because:
  1. Some existing datasets do not contain large numbers of triplets, resources, or predicates, and so the scalability of real world data (e.g. Wikipedia) cannot be reflected.
  1. Some datasets are static (which is not suitable for testing database update approaches)
  1. Some datasets require specific domain knowledge

Wikipedia is a good data source because of its large scale and is always changing as it is collaboratively contributed by authors. Moreover, most of its contents can be understood by general readers. Wikipedia is frequently backed up into data dumps in XML format available for download. In our project we try to utilize the structured infobox data by converting them into RDF triplets.

We aim at providing an extraction toolkit which enables users to generate Wikipedia infobox RDF data from the Wikipedia XML data dumps.

# Brief Description of the Toolkit #
This toolkit extracts the infobox RDF data from the Wikipedia data dumps in XML format, and the extracted data are stored in a user-specified PostgreSQL database. It consists of an infobox data extraction tool, a data cleansing tool, and the RDF conversion tool, and they are used in the following three different phases.

## Infobox Data Extraction ##
Besides the infobox contents, the Wikipedia data dump also contains other data such as page paragraphs. In order to speed up the performance for subsequent cleansing and extraction processes, it is better to extract essential contents only into an intermediate XML file.

## Data Cleansing ##
This process removes the WikiMedia markups, comments and HTML tags from the infobox data. It also performs infobox redirection, which replaces infobox alias names by their canonical names, and so semantically identical infoboxes can be grouped together.

## RDF Conversion ##
The cleansed infobox data are transformed into an RDF dataset according to the following logic:
  * The subject is the Wikipedia page name (e.g. Tsing\_Ma\_Bridge)
  * The predicate is the concatenation of the infobox name, the "#" symbol and the infobox property name (e.g. Bridge#bridge\_name)
  * The object is extracted from the property value, and a property value may produce one or more objects, thus several RDF triplets may be generated

**It is recommended to refer to the technical report for more technical details of the above processes.**

# Usage Guide #

## Pre-requisites ##
  1. JRE 6 or higher
  1. PostgreSQL server and its JDBC driver
  1. libxml-parser-perl
  1. libparse-mediawikidump

**For Ubuntu OS the Perl libraries (third and fourth components) can be obtained easily through apt-get or synaptic package manager.**

## Procedures ##
  1. Visit the data dump download site http://dumps.wikimedia.org/enwiki/ (English Wikipedia, you may select other languages as well), select the directory according to the date of the dump you want. Then, select the page articles dump of which the file name is in the format of "enwiki-yyyymmdd-pages-articles.xml.bz2" where yyyymmdd is the date of the dump
  1. Checkout the toolkit and build the necessary JAR file by Ant using the build.xml file
  1. Infobox data extraction - in the bin directory, execute the Perl program "infoboxAndTemplateExtractor.pl" by running the command "bzcat" on the dump file and pipelining the output to the program:<br />```
bzcat <data dump file path> | ./infoboxAndTemplateExtractor.pl <infobox data XML file path> <template XML file path>```<br />The infobox data XML file path and the template XML file path (for infobox redirection) must also be specified as input arguments to the program
  1. Data cleansing - in the bin directory, execute the shell script "cleanse\_infobox.sh":<br />```
./cleanse_infobox.sh <infobox data XML file path> <template XML file path> <cleansed data XML file path>```<br />where the infobox data XML file and the template XML file are obtained from the previous step
  1. RDF conversion - in the bin directory, execute the shell script "import\_infobox.sh":<br />```
./import_infobox.sh <postgreSQL JDBC driver file path> <cleansed data XML file path> <postgreSQL database name> <database user name> <database password> [batch size]```<br />the batch size is an optional parameter which specifies the number of RDF triplet insertion executed in a batch, the default value is 500,000

