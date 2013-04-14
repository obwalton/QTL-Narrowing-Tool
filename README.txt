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
commons-fileupload-1.2.1
commons-math-2.1
commons-net-ftp-2.0
gwt 2.4.0
gxt 2.2.5
jersey-1.0.3
mysql-connector-java-5.1.12
solr 1.4.1 specific libraries:
	dist/apache-solr-solrj-*.jar
	dist/solrj-lib
		commons-codec-1.3.jar
		commons-httpclient-3.1.jar
		commons-io-1.4.jar
		jcl-over-slf4j-1.5.5.jar
		slf4j-api-1.5.5.jar
	lib/slf4j-jdk14-1.5.5.jar
            lucene-core-2.9.3.jar
            lucene-analyzer-2.9.3.jar
            lucene-highlighter-2.9.3.jar
            lucene-spellchecker-2.9.3.jar
   Solr data being written to the solr/data directory in this project
   use the schema.xml file that is in solr folder with solr installation
SAM-Picard-1.5.6 

Data resources
CGD SNP DB
Sanger SNPs
UNC's Jeremy Wang Imputed SNPs
NEIHS 4M SNPs
Mouse Genome Database
Lung Strain Survey
Keith Shockley's Liver Strain Survey
John Sundberg's Alopecia areata data sets
Need to copy gxt images folder to qtl-narrowing-tool/web/resources/images/

Tomcat Resources:

Tomcat must be started with:  -Xms500m -Xmx4G

