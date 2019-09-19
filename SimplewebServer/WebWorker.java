
import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;

public class WebWorker implements Runnable
{

String fileName;
private Socket socket;
int errorCode;
Date date = new Date();
DateFormat formatDate = DateFormat.getDateTimeInstance();

/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}

/**
* Worker thread starting point. Each worker handles just one HTTP
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   System.err.println("Handling connection...");
   try 
   {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      String pathName = readHTTPRequest(is);
      writeHTTPHeader(os, "text/html", pathName);
      writeContent(os, pathName);
      os.flush();
      socket.close();
   } 
   catch (Exception e) 
   {
      System.err.println("Output error: " + e);
   }
   
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
**/
private String readHTTPRequest(InputStream is)
{
   String line;
   String path = "";
   BufferedReader reader = new BufferedReader(new InputStreamReader(is));

   while (true) 
   {
      try 
      {
         while (!reader.ready()) Thread.sleep(1);
         line = reader.readLine();
         
         if (line.contains("GET ")) 
         {
			path = line.substring(4);
			for (int i = 0; i < path.length(); i++) 
			{
				if (path.charAt(i) == ' ')
				{
					path = path.substring(0,i);
				}
			}
			
			path = "." + path;
			System.err.println("Path collected: " + path);
		 }
         
         System.err.println("Request line: ("+line +")");
         if (line.length() == 0) 
         {
        	 break;
         }
      } 
      
      catch (Exception e) 
      {
         System.err.println("Request error: " + e);
         break;
      }
   }
   
   return path;
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType, String contentPath) throws Exception
{

    File contentFile = new File(contentPath);
    
    if(contentFile.exists()) 
    {
        os.write("HTTP/1.1 200 WORKING\n".getBytes());
        errorCode = 200;
    }
    
    else 
    {
        os.write("HTTP/1.1 404 ERROR\n".getBytes());
        System.err.println("ERROR: File " + contentFile.toString() + " does not exist!");
        errorCode = 404;
    }
    // Write all data of header
    os.write("Date: ".getBytes());
    os.write((formatDate.format(date)).getBytes());
    os.write("\n".getBytes());
    os.write("Server: Omar's Server\n".getBytes());
    os.write("Content-Length: 438\n".getBytes());
    os.write("Connection: close\n".getBytes());
    os.write("Content-Type: ".getBytes());
    os.write(contentType.getBytes());
    os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
    return;
}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os, String contentPath) throws Exception
{
	String content = "";
	String pathy = contentPath;
	
	if (errorCode == 200) 
	{
        File fileName = new File(pathy);
		BufferedReader buffer = new BufferedReader(new FileReader(fileName));
 		while ((content = buffer.readLine()) != null) 
 		{
 			if(content.contains("<cs371date>"))
 			{
 				content += formatDate.format(date); 
 			}
 			
 			else if(content.contains("<cs371server>"))
 			{
 				content += "TAGGY"; 
 			}
 			
 			os.write(content.getBytes());
 			os.write( "\n".getBytes());
 		}
    } 
	
	else
	{
		os.write("<body bgcolor = \"#c5dbe8\">".getBytes());
	    os.write("<h1><b>404: Not Found</b></h1>".getBytes());
	    os.write("The page you are looking for does not exist!".getBytes());
	}
}

} // end class
