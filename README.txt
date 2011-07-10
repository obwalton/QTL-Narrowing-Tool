QTL Narrowing Tool
Software written by Dave Walton
Concept by Annerose Berndt

This project was developed for the Bev Paigen Lab and Annerose Berndt
at the Jackson Laboratory.  At the Jackson Laboratory the software was
released as an alpha for testing.  Developing is continuing under the 
direction of Annerose Berndt at University Pittsburg.

Any questions about the software can be directed to Annerose  
(anb128 at pitt dot edu) or Dave Walton 
(david dot o dot walton at gmail dot com).


Copyright


Dependencies
------------
Software Libraries
gwt 2.0.2
gxt 2.1.1
commons-math-2.1
mysql-connector-java-5.1.12
commons-net-ftp-2.0
solr 1.4.1 specific libraries:
	dist/apache-solr-solrj-*.jar
	dist/solrj-lib
		commons-codec-1.3.jar
		commons-httpclient-3.1.jar
		commons-io-1.4.jar
		jcl-over-slf4j-1.5.5.jar
		slf4j-api-1.5.5.jar
	lib/slf4j-jdk14-1.5.5.jar
   Solr data being written to the solr/data directory in this project
   use the schema.xml file that is in solr folder with solr installation
 
Data resources
CGD SNP DB
CGD Imputed SNPs
Mouse Genome Database
Lung Strain Survey
Liver Strain Survey
Need to copy gxt images folder to qtl-narrowing-tool/web/resources/images/

