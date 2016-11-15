package pda_servlet;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class UploadServlet extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public UploadServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
//		 processRequest(request, response);
		response.setContentType("text/html;charset=GBK"); 
		PrintWriter out = response.getWriter();
		System.out.println(request.getContentLength());
		System.out.println(request.getContentType());
		DiskFileItemFactory factory = new DiskFileItemFactory();
		//允许设置内存中存储数据的门限，单位：字节
		factory.setSizeThreshold(4096*1024);
		//如果文件大小大于SizeThreshold，则保存到临时目录
		factory.setRepository(new File("D:\\catv\\code\\defaultroot"));
		// Configure a repository (to ensure a secure temp location is used)
		ServletContext servletContext = this.getServletConfig().getServletContext();
		File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
		System.out.println(repository.getAbsolutePath());
		factory.setRepository(repository);
		ServletFileUpload upload = new ServletFileUpload(factory);
		//最大上传文件，单位：字节
		upload.setSizeMax(10000*1024*1024);
		try {
			List fileItems = upload.parseRequest(request);
			System.out.println("upload request size="+fileItems.size());
			Iterator iter = fileItems.iterator();
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();		
				// 忽略其他不是文件域的所有表单信息
				if (!item.isFormField()) {
					String name = item.getName();
					long size = item.getSize();
					System.out.println("name:"+name+"---size = "+size);
					if ((name == null || name.equals("") || "null".equalsIgnoreCase(name)) /*&& size == 0*/)
						continue;
					item.write(new File("D:\\catv\\code\\defaultroot\\" +name));
					out.print(name+"&nbsp;&nbsp;"+size+"<br>");
					//获取二进制流
					InputStream uploadedStream = item.getInputStream();
					uploadedStream.close();
					//获取二进制数组,用于保存到数据库
					byte[] datatemp=item.get();
				}else{
					//处理表单信息
					System.out.println(item.getFieldName()+"="+URLDecoder.decode(item.getString())+"=====item string  "+item.getString());
				}
				
			}
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("exception === "+e.toString());
		}
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}
	
	 protected void processRequest(HttpServletRequest request, HttpServletResponse response)
	            throws ServletException, IOException {
	        response.setContentType("text/html;charset=UTF-8");
	        //读取请求Body
	        byte[] body = readBody(request);
	        //取得所有Body内容的字符串表示
	        String textBody = new String(body, "ISO-8859-1");
	        //取得上传的文件名称
	        String fileName = getFileName(textBody);
	        //取得文件开始与结束位置
	        Position p = getFilePosition(request, textBody);
	        //输出至文件
	        writeTo(fileName, body, p);
	    }

	    //构造类
	    class Position {

	        int begin;
	        int end;

	        public Position(int begin, int end) {
	            this.begin = begin;
	            this.end = end;
	        }
	    }

	    private byte[] readBody(HttpServletRequest request) throws IOException {
	        //获取请求文本字节长度
	        int formDataLength = request.getContentLength();
	        //取得ServletInputStream输入流对象
	        DataInputStream dataStream = new DataInputStream(request.getInputStream());
	        byte body[] = new byte[formDataLength];
	        int totalBytes = 0;
	        while (totalBytes < formDataLength) {
	            int bytes = dataStream.read(body, totalBytes, formDataLength);
	            totalBytes += bytes;
	        }
	        return body;
	    }

	    private Position getFilePosition(HttpServletRequest request, String textBody) throws IOException {
	        //取得文件区段边界信息
	        String contentType = request.getContentType();
	        String boundaryText = contentType.substring(contentType.lastIndexOf("=") + 1, contentType.length());
	        //取得实际上传文件的气势与结束位置
	        int pos = textBody.indexOf("filename=\"");
	        pos = textBody.indexOf("\n", pos) + 1;
	        pos = textBody.indexOf("\n", pos) + 1;
	        pos = textBody.indexOf("\n", pos) + 1;
	        int boundaryLoc = textBody.indexOf(boundaryText, pos) - 4;
	        int begin = ((textBody.substring(0, pos)).getBytes("ISO-8859-1")).length;
	        int end = ((textBody.substring(0, boundaryLoc)).getBytes("ISO-8859-1")).length;

	        return new Position(begin, end);
	    }

	    private String getFileName(String requestBody) {
	        String fileName = requestBody.substring(requestBody.indexOf("filename=\"") + 10);
	        fileName = fileName.substring(0, fileName.indexOf("\n"));
	        fileName = fileName.substring(fileName.indexOf("\n") + 1, fileName.indexOf("\""));

	        return fileName;
	    }

	    private void writeTo(String fileName, byte[] body, Position p) throws IOException {
	        FileOutputStream fileOutputStream = new FileOutputStream("e:/workspace/" + fileName);
	        fileOutputStream.write(body, p.begin, (p.end - p.begin));
	        fileOutputStream.flush();
	        fileOutputStream.close();
	    }

}
