import csv

# For a final version of this automatically pull these from MGI via
# ftp, create our tables and then load our solr db.
# For now though, we'll assume the files have been downloaded.

#  This is the file that contains the QTL id, symbol, name and coordinates
fdQtlIn = open('MRK_Dump2.rpt.txt','r')
qtlReader = csv.reader(fdQtlIn,delimiter='\t')

#  This is the file that contains the cross reference for all markers to
#  Mammalian Phenotype (MP) id's.  We just want the QTLs.
fdQTL2MPIn = open('MGI_PhenoGenoMP.rpt.txt','r')
qtl2mpReader = csv.reader(fdQTL2MPIn,delimiter='\t')

#  This file contains the detail for the MP Terms
fdMPIn = open('VOC_MammalianPhenotype.rpt.txt','r')
mpReader = csv.reader(fdMPIn,delimiter='\t')

#  This will be our version of the file just containing the QTLs and the
#  Columns we care about (including MP Terms).
#  It will include duplicate detail for MP Terms that are associated with 
#  multiple QTL's.
#  It wastes disk, but it will simply things when loading to solr for searching
fdQtlOut = open('QTLs.csv','w')
qtlWriter = csv.writer(fdQtlOut, delimiter=',')

#  Harvest all of our QTLs out of the the qtlReader and write them to a file
qtls = {}
for line in qtlReader:
    if (line[5] == 'QTL'):
        qtl = [line[0],line[4], line[3].strip(), line[1].strip(),line[2].strip()]
        mp_terms = []
        qtl_dict = {}
        qtl_dict['qtl'] = qtl
        qtl_dict['mp'] = mp_terms
        qtls[line[0]] = qtl_dict

# Read the file of MP ID's and terms and store them in a dictionary for later
# use.
mpids = {}
for line in mpReader:
    mpids[line[0]] = [line[0], line[1], line[2].strip()]

# Read the marker to mp cross reference, store the qtl mappings to mp terms
# with mp detail 
for line in qtl2mpReader:
    qtlid = line[5]

    if (qtls.has_key(qtlid)):
	qtl = qtls[qtlid]
        mpid = line[3]
        if (mpids.has_key(mpid)):
            mp_terms = qtl['mp']
            mp_terms.append(mpids[mpid])

#  Now write it out to the file
qtlWriter.writerow(["id","chr","cm","symbol","name","mpid", "shortdesc","longdesc"])
for qtl in qtls.keys():
    qtl_dict = qtls[qtl]
    qtl_detail = qtl_dict['qtl']
    mp_terms = qtl_dict['mp']
    for term in mp_terms:
        qtlWriter.writerow(qtl_detail + term)


