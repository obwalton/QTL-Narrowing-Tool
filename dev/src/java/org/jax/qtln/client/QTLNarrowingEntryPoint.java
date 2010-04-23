/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jax.qtln.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.client.ProgressBar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jax.qtln.regions.Region;
import org.jax.qtln.regions.SNP;

/**
 * Main entry point.
 *
 * @author dow
 */
public class QTLNarrowingEntryPoint implements EntryPoint {
    private static final String UPLOAD_ACTION_URL = GWT.getModuleBaseURL()
            + "upload";
    //The message displayed to the user when the server cannot be reached or
    //returns an error.
    private static final String SERVER_ERROR = "An error occurred while "
        + "attempting to contact the server. Please check your network "
        + "connection and try again.";

    /**
     * Create a remote service proxy to talk to the server-side Greeting service.
     */
    private final QTLServiceAsync qtlService = GWT.create(QTLService.class);


    private Label mgiLabel = new Label("QTL Template by Phenotype from MGI:");
    private TextBox mgiTextBox = new TextBox();
    private Button mgiButton = new Button("Search MGI");
    private Label uploadLabel = new Label("Upload Custom QTL File:");
    private Button uploadButton = new Button("Upload");
    private final FileUpload upload = new FileUpload();
    private FlexTable qtlTable = new FlexTable();
    // Create the popup dialog box for use sending messages
    private final DialogBox dialogBox = new DialogBox();
    private final RadioButton gexRadio0 =
            new RadioButton("gexGroup", "Gene Expression Comparison:");
    private final RadioButton gexRadio1 =
            new RadioButton("gexGroup", "Upload RMA File:");
    private final ListBox gexListBox = new ListBox();
    private Button gexUploadButton = new Button("Define Exp. Desgn");
    private Button narrowButton = new Button("Narrow QTLs");
    private Button clearButton = new Button("Clear");
    private VerticalPanel resultsPanel = new VerticalPanel();

    private final DialogBox progressDialog = new DialogBox();
    private final ProgressBar progressBar = new ProgressBar(0.0,100.0,0.0);


    // Tracks the next row we'll add to our qtlTable
    // Start with row 1 as we won't count the header
    private int rowIndex = 1;

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

        final VerticalPanel masterPanel = new VerticalPanel();
        masterPanel.setSpacing(5);

        // Create a FormPanel and point it at a service.  This is strictly to
        // support file upload.
        final FormPanel form = new FormPanel();
        form.setAction(UPLOAD_ACTION_URL);

        // Because we're going to add a FileUpload widget, we'll need to set the
        // form to use the POST method, and multipart MIME encoding.
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);

        // Create a panel to hold all of the form widgets.  Again, this is being 
        // used to support file upload
        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(5);
        form.setWidget(panel);
        
        // All widgets have to be given names so they can be submitted via the form.
	// Again this is due to the use of the file upload.
	mgiTextBox.setName("mgi_text");
        
        //  This label is just to show the MGI button works
        final Label label = new Label("no value");
        HorizontalPanel mgiPanel = new HorizontalPanel();
	mgiPanel.setSpacing(5);
	mgiPanel.add(mgiLabel);
	mgiPanel.add(mgiTextBox);
	mgiPanel.add(mgiButton);
        label.setVisible(false);
	mgiPanel.add(label);
        panel.add(mgiPanel);

        // Create a FileUpload widget.
	
	upload.setName("uploadQTLFile");
        HorizontalPanel uploadPanel = new HorizontalPanel();
	uploadPanel.setSpacing(5);
	uploadPanel.add(uploadLabel);
        uploadPanel.add(upload);
        uploadPanel.add(uploadButton);
        //  This is used for the File Upload Servlet to be able to identify
        //  type of file uploaded
        Hidden qtlFileType = new Hidden("FileType","QTLFile");
        uploadPanel.add(qtlFileType);
      	panel.add(uploadPanel);

        masterPanel.add(form);

        initQtlTable();
        ScrollPanel scrollPanel = new ScrollPanel();
        qtlTable.setWidth("100%");
        scrollPanel.add(qtlTable);
        scrollPanel.setSize("650", "200");

        masterPanel.add(scrollPanel);

        //
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

        masterPanel.add(defaultGEXPanel);


        HorizontalPanel customGEXPanel = new HorizontalPanel();
        customGEXPanel.setSpacing(5);
        customGEXPanel.add(gexRadio1);

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
	upload.setName("uploadGEXFile");
        HorizontalPanel gexUploadPanel = new HorizontalPanel();
	gexUploadPanel.setSpacing(5);
        gexUploadPanel.add(gexUpload);
        gexUploadPanel.add(gexUploadButton);
        //  This is used for the File Upload Servlet to be able to identify
        //  type of file uploaded
        Hidden gexFileType = new Hidden("FileType","GEXFile");
        gexUploadPanel.add(gexFileType);
      	gexPanel.add(gexUploadPanel);

        customGEXPanel.add(gexForm);
        masterPanel.add(customGEXPanel);

        HorizontalPanel submitPanel = new HorizontalPanel();
        submitPanel.setSpacing(5);
        submitPanel.add(narrowButton);
        submitPanel.add(clearButton);

        masterPanel.add(submitPanel);



       	//  Due to File upload, we are adding the form directly to the panel
        RootPanel.get("formContainer").add(masterPanel);

        // Functionality for the MGI Button
        mgiButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                String text = mgiTextBox.getText().trim();
                if (! text.equals(""))
                    label.setText(text);
                label.setVisible(!label.isVisible());
            }
        });

        // Functionality for the Upload Button
        uploadButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                form.submit();
            }
        });
        
        // Create a dialog box for sending the user messages
        dialogBox.setText("QTL Narrowing");
        dialogBox.setAnimationEnabled(true);
        final Button closeButton = new Button("Close");
        // We can set the id of a widget by accessing its Element
        closeButton.getElement().setId("closeButton");
        final Label textForDialogLabel = new Label();
        final HTML htmlForDialogLabel = new HTML();
        VerticalPanel dialogVPanel = new VerticalPanel();
        dialogVPanel.addStyleName("dialogVPanel");
        dialogVPanel.add(new HTML("<b>Message:</b>"));
        dialogVPanel.add(textForDialogLabel);
        dialogVPanel.add(htmlForDialogLabel);
        dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
        dialogVPanel.add(closeButton);
        dialogBox.setWidget(dialogVPanel);

        // Add a handler to close the DialogBox
        closeButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                dialogBox.hide();
                uploadButton.setEnabled(true);
                gexUploadButton.setEnabled(true);
                narrowButton.setEnabled(true);
            }
        });

        //progressBar.setSize("100px", "25px");
        progressDialog.setText("Narrowing QTLs");
        //progressDialog.setAnimationEnabled(true);
        VerticalPanel progressPanel = new VerticalPanel();
        final Label progressLabel = new Label("Processing...");
        progressPanel.add(progressLabel);
        progressPanel.add(progressBar);
        progressBar.setTextVisible(false);

        progressDialog.setWidget(progressPanel);


        // Add an event handler for the upload form
        form.addSubmitHandler(new FormPanel.SubmitHandler() {

            public void onSubmit(SubmitEvent event) {
                // This event is fired just before the form is submitted. We can take
                // this opportunity to perform validation.
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
                    textForDialogLabel.setText(textForMessage);
                    htmlForDialogLabel.setText("");
                    dialogBox.center();
                    closeButton.setFocus(true);
                    uploadButton.setEnabled(true);
                    event.cancel();
                    return;
                }

            }
        });  // End of Submit Handler
        
        form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {

            public void onSubmitComplete(SubmitCompleteEvent event) {
                // When the form submission is successfully completed, this event is
                // fired. Assuming the service returned a response of type text/html,
                // we can get the result text here (see the FormPanel documentation for
                // further explanation).
                getQTLFile(event.getResults());
            }

            /**
             * upload our qtl file and get the contents from the server, then
             * load to our table.
             */
            private void getQTLFile(String outcome) {

                System.out.println("File qtl stored to on server: " +
                        outcome);


                textForDialogLabel.setText("");
                htmlForDialogLabel.setText("");

                qtlService.readQTLFile(new AsyncCallback<List<String[]>>() {

                    public void onFailure(Throwable caught) {
                        System.out.println("IN FAIL CASE");
                        // Show the RPC error message to the user
                        dialogBox.setText("Remote Procedure Call - Failure");
                        htmlForDialogLabel.addStyleName("serverResponseLabelError");
                        htmlForDialogLabel.setHTML(SERVER_ERROR + "<BR>" + caught.getMessage());
                        dialogBox.center();
                        closeButton.setFocus(true);
                    }

                    public void onSuccess(List<String[]> results) {
                        System.out.println("IN SUCCESS CASE");

                        for (Iterator<String[]> i = results.iterator(); i.hasNext();) {
                            String[] result = (String[]) i.next();
                            addRow(qtlTable, result);
                        }
                        applyDataRowStyles(qtlTable);
                        uploadButton.setEnabled(true);
                    }
                });
            }
        });

        //  Used to provide feedback on processing of qtl narrowing...
        final AsyncCallback statusCallback = new AsyncCallback<String>() {

            public void onFailure(Throwable caught) {
                // Show the RPC error message to the user
                dialogBox.setText("Unable to get status - Failure");
                htmlForDialogLabel.addStyleName("serverResponseLabelError");
                htmlForDialogLabel.setHTML(SERVER_ERROR + "<BR>" + caught.getMessage());
                dialogBox.center();
                closeButton.setFocus(true);
                progressBar.setProgress(100.0);
                progressLabel.setText("Done!");
            }

            public void onSuccess(String status) {

                progressLabel.setText(status);
                double progress = progressBar.getProgress();
                progressBar.setProgress(progress + (100 - progress) / 4);
                if (status.equals("Done!")) {
                    progressBar.setProgress(100.0);
                }
            }
        };

        // Functionality for the MGI Button
        narrowButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                narrowButton.setEnabled(false);

                ArrayList<List> qtls = new ArrayList<List>();
                //  Skip line 0 as it's a header row
                for (int i = 1; i < qtlTable.getRowCount(); i++) {
                    ArrayList row = new ArrayList();
                    for (int j = 0; j < qtlTable.getCellCount(i); j++) {
                        row.add(qtlTable.getText(i,j));
                    }
                    qtls.add((List)row);
                }

                Timer timer = new Timer() {

                    public void run() {
                        double progress = progressBar.getProgress();
                        if (progress >= 100.0) {
                            cancel();
                            progressDialog.hide();
                        } else if (progress == 0) {
                            progressBar.setProgress(1);
                        } else {
                            qtlService.getNarrowingStatus(statusCallback);
                        }
                    }
                };
                progressLabel.setText("Processing...");
                progressBar.setProgress(0);
                timer.scheduleRepeating(1000);
                progressDialog.center();


                System.out.println("Calling narrowQTLs");
                qtlService.narrowQTLs((List<List>)qtls, new AsyncCallback<Map<String, List<Region>>>() {

                    public void onFailure(Throwable caught) {
                        progressBar.setProgress(100.0);
                        System.out.println("IN FAIL CASE");
                        // Show the RPC error message to the user
                        dialogBox.setText("Remote Procedure Call - Failure");
                        htmlForDialogLabel.addStyleName("serverResponseLabelError");
                        htmlForDialogLabel.setHTML(SERVER_ERROR + "<BR>" + caught.getMessage());
                        dialogBox.center();
                        closeButton.setFocus(true);
                    }

                    public void onSuccess(Map<String, List<Region>> results) {
                        System.out.println("IN SUCCESS CASE");

                        FlexTable resultsTable = new FlexTable();
                        resultsTable.addStyleName("FlexTable");
                        resultsTable.insertRow(0);
                        //qtlTable.getRowFormatter().addStyleName(0,"FlexTable-Header");

                        addColumn(resultsTable, 0, "Chromosome");
                        addColumn(resultsTable, 0, "Build");
                        addColumn(resultsTable, 0, "Start");
                        addColumn(resultsTable, 0, "End");
                        addColumn(resultsTable, 0, "SNP ID");
                        addColumn(resultsTable, 0, "RS Num");
                        addColumn(resultsTable, 0, "B36");
                        addColumn(resultsTable, 0, "B37");
                        addColumn(resultsTable, 0, "HR<br>Base");
                        addColumn(resultsTable, 0, "LR<br>Base");
                        addColumn(resultsTable, 0, "HR Strains");
                        addColumn(resultsTable, 0, "LR Strains");

                        ScrollPanel scrollPanel = new ScrollPanel();
                        resultsTable.setWidth("100%");
                        scrollPanel.add(resultsTable);
                        scrollPanel.setSize("250", "200");

                        Set<String> keys = results.keySet();
                        int row = 1;
                        for (String key : keys) {
                            List<Region> regions = results.get(key);
                            int numSNPs = 0;
                            for (Region region : regions) {
                                List<SNP> snps = region.getSnps();
                                resultsTable.insertRow(row);
                                addColumn(resultsTable, row, key);
                                addColumn(resultsTable, row, region.getBuild());
                                addColumn(resultsTable, row, region.getStart());
                                addColumn(resultsTable, row, region.getEnd());
                                if (snps != null) {
                                    numSNPs = snps.size();
                                    if (numSNPs == 1) {
                                        SNP snp = snps.get(0);
                                        addColumn(resultsTable, row, snp.getSnpId());
                                        addColumn(resultsTable, row, snp.getRsNumber());
                                        addColumn(resultsTable, row, snp.getBuild36Position());
                                        addColumn(resultsTable, row, snp.getBuild37Position());
                                        addColumn(resultsTable, row, snp.getHighRespondingBaseValue());
                                        addColumn(resultsTable, row, snp.getLowRespondingBaseValue());
                                        if (snp.getHighRespondingStrains() != null) {
                                            addColumn(resultsTable, row, snp.getHighRespondingStrains().size());
                                            if (snp.getLowRespondingStrains() != null) {
                                                addColumn(resultsTable, row, snp.getHighRespondingStrains().size());
                                            }
                                        }
                                        row += 1;
                                    }
                                    else if (numSNPs > 1) {
                                        resultsTable.getFlexCellFormatter().setRowSpan(row, 0, numSNPs);
                                        resultsTable.getFlexCellFormatter().setRowSpan(row, 1, numSNPs);
                                        resultsTable.getFlexCellFormatter().setRowSpan(row, 2, numSNPs);
                                        resultsTable.getFlexCellFormatter().setRowSpan(row, 3, numSNPs);
                                        for (SNP snp: snps) {
                                            addColumn(resultsTable, row, snp.getSnpId());
                                            addColumn(resultsTable, row, snp.getRsNumber());
                                            addColumn(resultsTable, row, snp.getBuild36Position());
                                            addColumn(resultsTable, row, snp.getBuild37Position());
                                            addColumn(resultsTable, row, snp.getHighRespondingBaseValue());
                                            addColumn(resultsTable, row, snp.getLowRespondingBaseValue());
                                            if (snp.getHighRespondingStrains() != null) {
                                                addColumn(resultsTable, row, snp.getHighRespondingStrains().size());
                                                if (snp.getLowRespondingStrains() != null) {
                                                    addColumn(resultsTable, row, snp.getHighRespondingStrains().size());
                                                }
                                            }
                                            row += 1;
                                        }
                                    } else {
                                        row += 1;
                                    }
                                } else {
                                    row += 1;
                                }
                            }
                        }
                        applyDataRowStyles(resultsTable);
                        progressBar.setProgress(100.0);
                        resultsPanel.add(resultsTable);
			resultsPanel.setVisible(true);
			RootPanel.get("analysisResults").add(resultsPanel);
                        narrowButton.setEnabled(true);
                    }
                });
            }
        });

        // Functionality for the Clear Button
        clearButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                clear();
            }
        });



    }

  private void addColumn(FlexTable flexTable, int HeaderRowIndex,
          Object columnHeading) {
    Widget widget = createCellWidget(columnHeading);
    int cell = flexTable.getCellCount(HeaderRowIndex);

    widget.setWidth("100%");
    widget.addStyleName("FlexTable-ColumnLabel");

    flexTable.setWidget(HeaderRowIndex, cell, widget);

    flexTable.getCellFormatter().addStyleName(
        HeaderRowIndex, cell,"FlexTable-ColumnLabelCell");
  }

  private Widget createCellWidget(Object cellObject) {
    Widget widget = null;

    if (cellObject instanceof Widget)
      widget = (Widget) cellObject;
    else
      widget = new Label(cellObject.toString());

    return widget;
  }

  private void addRow(FlexTable flexTable, Object[] cellObjects) {

    for (int cell = 0; cell < cellObjects.length; cell++) {
      Widget widget = createCellWidget(cellObjects[cell]);
      flexTable.setWidget(rowIndex, cell, widget);
      flexTable.getCellFormatter().addStyleName(rowIndex,cell,"FlexTable-Cell");
    }
    rowIndex++;
  }

  private void applyDataRowStyles(FlexTable flexTable) {
    HTMLTable.RowFormatter rf = flexTable.getRowFormatter();

    for (int row = 1; row < flexTable.getRowCount(); ++row) {
      if ((row % 2) != 0) {
        rf.addStyleName(row, "FlexTable-OddRow");
      }
      else {
        rf.addStyleName(row, "FlexTable-EvenRow");
      }
    }
  }

  private void initQtlTable() {
        this.qtlTable.addStyleName("FlexTable");
        this.qtlTable.insertRow(0);
        //qtlTable.getRowFormatter().addStyleName(0,"FlexTable-Header");

        addColumn(this.qtlTable, 0, "QTL ID");
        addColumn(this.qtlTable, 0, "Phenotype");
        addColumn(this.qtlTable, 0, "Species");
        addColumn(this.qtlTable, 0, "High Resp Strain");
        addColumn(this.qtlTable, 0, "Low Resp Strain");
        addColumn(this.qtlTable, 0, "Chr");
        addColumn(this.qtlTable, 0, "QTL Start");
        addColumn(this.qtlTable, 0, "QTL End");
        addColumn(this.qtlTable, 0, "Bld");
  }

  private void clear () {
      this.mgiTextBox.setText("");
      this.mgiButton.setEnabled(true);
      this.uploadButton.setEnabled(true);
      int count = this.qtlTable.getRowCount();
      if (count > 1) {
          this.qtlTable.removeAllRows();
          initQtlTable();
      }
      gexRadio0.setValue(true);
      gexRadio1.setValue(false);
      gexUploadButton.setEnabled(true);
      narrowButton.setEnabled(true);
      clearButton.setEnabled(true);
      resultsPanel.setVisible(false);
  }
}
