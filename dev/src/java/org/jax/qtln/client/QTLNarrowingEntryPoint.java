/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jax.qtln.client;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jax.qtln.regions.Gene;
import org.jax.qtln.regions.QTL;
import org.jax.qtln.regions.QTLSet;
import org.jax.qtln.regions.ReturnRegion;
import org.jax.qtln.regions.SNP;

/**
 * QTLNarrowing main client entry point.
 * <p>
 * This class is the primary client for the QTL Narrowing application.
 * The QTL Narrowing application is a web based GWT front-end to an automated
 * workflow that brings together a manual process involving various analysis
 * steps and data from disparate sources.
 * <p>
 * The primary steps of the process include:
 * <ul>
 * <li>Acquisition of a QTL set</li>
 * <li>Finding the smallest common regions within the QTL set</li>
 * <li>Haplotype analysis of the new regions to find polymorphic SNPs</li>
 * <li>Collecting SNP annotations for the CGD SNP database.</li>
 * <li>Gene expression analysis</li>
 * <ul>
 * <li>Find the associated probe-sets from MGI</li>
 * <li>Calculate Mean Intensities for High and Low responding strains & p-value
 * from default or user provided GEX experiment.</li>
 * </ul>
 * <li>Prepare interactive and downloadable results for user to explore.</li>
 * </ul>
 * @author Dave Walton - dow@jax.org
 */
public class QTLNarrowingEntryPoint implements EntryPoint {
    // Constants
    // URL for doing a file upload
    private static final String UPLOAD_ACTION_URL = GWT.getModuleBaseURL()
            + "upload";
    //The message displayed to the user when the server cannot be reached or
    //returns an error.
    private static final String SERVER_ERROR = "An error occurred while "
        + "attempting to contact the server. Please check your network "
        + "connection and try again. ";
    // The ordered list of mouse chromosomes for sorting purposes
    private static final String[] MOUSE_CHROMOSOMES = {"1", "2", "3", "4", "5"
            ,"6", "7", "8", "9", "10", "11", "12", "13", "14", "15"
            , "16", "17", "18", "19", "X", "Y"};
    //  URL for MGI Gene Detail pages
    private static final String  MGI_GENE_URL =
            "http://www.informatics.jax.org/javawi2/servlet/WIFetch?page=markerDetail&id=";

    //  URL for CGD SNP Database SNP Detail pages
    private static final String  CGD_SNP_URL = "http://cgd.jax.org/cgdsnpdb/parseQuery.php?snpid=";

    /**
     * Create a remote service proxy to talk to the server-side analysis service
     */
    private final QTLServiceAsync qtlService = GWT.create(QTLService.class);

    // These are mostly interface variables, I want visible to all submethods
    // of the class.
    private final VerticalPanel masterPanel = new VerticalPanel();
    private final ContentPanel prepPanel = new ContentPanel();

    private String[] strains = new String[0];
    private Map<Integer, String> snpAnnotLookup = new HashMap<Integer, String>();
    private Label mgiLabel = new Label("QTL Template by Phenotype from MGI:");
    private TextBox mgiTextBox = new TextBox();
    private Button mgiButton = new Button("Search MGI");
    private Label uploadLabel = new Label("Upload Custom QTL File:");
    private Button uploadButton = new Button("Upload");
    private final HorizontalPanel uploadPanel = new HorizontalPanel();
    private final ContentPanel qtlPanel = new ContentPanel();

    // Create the popup dialog box for use sending messages
    private MessageBox dialogBox;
    private Listener alertListener;
    private final RadioButton gexRadio0 =
            new RadioButton("gexGroup", "Gene Expression Comparison:");
    private final RadioButton gexRadio1 =
            new RadioButton("gexGroup", "Upload RMA File:");
    private final RadioButton gexRadio2 =
            new RadioButton("gexGroup", "No Gene Expression Comparison");
    private final ListBox gexListBox = new ListBox();
    private Button gexUploadButton = new Button("Define Exp. Desgn");
    private Button narrowButton = new Button("Narrow QTLs");
    private Button clearButton = new Button("Clear");
    private ContentPanel resultsPanel = new ContentPanel();

    private MessageBox processingDialog;
    private Map<String, Map<String, ReturnRegion>> resultMap;
    private boolean doGEX = false;


    /** 
     * Creates a new instance of QTLNarrowingEntryPoint
     */
    public QTLNarrowingEntryPoint() {
    }

    /** 
     * The entry point method, called automatically by loading a module
     * that declares an implementing class as an entry-point
     */
    public void onModuleLoad() {
        //  Master Panel is the outer panel for all our display widgets
        masterPanel.setSpacing(5);

        //  The Prep Panel is the outer panel for the QTL List preperation
        //  section of the interface
        prepPanel.setBodyBorder(true);
        prepPanel.setCollapsible(true);
        prepPanel.setHeading("QTL List Preperation");
        prepPanel.setButtonAlign(HorizontalAlignment.CENTER);
        prepPanel.setWidth(760);
        prepPanel.setLayout(new FitLayout());

        //  I created a vertical panel inside the prep panel because I liked
        //  the ease of dropping in widgets one below the next...
        final VerticalPanel internalPrep = new VerticalPanel();
        //  ... it also allowed me to put a margin around all widgets
        prepPanel.add(internalPrep, new MarginData(5));

        // Create a FormPanel and point it at a service.  This is strictly to
        // support file upload.
        final FormPanel form = new FormPanel();
        form.setAction(UPLOAD_ACTION_URL);

        // Because we're going to add a FileUpload widget, we'll need to set the
        // form to use the POST method, and multipart MIME encoding.
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);

        // Create a panel to hold all of the 'form' widgets.  Again, this is
        // being used to support file upload
        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(5);
        form.setWidget(panel);

        // All widgets have to be given names so they can be submitted via the
        // form. Again this is due to the use of the file upload.
        mgiTextBox.setName("mgi_text");
        // Disable until we get phenotype searching set up
        mgiTextBox.setEnabled(false);
        mgiButton.setEnabled(false);


        HorizontalPanel mgiPanel = new HorizontalPanel();
        mgiPanel.setSpacing(5);
        mgiPanel.add(mgiLabel);
        mgiPanel.add(mgiTextBox);
        mgiPanel.add(mgiButton);
        panel.add(mgiPanel);

        // Create a FileUpload widget.
        final FileUpload upload = new FileUpload();
        upload.setName("uploadQTLFile");
        uploadPanel.setSpacing(5);
        uploadPanel.add(uploadLabel);
        uploadPanel.add(upload);
        uploadPanel.add(uploadButton);
        //  This is used for the File Upload Servlet to be able to identify
        //  type of file uploaded
        Hidden qtlFileType = new Hidden("FileType", "QTLFile");
        uploadPanel.add(qtlFileType);
        panel.add(uploadPanel);

        internalPrep.add(form);


        //  Before we do anything else, get our list of strains.  These strains
        //  Are the strains associated with the CGD Imputed SNP dataset.  They
        //  are the only strains we support at this time.
        qtlService.getStrains(new AsyncCallback<String[]>() {

            public void onFailure(Throwable caught) {
                System.out.println("IN FAIL CASE GET STRAINS");
                // Show the RPC error message to the user
                String textForMessage = caught.getMessage();
                dialogBox = MessageBox.alert("QTL Narrowing",
                        textForMessage,
                        new Listener<MessageBoxEvent>() {

                            public void handleEvent(MessageBoxEvent mbe) {
                                dialogBox.close();
                                strains = new String[0];
                            }
                        });


                dialogBox.show();
            }

            //  There were problems with timing of widget presentation
            //  that resulted in needing to put the remaining creation
            //  of interface widgets inside this "onSuccess".  When I did
            //  not do this, the strains did not get added to the qtl grid
            //  widget, also all other widgets appeared above the qtl grid
            //  widget once I put it in here.  This also results in all the
            //  fields appearing at the same time.
            public void onSuccess(String[] results) {
                System.out.println("IN SUCCESS CASE GET STRAINS");
                strains = results;

                EditorGrid qtlTable = initEditableQTLTable();

                qtlPanel.setBodyBorder(false);
                qtlPanel.setHeading("QTL List");
                qtlPanel.setButtonAlign(HorizontalAlignment.CENTER);
                qtlPanel.setLayout(new FitLayout());
                qtlPanel.setSize(750, 300);
                qtlPanel.add(qtlTable);

                internalPrep.add(qtlPanel);


                //  All the controls below are added as part of this onSuccess
                //  to ensure they appear below the QTL Panel.
                //
                VerticalPanel radioPanel = new VerticalPanel();
                //  Add controls for defining gene expression analysis
                //
                HorizontalPanel defaultGEXPanel = new HorizontalPanel();
                defaultGEXPanel.setSpacing(5);
                defaultGEXPanel.add(gexRadio0);
                gexRadio0.setValue(true);
                gexListBox.addItem("12 Strain Survey in Lung", "lung");
                gexListBox.addItem("12 Strain Survey in Liver", "liver");
                gexListBox.setVisibleItemCount(1);
                defaultGEXPanel.add(gexListBox);
                radioPanel.add(defaultGEXPanel);

                HorizontalPanel customGEXPanel = new HorizontalPanel();
                customGEXPanel.setSpacing(5);
                customGEXPanel.add(gexRadio1);
                //  Disable during ALPHA release
                //  TODO:  Reenable after ALPHA release
                gexRadio1.setEnabled(false);

                // Create a FormPanel for GEX file upload.  This is strictly to
                // support file upload.
                final FormPanel gexForm = new FormPanel();
                gexForm.setAction(UPLOAD_ACTION_URL);

                // Because we're going to add a FileUpload widget, we'll need to set the
                // form to use the POST method, and multipart MIME encoding.
                gexForm.setEncoding(FormPanel.ENCODING_MULTIPART);
                gexForm.setMethod(FormPanel.METHOD_POST);

                // Create a panel to hold all of the form widgets.  Again, this is being
                // used to support file upload
                HorizontalPanel gexPanel = new HorizontalPanel();
                gexPanel.setSpacing(5);
                gexForm.setWidget(gexPanel);

                // Create a FileUpload widget.
                final FileUpload gexUpload = new FileUpload();
                //  Disable during ALPHA release
                //  TODO:  Reenable after ALPHA release
                gexUpload.setEnabled(false);
                gexUpload.setName("uploadGEXFile");
                HorizontalPanel gexUploadPanel = new HorizontalPanel();
                gexUploadPanel.setSpacing(5);
                gexUploadPanel.add(gexUpload);
                gexUploadPanel.add(gexUploadButton);
                //  Disable during ALPHA release
                //  TODO:  Reenable after ALPHA release
                gexUploadButton.setEnabled(false);
                //  This is used for the File Upload Servlet to be able to identify
                //  type of file uploaded
                Hidden gexFileType = new Hidden("FileType", "GEXFile");
                gexUploadPanel.add(gexFileType);
                gexPanel.add(gexUploadPanel);

                customGEXPanel.add(gexForm);
                radioPanel.add(customGEXPanel);

                HorizontalPanel noGEXPanel = new HorizontalPanel();
                noGEXPanel.setSpacing(5);
                noGEXPanel.add(gexRadio2);
                radioPanel.add(noGEXPanel);
                internalPrep.add(radioPanel);


                HorizontalPanel submitPanel = new HorizontalPanel();
                submitPanel.setSpacing(5);
                submitPanel.add(narrowButton);
                submitPanel.add(clearButton);

                internalPrep.add(submitPanel);

                masterPanel.add(prepPanel);


                Grid summaryTable = initResultTable();
                resultsPanel.setBodyBorder(false);
                resultsPanel.setHeading("QTL Narrowing Results");
                resultsPanel.setButtonAlign(HorizontalAlignment.CENTER);
                resultsPanel.setLayout(new FitLayout());
                //resultsPanel.setSize(760, 310);
                //resultsPanel.add(summaryTable, new MarginData(5));
                resultsPanel.add(summaryTable);
                resultsPanel.hide();

                masterPanel.add(resultsPanel);

            }
        });  


        RootPanel.get("formContainer").add(masterPanel);
        GXT.hideLoadingPanel("loading");

        //  Behind the scenes we need to go fetch the SNP Annotation Lookup.
        //  This is not used until after the results table is populated.
        qtlService.getSnpAnnotLookup(new AsyncCallback<Map<Integer,String>>() {

            public void onFailure(Throwable caught) {
                System.out.println("IN FAIL CASE GET ANNOTS");
                // Show the RPC error message to the user
                String textForMessage = caught.getMessage();
                dialogBox = MessageBox.alert("QTL Narrowing",
                        textForMessage,
                        new Listener<MessageBoxEvent>() {

                            public void handleEvent(MessageBoxEvent mbe) {
                                dialogBox.close();
                                strains = new String[0];
                            }
                        });


                dialogBox.show();
            }

            //  There were problems with timing of widget presentation
            //  that resulted in needing to put the remaining creation
            //  of interface widgets inside this "onSuccess".  When I did
            //  not do this, the strains did not get added to the qtl grid
            //  widget, also all other widgets appeared above the qtl grid
            //  widget once I put it in here.  This also results in all the
            //  fields appearing at the same time.
            public void onSuccess(Map<Integer, String> results) {
                System.out.println("IN SUCCESS CASE GET Annots");
                snpAnnotLookup = results;
            }
        });


        // Functionality for the MGI Button
        mgiButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                String text = mgiTextBox.getText().trim();
            }
        });

        // Functionality for the Upload Button
        uploadButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                form.submit();
            }
        });

       alertListener = new Listener<MessageBoxEvent>() {

            public void handleEvent(MessageBoxEvent mbe) {
                dialogBox.close();
                uploadButton.setEnabled(true);
                //  Disable during ALPHA release
                //  TODO:  Reenable after ALPHA release
                gexUploadButton.setEnabled(false);
                //gexUploadButton.setEnabled(true);
                narrowButton.setEnabled(true);
            }
        };


        // Add an event handler for the upload form
        form.addSubmitHandler(new FormPanel.SubmitHandler() {

            public void onSubmit(SubmitEvent event) {
                // This event is fired just before the form is submitted.
                // We can take this opportunity to perform validation.
                validateFields(event);
            }

            /**
             * Make sure a file has been selected for upload.
             */
            private void validateFields(SubmitEvent event) {
                uploadButton.setEnabled(false);

                final String qtlFileName = upload.getFilename();

                if (qtlFileName.equals("")) {
                    String textForMessage = "No QTL file selected for upload.";
                    dialogBox = MessageBox.alert("QTL Narrowing",
                            textForMessage, alertListener);
                    dialogBox.show();
                    event.cancel();
                    return;
                }

            }
        });  // End of Submit Handler
        
        form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {

            public void onSubmitComplete(SubmitCompleteEvent event) {
                // When the form submission is successfully completed, this
                // event is fired. Assuming the service returned a response of
                // type text/html, we can get the result text here (see the
                // FormPanel documentation for further explanation).
                getQTLFile(event.getResults());
            }

            /**
             * upload our qtl file and get the contents from the server, then
             * load to our table.
             */
            private void getQTLFile(String outcome) {

                System.out.println("File qtl stored to on server: " +
                        outcome);


                qtlService.readQTLFile(new AsyncCallback<List<String[]>>() {

                    public void onFailure(Throwable caught) {
                        System.out.println("IN FAIL CASE");
                        // Show the RPC error message to the user
                        String textForMessage = caught.getMessage();
                        dialogBox = MessageBox.alert("QTL Narrowing",
                                textForMessage, alertListener);

                        dialogBox.show();
                    }

                    public void onSuccess(List<String[]> results) {
                        System.out.println("IN SUCCESS CASE");
                        EditorGrid qtlTable = (EditorGrid)qtlPanel.getWidget(0);
                        ListStore<UIQTL> qtlList = 
                                (ListStore<UIQTL>)qtlTable.getStore();
                        for (Iterator<String[]> i = results.iterator();
                        i.hasNext();) {
                            String[] result = (String[]) i.next();
                            //  TODO: Validate the number of columns!!
                            String qtlid = result[0];
                            String phenotype = result[1];
                            String species = result[2];
                            String hrstrain = result[3];
                            String lrstrain = result[4];
                            String chr = result[5];
                            Integer start = new Integer(result[6]);
                            Integer end = new Integer(result[7]);

                            UIQTL qtl = new UIQTL(qtlid, phenotype, species,
                                    hrstrain, lrstrain, chr, start, end);
                            //  TODO:  Add a catch of "Not a number" for start
                            //  and end...
                            qtlList.add(qtl);
                        }
                        uploadButton.setEnabled(true);
                    }
                });
            }
        });

        //  Used to provide feedback on processing of qtl narrowing...
        final AsyncCallback statusCallback = new AsyncCallback<String>() {

            public void onFailure(Throwable caught) {
                // Show the RPC error message to the user
                processingDialog.close();
                String textForMessage ="Unable to get status - Failure" +
                        SERVER_ERROR + "<BR>" + caught.getMessage();
                dialogBox = MessageBox.alert("QTL Narrowing", textForMessage,
                        alertListener);
                dialogBox.show();
            }

            public void onSuccess(String status) {
                ProgressBar pb = processingDialog.getProgressBar();
                pb.updateText(status);
            }
        };

        //  This is the call back for the Narrow QTL service
        final AsyncCallback narrowingCallback = new AsyncCallback<Map<String,
                List<ReturnRegion>>>() {

            public void onFailure(Throwable caught) {
                processingDialog.close();
                //progressDialog.hide();
                System.out.println("IN FAIL CASE");
                // Show the RPC error message to the user
                String textForMessage = caught.getMessage();
                dialogBox = MessageBox.alert("QTL Narrowing", textForMessage,
                        alertListener);
                dialogBox.show();
            }

            public void onSuccess(Map<String, List<ReturnRegion>> results) {
                System.out.println("IN SUCCESS CASE");

                //Grid summaryTable = initResultTable();
                Grid summaryTable = (Grid)resultsPanel.getWidget(0);
                ListStore<QTLResult> summaryList =
                        (ListStore<QTLResult>) summaryTable.getStore();

                resultMap =
                        new HashMap<String, Map<String, ReturnRegion>>();
                Set<String> keys = results.keySet();
                GWT.log("Populate summaryList");
                // I've had problems with null pointer exceptions
                // inside the grid infrastructure.
                // This failure flag is to allow me to break out and
                // display a dialog if something goes wrong...
                boolean failure = false;
                String failText = "";
                int rows = 0;
                for (String chr : keys) {

                    Map chrMap = new HashMap<String, ReturnRegion>();
                    resultMap.put(chr, chrMap);

                    List<ReturnRegion> regions = results.get(chr);
                    for (ReturnRegion region : regions) {

                        String range = (String) region.getRegionKey();
                        chrMap.put(range, region);
                        QTLSet qtlSet = (QTLSet) region.getQtls();
                        List<QTL> qtls = qtlSet.asList();
                        Integer snps = (Integer) region.getNumberSnps();
                        List<Gene> genes = (List<Gene>) region.getGenes();

                        //  TODO: Validate the number of columns!!

                        // As noted above.  Had trouble with null
                        // pointers in adding to the "store" for the
                        // grid.
                        try {
                            QTLResult qtlResult = new QTLResult(chr,
                                    range, qtls.size(),
                                    region.getTotalNumSNPsInRegion(),
                                    snps, genes.size());
                            //  TODO:  Add a catch of "Not a number" for start
                            //  and end...
                            summaryList.add(qtlResult);
                            ++rows;
                        } catch (Throwable t) {
                            failure = true;
                            failText = "There was a problem adding " +
                                    chr + ":" + range + " to results " +
                                    "table: " + t.getMessage();
                            t.printStackTrace();
                            break;
                        }

                    }
                    if (failure) {
                        break;
                    }

                }
                summaryTable.setAutoHeight(true);

                if (failure) {
                    processingDialog.close();
                    System.out.println("Failure processing results!");
                    // Show the RPC error message to the user
                    String textForMessage = failText;
                    dialogBox = MessageBox.alert("QTL Narrowing",
                            textForMessage, alertListener);
                    dialogBox.show();
                    narrowButton.setEnabled(true);
                } else {
                    System.out.println("finished adding rows to summaryList");
                    processingDialog.close();
                    //  If all went well, we'll collapse the prep panel so the
                    //  user's focus is drawn to the results.
                    prepPanel.collapse();
                    resultsPanel.show();
                    //narrowButton.setEnabled(true);
                }
            }
        };

        // Functionality for the Narrow QTL Button
        narrowButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                narrowButton.setEnabled(false);

                //  This try allows us to break out of the process of 
                //  narrowing qtls if one of the strains is invalid
                try {
                    ArrayList<List> qtls = new ArrayList<List>();
                    EditorGrid qtlTable = (EditorGrid) qtlPanel.getWidget(0);
                    ListStore<UIQTL> qtlList = (ListStore<UIQTL>) qtlTable.getStore();

                    for (int i = 0; i < qtlList.getCount(); i++) {
                        ArrayList row = new ArrayList();
                        // Replace this with a method that takes a UIQTL and sends
                        // to back end to be parsed.
                        UIQTL qtl = qtlList.getAt(i);
                        row.add(qtl.getQtlid());
                        row.add(qtl.getPhenotype());
                        row.add(qtl.getSpecies());
                        String hrstrain = qtl.getHrstrain();
                        String lrstrain = qtl.getLrstrain();
                        //  Confirm that we are processing valid strains.
                        if (Arrays.binarySearch(strains, hrstrain) < 0) {
                            throw new InvalidStrainException("You have used " +
                                    "at least 1 strain '" + hrstrain +
                                    "' which is not part of the list of imputed " +
                                    "strains.  Use the drop down list in the " +
                                    "HR and LR Strain columns in the QTL List " +
                                    "to find supported strain names.");
                        }
                        if (Arrays.binarySearch(strains, lrstrain) < 0) {
                            throw new InvalidStrainException("You have used " +
                                    "at least 1 strain '" + lrstrain +
                                    "' which is not part of the list of imputed " +
                                    "strains.  Use the drop down list in the " +
                                    "HR and LR Strain columns in the QTL List " +
                                    "to find supported strain names.");
                        }
                        row.add(hrstrain);
                        row.add(lrstrain);
                        row.add(qtl.getChr());
                        row.add("" + qtl.getQtlstart());
                        row.add("" + qtl.getQtlend());

                        qtls.add((List) row);
                    }

                    Timer timer = new Timer() {

                        public void run() {
                            qtlService.getNarrowingStatus(statusCallback);
                        }
                    };
                    processingDialog = MessageBox.wait("Narrow QTLs",
                            "Please wait while your QTL list is narrowed to a " +
                            "list of Genes", "Processing...");

                    //run this timer to periodically check the status of our
                    //narrowing process...
                    timer.scheduleRepeating(1000);
                    ProgressBar pb = processingDialog.getProgressBar();

                    // Determine whether or not we are doing a Gene Expression
                    // Comparison, and if so, what kind.
                    doGEX = false;
                    if (gexRadio0.getValue() || gexRadio1.getValue()) {
                        doGEX = true;
                    }
                    System.out.println("Calling narrowQTLs");
                    qtlService.narrowQTLs((List<List>) qtls, doGEX,
                            narrowingCallback);

                }
                catch (InvalidStrainException ise) {

                    String textForMessage = ise.getMessage();
                    dialogBox = MessageBox.alert("QTL Narrowing", textForMessage,
                            alertListener);
                    dialogBox.show();
                    narrowButton.setEnabled(false);

                }
            }
        });

        // Functionality for the Clear Button
        clearButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                clear();
            }
        });


    }

    private EditorGrid initEditableQTLTable() {
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        //  First Column is the QTL ID
        ColumnConfig column = new ColumnConfig();
        column.setId("qtlid");
        column.setHeader("QTL ID");
        column.setWidth(100);
        TextField<String> text = new TextField<String>();
        text.setAllowBlank(false);
        text.setAutoValidate(true);
        column.setEditor(new CellEditor(text));
        configs.add(column);
        //  Second Column is the Phenotype
        column = new ColumnConfig();
        column.setId("phenotype");
        column.setHeader("Phenotype");
        column.setWidth(100);
        text = new TextField<String>();
        text.setAllowBlank(false);
        text.setAutoValidate(true);
        column.setEditor(new CellEditor(text));
        configs.add(column);
        //  Third Column is the Species (should be changed to an enum - Mouse...)
        column = new ColumnConfig();
        column.setId("species");
        column.setHeader("Species");
        column.setWidth(100);
        text = new TextField<String>();
        text.setAllowBlank(false);
        text.setAutoValidate(true);
        column.setEditor(new CellEditor(text));
        configs.add(column);

        //  Render for displaying invalid strains as red
        GridCellRenderer<UIQTL> strainRenderer = new GridCellRenderer<UIQTL>() {

            public String render(UIQTL model, String property, ColumnData config,
                    int rowIndex, int colIndex, ListStore<UIQTL> qtlList,
                    Grid<UIQTL> grid) {
                String val = (String) model.get(property);
                String html = "";
                if (Arrays.binarySearch(strains, val) > -1) {
                    html = val;
                }
                else {
                    html = "<span style='color:red'>" + val + "</span>";
                }

                return html;
            }
        };

        //  Forth Column is High Responding Strain, this will be a selectable
        //  combo box
        final SimpleComboBox<String> combo1 = new SimpleComboBox<String>();
        combo1.setTriggerAction(TriggerAction.ALL);
        for (String strain : this.strains) {
            combo1.add(strain);
        }
        CellEditor editor = new CellEditor(combo1) {

            @Override
            public Object preProcessValue(Object value) {
                if (value == null) {
                    return value;
                }
                return combo1.findModel(value.toString());
            }

            @Override
            public Object postProcessValue(Object value) {
                if (value == null) {
                    return value;
                }
                return ((ModelData) value).get("value");
            }
        };

        column = new ColumnConfig("hrstrain", "High Resp Strain", 100);
        column.setAlignment(HorizontalAlignment.LEFT);
        column.setEditor(editor);
        column.setRenderer(strainRenderer);
        configs.add(column);
        //  Fifth Column is Low Responding Strain.  Just like last column.
        final SimpleComboBox<String> combo2 = new SimpleComboBox<String>();
        combo2.setTriggerAction(TriggerAction.ALL);
        for (String strain : this.strains) {
            combo2.add(strain);
        }
        editor = new CellEditor(combo2) {

            @Override
            public Object preProcessValue(Object value) {
                if (value == null) {
                    return value;
                }
                return combo2.findModel(value.toString());
            }

            @Override
            public Object postProcessValue(Object value) {
                if (value == null) {
                    return value;
                }
                return ((ModelData) value).get("value");
            }
        };

        column = new ColumnConfig("lrstrain", "Low Resp Strain", 100);
        column.setAlignment(HorizontalAlignment.LEFT);
        column.setEditor(editor);
        column.setRenderer(strainRenderer);
        configs.add(column);
        //  Sixth Column is Chromosome.  Similar to last two columns.
        final SimpleComboBox<String> combo3 = new SimpleComboBox<String>();
        combo3.setTriggerAction(TriggerAction.ALL);
        for (String chr : QTLNarrowingEntryPoint.MOUSE_CHROMOSOMES) {
            combo3.add(chr);
        }
        editor = new CellEditor(combo3) {

            @Override
            public Object preProcessValue(Object value) {
                if (value == null) {
                    return value;
                }
                return combo3.findModel(value.toString());
            }

            @Override
            public Object postProcessValue(Object value) {
                if (value == null) {
                    return value;
                }
                return ((ModelData) value).get("value");
            }
        };

        column = new ColumnConfig("chr", "Chr", 50);
        column.setAlignment(HorizontalAlignment.LEFT);
        column.setEditor(editor);
        configs.add(column);
        final NumberFormat fmt = NumberFormat.getFormat("#,##0");
        //  Seventh Column is the QTL Start
        column = new ColumnConfig();
        column.setId("qtlstart");
        column.setHeader("QTL Start");
        column.setWidth(100);
        NumberField field = new NumberField();
        field.setFormat(fmt);
        field.setPropertyEditorType(Integer.class);
        field.setAllowBlank(false);
        field.setAutoValidate(true);
        column.setEditor(new CellEditor(field));
        GridCellRenderer<UIQTL> formatPosition = new GridCellRenderer<UIQTL>() {

            public String render(UIQTL model, String property, ColumnData config, 
                    int rowIndex, int colIndex, ListStore<UIQTL> qtlList,
                    Grid<UIQTL> grid) {
                int val = (Integer) model.get(property);

                return fmt.format(val);
            }
        };
        column.setRenderer(formatPosition);
        configs.add(column);
        //  Eighth Column is the QTL End
        column = new ColumnConfig();
        column.setId("qtlend");
        column.setHeader("QTL End");
        column.setWidth(100);
        field = new NumberField();
        field.setFormat(fmt);
        field.setPropertyEditorType(Integer.class);
        field.setAllowBlank(false);
        field.setAutoValidate(true);
        column.setEditor(new CellEditor(field));
        column.setRenderer(formatPosition);
        configs.add(column);

        // Now create our Grid
        ListStore<UIQTL> qtlList = new ListStore<UIQTL>();
        //employeeList.add(TestData.getEmployees());
        ColumnModel cm = new ColumnModel(configs);

        final EditorGrid<UIQTL> grid = new EditorGrid<UIQTL>(qtlList, cm);
        grid.setStyleAttribute("borderTop", "none");
        //grid.setAutoExpandColumn("name");
        grid.setBorders(true);
        grid.setStripeRows(true);

        return grid;
    }

    private Grid initResultTable() {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        //  First Column is the Chromosome
        ColumnConfig column = new ColumnConfig();
        column.setId("chr");
        column.setHeader("Chr");
        column.setWidth(50);
        configs.add(column);

        //  Second Column is the BP Range
        column = new ColumnConfig();
        column.setId("range");
        column.setHeader("BP Range");
        column.setWidth(150);
        configs.add(column);

        //  Set up number formatter for later use.
        final NumberFormat countFmt = NumberFormat.getFormat("#,##0");
        GridCellRenderer<QTLResult> formatInt =
                new GridCellRenderer<QTLResult>() {

            public String render(QTLResult model, String property, 
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<QTLResult> qtlList, Grid<QTLResult> grid) {
                int val = (Integer) model.get(property);

                return countFmt.format(val);
            }
        };

        //  Thirds Column is the Number Overlapping QTLs
        column = new ColumnConfig();
        column.setId("numqtls");
        column.setHeader("# Overlapping QTLs");
        column.setWidth(100);
        //column.setRenderer(formatInt);
        column.setRenderer(new GridCellRenderer<QTLResult>() {

            @Override
            public Object render(QTLResult model, String property,
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<QTLResult> store, Grid<QTLResult> grid) {
                int val = (Integer) model.getNumqtls();
                String fmtVal = countFmt.format(val);
                String chr = model.getChr();
                String range = model.getRange();
                Map<String, ReturnRegion> chrMap = resultMap.get(chr);
                ReturnRegion region = chrMap.get(range);

                QTLSet qtlSet = (QTLSet) region.getQtls();
                List<QTL> qtls = qtlSet.asList();
                String qtlHtml = "<DL>";
                for (QTL qtl : qtls) {
                    qtlHtml += "<DD>" + qtl.getQtlID() + "</DD>";
                }
                qtlHtml += "</DL>";

                String html = "<span qtitle='Contributing QTLs' qtip='" +
                        qtlHtml + "'>" + fmtVal + "</span>";
                return html;
            }
        });
        configs.add(column);

        //  Total Number SNPs
        column = new ColumnConfig();
        column.setId("totalsnps");
        column.setHeader("Total # SNPS in region");
        column.setWidth(125);
        column.setRenderer(formatInt);
        configs.add(column);

        //  Number Selected SNPs
        column = new ColumnConfig();
        column.setId("numsnps");
        column.setHeader("# Selected SNPS");
        column.setWidth(100);
        column.setRenderer(formatInt);
        configs.add(column);

        //  Fifth Column is the Number Genes
        column = new ColumnConfig();
        column.setId("numgenes");
        column.setHeader("# Genes");
        column.setWidth(100);
        //column.setRenderer(formatInt);
        column.setRenderer(new GridCellRenderer<QTLResult>() {

            @Override
            public Object render(QTLResult model, String property,
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<QTLResult> store, Grid<QTLResult> grid) {
                int val = (Integer) model.getNumgenes();
                String fmtVal = countFmt.format(val);
                String html = "<span qtitle='Genes' qtip='Click cell for list of " + 
                        fmtVal + " Genes and Expression info'>" + fmtVal
                        + "</span>";
                return html;
            }
        });
        configs.add(column);

        // Now create our Grid
        ListStore<QTLResult> qtlList = new ListStore<QTLResult>();
        //employeeList.add(TestData.getEmployees());
        ColumnModel cm = new ColumnModel(configs);

        final Grid<QTLResult> grid = new Grid<QTLResult>(qtlList, cm);

        grid.setStyleAttribute("borderTop", "none");
        //grid.setAutoExpandColumn("name");
        grid.setBorders(true);
        grid.setStripeRows(true);
        new QuickTip(grid);

        grid.addListener(Events.CellClick, new Listener<GridEvent>() {

            public void handleEvent(GridEvent e) {
                int col = e.getColIndex();
                int row = e.getRowIndex();
                Grid g = e.getGrid();
                ListStore<QTLResult> listStore = g.getStore();
                QTLResult record = null;
                //  Need to figure out which column maps to which property
                String property = configs.get(e.getColIndex()).getDataIndex();
                if (property.equals("numgenes")) {
                    record = listStore.getAt(row);
                    //Record record = listStore.getRecord(listStore.getAt(row));
                    String chromosome = record.getChr();
                    String range = record.getRange();

                    Map<String, ReturnRegion> chrMap = resultMap.get(chromosome);
                    ReturnRegion region = chrMap.get(range);

                    List<Gene> genes = (List<Gene>) region.getGenes();
                    if (genes.size() != 0) {
                        Grid geneGrid;
                        //  Threw in try block because of problems
                        //  occuring with suspected uncaught exceptions
                        try {
                            geneGrid = initGeneTable(genes, doGEX);
                            GWT.log("Table Done!");

                            // Create a Dialog object
                            Dialog d = new Dialog();
                            d.setHideOnButtonClick(true);
                            d.setButtons(Dialog.CLOSE);
                            d.setBodyBorder(false);
                            d.setHeading("Genes for Chromosome " + chromosome +
                                    " range " + range);
                            d.setButtonAlign(HorizontalAlignment.CENTER);
                            //  For some reason, results were not showing in
                            //  grid with BorderLayout!
                            d.setLayout(new FitLayout());
                            if (doGEX)
                                d.setSize(750, 300);
                            else
                                d.setSize(450,300);
                            d.add(geneGrid);
                            d.show();
                        } catch (Throwable ex) {
                            GWT.log(ex.getMessage(), ex);
                        }
                    }
                }
            }
        });


        return grid;
    }

    private Grid initGeneTable(List<Gene> genes, boolean showGEXResults) {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        final Map<String, Gene> genesByMGI = new HashMap<String, Gene>();
        for (Gene gene: genes) {
            genesByMGI.put(gene.getMgiId(), gene);
        }

        //  first Column is the mgiid
        ColumnConfig column = new ColumnConfig();
        column = new ColumnConfig();
        column.setId("mgiid");
        column.setHeader("MGI ID");
        column.setWidth(100);
        column.setRenderer(new GridCellRenderer<GeneResult>() {

            @Override
            public Object render(GeneResult model, String property,
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<GeneResult> store, Grid<GeneResult> grid) {
                String val = (String) model.getMgiid();

                String html = "<a href='" + QTLNarrowingEntryPoint.MGI_GENE_URL;
                html += val + "' target='_NEW'>" + val + "</a>";
                return html;
            }
        });
        configs.add(column);

        //  second Column is the Gene Symbol
        column = new ColumnConfig();
        column.setId("symbol");
        column.setHeader("Gene Symbol");
        column.setWidth(100);
        configs.add(column);

        //  third Column is the Gene name
        column = new ColumnConfig();
        column.setId("name");
        column.setHeader("Gene Name");
        column.setWidth(150);
        configs.add(column);

        //  Set up some number formatters for later use.
        final NumberFormat countFmt = NumberFormat.getFormat("#,##0");
        final NumberFormat meanFmt = NumberFormat.getFormat("#0.00");
        final NumberFormat pvalFmt = NumberFormat.getFormat("#0.0000");
        GridCellRenderer<GeneResult> formatInt =
                new GridCellRenderer<GeneResult>() {

            public String render(GeneResult model, String property, 
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<GeneResult> qtlList, Grid<GeneResult> grid) {
                int val = (Integer) model.get(property);

                return countFmt.format(val);
            }
        };
        GridCellRenderer<GeneResult> formatMean =
                new GridCellRenderer<GeneResult>() {

            public String render(GeneResult model, String property, 
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<GeneResult> qtlList, Grid<GeneResult> grid) {
                double val = (Double) model.get(property);
                String result = "";
                if (Double.isNaN(val)) {
                    result = "NA";
                }
                else {
                    result = meanFmt.format(val);
                }

                return result;
            }
        };
        GridCellRenderer<GeneResult> formatPVal =
                new GridCellRenderer<GeneResult>() {

            public String render(GeneResult model, String property, 
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<GeneResult> qtlList, Grid<GeneResult> grid) {
                double val = (Double) model.get(property);
                String result = "";
                if (val < 0.0001 && val != 0.0) {
                    result = "< 0.0001";
                }
                else if (Double.isNaN(val)) {
                    result = "NA";
                }
                else {
                    result = pvalFmt.format(val);
                }

                return result;
            }
        };
        column = new ColumnConfig();
        column.setId("numsnps");
        column.setHeader("# SNPs in Gene");
        column.setWidth(100);
        //column.setRenderer(formatInt);
        column.setRenderer(new GridCellRenderer<GeneResult>() {

            @Override
            public Object render(GeneResult model, String property,
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<GeneResult> store, Grid<GeneResult> grid) {
                int val = (Integer) model.getNumsnps();
                String fmtVal = countFmt.format(val);
                GWT.log("IN NumSNPs Renderer Val = "  + fmtVal);

                String html =
                        "<span qtitle='SNPs' qtip='Click cell for list of " + 
                        fmtVal + " SNPs within gene'>" + fmtVal + "</span>";
                return html;
            }
        });
        configs.add(column);

        if (showGEXResults) {
            column = new ColumnConfig();
            column.setId("hrmean");
            column.setHeader("HR Mean Intensity");
            column.setWidth(100);
            column.setRenderer(formatMean);
            configs.add(column);

            column = new ColumnConfig();
            column.setId("lrmean");
            column.setHeader("LR Mean Intensity");
            column.setWidth(100);
            column.setRenderer(formatMean);
            configs.add(column);

            //  Third Column is the High Responding Mean Intensity
            column = new ColumnConfig();
            column.setId("pvalue");
            column.setHeader("P-Value");
            column.setWidth(100);
            column.setRenderer(formatPVal);
            configs.add(column);
        }


        // Now Populate the rows of our store
        ListStore<GeneResult> geneList = new ListStore<GeneResult>();
        for (Gene gene : genes) {
            String mgiId = gene.getMgiId();
            String symbol = "";
            if (gene.getSymbol() != null) {
                symbol = gene.getSymbol();
            }
            String name = "";
            if (gene.getName() != null) {
                name = gene.getName();
            }
            List<SNP> snps = gene.getAssociatedSnps();
            Integer numSnps = new Integer(0);
            if (snps != null) {
                numSnps = new Integer(snps.size());
            }
            Double hrMean = gene.getHighRespondingMeanIntensity();
            Double lrMean = gene.getLowRespondingMeanIntensity();
            Double pValue = gene.getPValue();

            GeneResult geneResult = new GeneResult(mgiId, symbol, name,
                    numSnps.intValue(), hrMean.doubleValue(),
                    lrMean.doubleValue(), pValue.doubleValue());
            //  add to liststore
            geneList.add(geneResult);
        }
        //employeeList.add(TestData.getEmployees());
        ColumnModel cm = new ColumnModel(configs);

        // Now create our Grid
        //final Grid<GeneResult> grid = new Grid<GeneResult>(geneList, cm);
        Grid<GeneResult> grid = new Grid<GeneResult>(geneList, cm);

        grid.setStyleAttribute("borderTop", "none");
        //grid.setAutoExpandColumn("name");
        grid.setBorders(true);
        grid.setStripeRows(true);

        grid.addListener(Events.CellClick, new Listener<GridEvent>() {

            public void handleEvent(GridEvent e) {
                //  TODO: We're relying on the row mapping to the right row in the
                //  Genes list we were passed... We may need to turn off sorting
                //  In our Gene Grid!!!
                int row = e.getRowIndex();
                Grid g = e.getGrid();
                //  Need to figure out which column maps to which property
                String property = configs.get(e.getColIndex()).getDataIndex();
                if (property.equals("numsnps")) {

                    ListStore<GeneResult> listStore = g.getStore();
                    GeneResult record = listStore.getAt(row);
                    String mgiId = record.getMgiid();
                    Gene gene = genesByMGI.get(mgiId);
                    List<SNP> snps = gene.getAssociatedSnps();

                    if (snps.size() != 0) {

                        Grid snpGrid;
                        //  Threw in try block because of problems
                        //  occuring with suspected uncaught exceptions
                        try {
                            snpGrid = initSNPTable(snps);
                            GWT.log("Table Done!");

                            // Create a Dialog object
                            Dialog d = new Dialog();
                            d.setHideOnButtonClick(true);
                            d.setButtons(Dialog.CLOSE);
                            d.setBodyBorder(false);
                            d.setHeading("SNPs within Gene " + gene.getMgiId());
                            d.setButtonAlign(HorizontalAlignment.CENTER);
                            //  For some reason, results were not showing in
                            //  grid with BorderLayout!
                            d.setLayout(new FitLayout());
                            d.setSize(550, 300);
                            d.add(snpGrid);
                            d.show();
                        } catch (Throwable ex) {
                            GWT.log(ex.getMessage(), ex);
                        }
                    }
                }
            }
        });

        return grid;
    }

    private Grid initSNPTable(List<SNP> snps) {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        //  first Column is the SNP ID
        ColumnConfig column = new ColumnConfig();
        //  number formatters for later use.
        final NumberFormat intFmt = NumberFormat.getFormat("#,##0");
        GridCellRenderer<SnpResult> formatInt = new GridCellRenderer<SnpResult>() {

            public String render(SnpResult model, String property,
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<SnpResult> qtlList, Grid<SnpResult> grid) {
                int val = (Integer) model.get(property);

                return intFmt.format(val);
            }
        };

        column = new ColumnConfig();
        column.setId("position");
        column.setHeader("BP Position");
        column.setWidth(100);
        column.setRenderer(formatInt);
        column.setRenderer(new GridCellRenderer<SnpResult>() {

            @Override
            public Object render(SnpResult model, String property,
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<SnpResult> store, Grid<SnpResult> grid) {
                int ival = (Integer) model.get(property);
                String val = intFmt.format(ival);
                String id = ((Integer) model.getCgdsnpid()).toString();

                String html = "<a href='" + QTLNarrowingEntryPoint.CGD_SNP_URL;
                html += id + "' target='_NEW'>" + val + "</a>";
                return html;
            }
        });
        configs.add(column);

        column = new ColumnConfig();
        column.setId("hrbase");
        column.setHeader("HR Base");
        column.setWidth(50);
        configs.add(column);

        column = new ColumnConfig();
        column.setId("lrbase");
        column.setHeader("LR Base");
        column.setWidth(50);
        configs.add(column);

        column = new ColumnConfig();
        column.setId("snpid");
        column.setHeader("SNP ID");
        column.setWidth(100);
        configs.add(column);

        column = new ColumnConfig();
        column.setId("idsource");
        column.setHeader("SNP ID Source");
        column.setWidth(100);
        configs.add(column);

        column = new ColumnConfig();
        column.setId("annotation");
        column.setHeader("Annotation");
        column.setWidth(150);
        configs.add(column);

        // Now Populate the rows of our store
        ListStore<SnpResult> snpList = new ListStore<SnpResult>();
        for (SNP snp : snps) {
            String snpId = snp.getSnpId();
            String idSource = snp.getSource();
            Integer cgdSnpId = snp.getCgdSnpId();
            Integer position = snp.getBPPosition();
            Character hrBase = snp.getHighRespondingBaseValue();
            Character lrBase = snp.getLowRespondingBaseValue();
            
            List<Integer> annotations = snp.getSnpAnnotations();
            if (annotations != null) {
                for (Integer i:annotations) {
                    String annotation = snpAnnotLookup.get(i);

                    SnpResult snpResult = new SnpResult(snpId, idSource,
                            cgdSnpId, position.intValue(), annotation,
                            hrBase, lrBase);
                    //  add to liststore
                    snpList.add(snpResult);
                }
            }
            else {

                SnpResult snpResult = new SnpResult(snpId, idSource, 
                        cgdSnpId, position.intValue(), "", hrBase, lrBase);
                //  add to liststore
                snpList.add(snpResult);
            }

        }
        //employeeList.add(TestData.getEmployees());
        ColumnModel cm = new ColumnModel(configs);

        // Now create our Grid
        //final Grid<GeneResult> grid = new Grid<GeneResult>(geneList, cm);
        Grid<SnpResult> grid = new Grid<SnpResult>(snpList, cm);

        grid.setStyleAttribute("borderTop", "none");
        grid.setAutoExpandColumn("annotation");
        grid.setBorders(true);
        grid.setStripeRows(true);

        return grid;
    }

    private void clear() {
        this.mgiTextBox.setText("");
        //  TODO: Turn off until mgi phenotype searching fixed.
        //this.mgiButton.setEnabled(true);
        this.uploadButton.setEnabled(true);
        FileUpload upload = (FileUpload) this.uploadPanel.getWidget(1);
        this.uploadPanel.remove(upload);
        upload = new FileUpload();
        upload.setName("uploadQTLFile");
        this.uploadPanel.insert(upload, 1);

        EditorGrid qtlTable = (EditorGrid) qtlPanel.getWidget(0);
        qtlPanel.remove(qtlTable);
        qtlTable = initEditableQTLTable();
        qtlPanel.add(qtlTable);
        gexRadio0.setValue(true);
        gexRadio1.setValue(false);
        gexRadio2.setValue(false);
        gexUploadButton.setEnabled(false);  // should be false when radio false
        narrowButton.setEnabled(true);
        clearButton.setEnabled(true);
        resultsPanel.hide();
        resultsPanel.removeAll();

    }
}

class UIQTL extends BaseModel {
    public UIQTL() {
    }
    public UIQTL(String qtlid, String phenotype, String species, String hrStrain,
            String lrStrain, String chr, int start, int end) {
        set("qtlid",qtlid);
        set("phenotype",phenotype);
        set("species",species);
        set("hrstrain",hrStrain);
        set("lrstrain",lrStrain);
        set("chr",chr);
        set("qtlstart",start);
        set("qtlend",end);
    }

    public String getQtlid() {
        return (String)get("qtlid");
    }
    public String getPhenotype() {
        return (String)get("phenotype");
    }
    public String getSpecies() {
        return (String)get("species");
    }
    public String getHrstrain() {
        return (String)get("hrstrain");
    }
    public String getLrstrain() {
        return (String)get("lrstrain");
    }
    public String getChr() {
        return (String)get("chr");
    }
    public int getQtlstart() {
        Integer qtlstart = (Integer)get("qtlstart");
        return qtlstart.intValue();
    }
    public int getQtlend() {
        Integer qtlend = (Integer)get("qtlend");
        return qtlend.intValue();
    }
}

class QTLResult extends BaseModel {
    public QTLResult() {
    }
    public QTLResult(String chr, String range, int numQtls, int totalSnps,
            int numSnps, int numGenes) {
        set("chr",chr);
        set("range",range);
        set("numqtls",numQtls);
        set("totalsnps", totalSnps);
        set("numsnps",numSnps);
        set("numgenes",numGenes);
    }

    public String getChr() {
        return (String)get("chr");
    }
    public String getRange() {
        return (String)get("range");
    }
    public int getNumqtls() {
        Integer numqtls = (Integer)get("numqtls");
        return numqtls.intValue();
    }
    public int getTotalsnps() {
        Integer totalsnps = (Integer)get("totalsnps");
        return totalsnps.intValue();
    }
    public int getNumsnps() {
        Integer numsnps = (Integer)get("numsnps");
        return numsnps.intValue();
    }
    public int getNumgenes() {
        Integer numgenes = (Integer)get("numgenes");
        return numgenes.intValue();
    }
}

class GeneResult extends BaseModel {
    public GeneResult() {
    }
    public GeneResult(String mgiid, String symbol, String name, int numSnps,
            double hrmean, double lrmean, double pvalue) {
        set("symbol",symbol);
        set("mgiid",mgiid);
        set("name", name);
        set("numsnps", numSnps);
        set("hrmean",hrmean);
        set("lrmean",lrmean);
        set("pvalue",pvalue);
    }

    public String getSymbol() {
        return (String)get("symbol");
    }
    public String getMgiid() {
        return (String)get("mgiid");
    }
    public String getName() {
        return (String)get("name");
    }
    public int getNumsnps() {
        Integer numsnps = (Integer)get("numsnps");
        return numsnps.intValue();
    }
    public double getHrmean() {
        Double hrmean = (Double)get("hrmean");
        return hrmean.doubleValue();
    }
    public double getLrmean() {
        Double lrmean = (Double)get("lrmean");
        return lrmean.doubleValue();
    }
    public double getPvalue() {
        Double pvalue = (Double)get("pvalue");
        return pvalue.doubleValue();
    }
}

class SnpResult extends BaseModel {
    public SnpResult() {
    }
    public SnpResult(String snpid, String idsource, int cgdSnpId, int position,
            String annotation, char HRBase, char LRBase) {
        set("position", position);
        set("cgdsnpid", cgdSnpId);
        set("hrbase", HRBase);
        set("lrbase", LRBase);
        set("snpid",snpid);
        set("idsource", idsource);
        set("annotation", annotation);
    }

    public int getPosition() {
        Integer position = (Integer)get("position");
        return position.intValue();
    }
    public int getCgdsnpid() {
        Integer cgdsnpid = (Integer)get("cgdsnpid");
        return cgdsnpid.intValue();
    }
    public char getHrbase() {
        return (Character)get("hrbase");
    }
    public char getLrbase() {
        return (Character)get("Lrbase");
    }
    public String getSnpid() {
        return (String)get("snpid");
    }
    public String getIdsource() {
        return (String)get("idsource");
    }
    public String getAnnotation() {
        return (String)get("annotation");
    }
}