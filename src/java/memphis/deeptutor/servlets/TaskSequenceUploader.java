package memphis.deeptutor.servlets;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class TaskSequenceUploader extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String TMP_DIR_PATH = System
			.getProperty("java.io.tmpdir");
	private File tmpDir;
	private File taskSeqDestinationFolder;

	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		tmpDir = new File(TMP_DIR_PATH);
		if (!tmpDir.isDirectory())
		{
			throw new ServletException(TMP_DIR_PATH + " is not a directory");
		}

		taskSeqDestinationFolder = new File(
				memphis.deeptutor.singleton.ConfigManager.GetDataPath());
		if (!taskSeqDestinationFolder.isDirectory())
		{
			throw new ServletException(taskSeqDestinationFolder
					+ " is not a directory");
		}
	}

	@SuppressWarnings("rawtypes")
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{

		DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
		/*
		 * Set the size threshold, above which content will be stored on disk.
		 */
		fileItemFactory.setSizeThreshold(1 * 1024 * 1024); // 1 MB
		/*
		 * Set the temporary directory to store the uploaded files of size above
		 * threshold.
		 */
		fileItemFactory.setRepository(tmpDir);

		ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
		try
		{
			HttpSession session = request.getSession();

			/*
			 * Parse the request
			 */
			List items = uploadHandler.parseRequest(request);
			if (items != null && items.size() <= 0)
			{
				session.setAttribute("status", "No file was chosen to upload!");
			}
			else
			{
				Iterator itr = items.iterator();
				while (itr.hasNext())
				{
					FileItem item = (FileItem) itr.next();
					/*
					 * Handle Form Fields.
					 */
					if (item.isFormField())
					{
						// out.println("File Name = "+item.getFieldName()+", Value = "+item.getString());
					}
					else
					{
						// Handle Uploaded files.
						// out.println("Field Name = "+item.getFieldName()+
						// ", File Name = "+item.getName()+
						// ", Content type = "+item.getContentType()+
						// ", File Size = "+item.getSize());
						/*
						 * Write file to the ultimate location.
						 */
						File file = null;
						if (item.getName().endsWith(".xml"))
						{
							file = new File(taskSeqDestinationFolder,
									"experimentConfig.xml");
							item.write(file);
							session.setAttribute("status", item.getName()
									+ " is uploaded succesfully.");
						}
						else
							session.setAttribute("status",
									"Failed! It can only upload XML files.");
					}

				}
				String destPage = "/experiment";
				RequestDispatcher rd = getServletContext()
						.getRequestDispatcher(destPage);
				rd.forward(request, response);
			}
		}
		catch (FileUploadException ex)
		{
			log("Error encountered while parsing the request", ex);
		}
		catch (Exception ex)
		{
			log("Error encountered while uploading file", ex);
		}
	}

}