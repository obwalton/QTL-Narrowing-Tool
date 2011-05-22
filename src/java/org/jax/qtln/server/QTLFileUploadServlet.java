/*
 * Copyright (c) 2010 The Jackson Laboratory
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jax.qtln.server;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

/**
 * servlet to handle file upload requests
 * 
 * @author hturksoy
 * 
 */
public class QTLFileUploadServlet extends HttpServlet {

    private static final String UPLOAD_DIRECTORY = "/tmp/";
    private static final String DEFAULT_TEMP_DIR = ".";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        System.out.println("In doPost");
        //  for debugging
        for (Enumeration e = req.getAttributeNames() ; e.hasMoreElements() ;) {
         System.out.println(e.nextElement());
        }

        // process only multipart requests
        if (ServletFileUpload.isMultipartContent(req)) {
            System.out.println("isMultipartContent!");

            File tempDir = getTempDir();
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            System.out.println("tmpDir setup");

            // Create a factory for disk-based file items
            FileItemFactory factory = new DiskFileItemFactory();

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);

            // Parse the request
            System.out.println("Parsing request...");
            try {
                List<FileItem> items = upload.parseRequest(req);
                for (FileItem fileItem : items) {
                    // process only file upload
                    if (fileItem.isFormField()) {
                        continue;
                    }

                    String fileName = fileItem.getName();
                    System.out.println("Filename = " + fileName);
                    // get only the file name not whole path
                    if (fileName != null && !fileName.equals("")) {
                        fileName = FilenameUtils.getName(fileName);
                    }
                    if (fileName.equals("")) {
                        HttpSession session = req.getSession(true);
                        //String fileType = (String)session.getAttribute("FileType");
                        //session.setAttribute(fileType, "");
                        session.setAttribute("QTLFile", "");
                        resp.setStatus(HttpServletResponse.SC_CREATED);
                        resp.getWriter().write("No file to upload.");
                    } else {
                        // Make name somewhat unique
                        long time = System.currentTimeMillis();
                        fileName += time;
                        File uploadedFile = new File(UPLOAD_DIRECTORY, fileName);
                        if (uploadedFile.createNewFile()) {
                            fileItem.write(uploadedFile);
                            HttpSession session = req.getSession(true);
                            //String fileType = (String)session.getAttribute("FileType");
                            //session.setAttribute(fileType, UPLOAD_DIRECTORY +
                            //        fileName);
                            session.setAttribute("QTLFile", UPLOAD_DIRECTORY +
                                    fileName);
                            resp.setStatus(HttpServletResponse.SC_CREATED);
                            resp.getWriter().write("File Successfully Uploaded!");
                            resp.flushBuffer();
                        } else {
                            throw new IOException("File already exists in " +
                                    "repository.");
                        }
                    }
                }
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "An error occurred while creating the file : " +
                        e.getMessage());
            }

        } else {
            System.out.println("Is Not Multipart content");
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    "Request contents type is not supported by the servlet.");
        }
        System.out.println("Done in upload Servlet");
    }

    private File getTempDir() {
        return new File(DEFAULT_TEMP_DIR);
    }
}
