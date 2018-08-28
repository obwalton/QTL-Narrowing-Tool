QTL Narrowing Tool
------------------
Software written by Dave Walton
Concept by Annerose Berndt
Created: May 2011

This project was originally developed for the Bev Paigen Lab and Annerose Berndt
at the Jackson Laboratory.  At the Lab, the software was released as an alpha 
for testing.  When Annerose moved to a faculty position at the University of
Pittsburg, it was agreed that JAX had no ongoing interest in further
development of the tool, and that Dave could continue working on the 
application on his own time for Annerose.
Development continued under the direction of Annerose Berndt at University 
Pittsburg, for approximately another year or two.

The software was deployed on servers at Pittsburg for Dr. Berndt's use,
but eventually the project was abandoned.  The software is being relocated
from Bitbucket to Github, just so Dave does not lose track of it, but
this is now an inactive project developed under a tech stack that would
be considered dated and obsolete.

Any questions about the software can be directed to Annerose  
(anb128 at pitt dot edu) or Dave Walton 
(david dot o dot walton at gmail dot com).


The software is being made available under an MIT licence which can be
found in the LICENSE.txt file in the root directory.

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
--------------
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
-----------------
Tomcat must be started with:  -Xms500m -Xmx4G

