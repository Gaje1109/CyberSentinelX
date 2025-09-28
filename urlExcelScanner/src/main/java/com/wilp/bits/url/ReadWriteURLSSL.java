package com.wilp.bits.url;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ReadWriteURLSSL {
	
	
	private static final Logger readWriteURLSSL = Logger.getLogger(ReadWriteURLSSL.class.getName());
	String methodsName="";
	XSSFWorkbook workbook;
	XSSFSheet sheet;
	FileInputStream input;
	FileOutputStream output;
	DataFormatter formatter ;
	CellReference cellRef;
	String text;
	String updatedUrl;
	int slno=1;
	String statusmessage = null;
	int status = 0;
	String urltest;
	String urltest1;
	URL requesturl;
	Date startdate = null ;
	Date enddate = null;
	boolean redirect =false;
	String lastlineremove;
	String firstlineremove1;
	String firstlineremove2;
	String IssueTo ;
	String Domainname;
	String reupdatedUrl;
	Row row;

	
	String regex = "((http|https|www)://)(www.)?"
            + "[a-zA-Z0-9@:%._\\+~#?&//=]"
            + "{2,256}\\.[a-z]"
            + "{2,6}\\b([-a-zA-Z0-9@:%"
            + "._\\+~#?&//=]*)";
	
	public static void main(String[] args) throws Exception {
		
		ReadWriteURLSSL urls = new ReadWriteURLSSL();
		File inputFile = new File(args[0]);
		File updatedFile ;
		File outputFile;
		
		if(args.length < 1)
		{
			System.out.println("Usage: Input file not present");		
		}
		if (!inputFile.exists()) {
            System.err.println("Input file not found: " + inputFile.getAbsolutePath());
            System.exit(2);
        }
		updatedFile = urls.excelReadAndCheck(inputFile);
		String abs = updatedFile.getAbsolutePath().replace("\\", "\\\\"); // Windows escaping for JSON
        System.out.println("{\"output_path\":\"" + abs + "\"}");
		//outputFile = new File()
		
	}
	
	//Reading excel, Checking redirection, Writing into excel
	public File excelReadAndCheck(File filename)throws Exception
	{
		methodsName="excelReadAndCheck()";
		readWriteURLSSL.info("Inside "+methodsName+" -- Start ");
		input = new FileInputStream(filename);
		readWriteURLSSL.info("Filename: "+filename);
		workbook = new XSSFWorkbook(input);
		sheet= workbook.getSheetAt(0);
		formatter = new DataFormatter();
		Row header = sheet.createRow(0);
		
		/* In excel column no. starts from 0.
		 * First column =0 index no.
		 * Second column =1 index no.
		 * Third column =2 index no.
		 * 
		 */
		 //Headers
		header.createCell(0).setCellValue("REQUEST URL");
		header.createCell(1).setCellValue("PREDICTION");
		 header.createCell(2).setCellValue("RESPONSE URL");
		 header.createCell(3).setCellValue("STATUS CODES");
		 header.createCell(4).setCellValue("STATUS MESSAGE");
		// header.createCell(5).setCellValue("CERTIFICATE START DATE");
		 //header.createCell(6).setCellValue("CERTIFICATE END DATE");
		 //header.createCell(7).setCellValue("CERTIFICATE ISSUER DATA");
		for( Row row: sheet)
		{
			//Skipping the first row since they are headers
			if(row.getRowNum()==0)
			{
				continue;
			}
			
			Cell cellnum= row.getCell(0);
			cellRef  = new CellReference(row.getRowNum(), cellnum.getColumnIndex());
			//cellnum.setCellFormula(");
			
			text =formatter.formatCellValue(cellnum);
			
			readWriteURLSSL.info("*****************************************URL VALIDATION ELITE**************************************");
			
			
			//If orginal(Request Url) is matching with Regex
			if(text.matches(regex))
			{
				/*Here the request Url are been checked with regex expression. 
				 * If they match then this block will be executed. 
				 * In addition it will check the status code, status message and redirected Url if any. 
				 */
				
				try
				{
					urltest= text;
					 readWriteURLSSL.info("-----------------REGEX URLs VALIDATON STARTED---------------");
						
					readWriteURLSSL.info("Request URL matching with Regex");
				 
					//Redirection Status, Response Code, Redirected URL
				 	requesturl = new URL(urltest);
					HttpURLConnection conn= (HttpURLConnection)requesturl.openConnection();
					conn.setReadTimeout(5000);
					conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
					conn.addRequestProperty("User-Agent", "Chrome");
					conn.addRequestProperty("Referer", "google.com");
					
					readWriteURLSSL.info((slno++)+ ")"+ "Request URL : "+requesturl);
					
					//Getting the response code and response message
					status = conn.getResponseCode();
					statusmessage=conn.getResponseMessage();
				
						if (	
							status == HttpURLConnection.HTTP_MOVED_TEMP 
							|| status == HttpURLConnection.HTTP_SEE_OTHER
							|| status == HttpURLConnection.HTTP_MOVED_PERM
							||status ==HttpURLConnection.HTTP_MULT_CHOICE
							||status ==HttpURLConnection.HTTP_USE_PROXY
							||status ==HttpURLConnection.HTTP_NOT_MODIFIED
							||status ==HttpURLConnection.HTTP_OK)
						{
					
							redirect =true;
						}
					
				
					readWriteURLSSL.info("Response Code : "+status);
					readWriteURLSSL.info("Status Message : "+statusmessage);
					
//-----------------------------------------------------------------------------------------------------
					//Certification Dates and Names
					//certificateConfigurtions(firstlineremove1);
//--------------------------------------------------------------------------------------------------------------					
					if(redirect)
					{// redirect start
						
						// get redirect URL from "location" header field
						updatedUrl = conn.getHeaderField("Location");
						// get the cookie if need, for login
						String cookies = conn.getHeaderField("Set-Cookie");
						// open the new Connection again
						conn = (HttpURLConnection) new URL(updatedUrl).openConnection();
						conn.setRequestProperty("Cookie", cookies);
						conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
						conn.addRequestProperty("User-Agent", "Chrome");
						conn.addRequestProperty("Referer", "google.com");	
						readWriteURLSSL.info("REGEX REDIRECTION URL: "+updatedUrl);
//						if(updatedUrl.endsWith("/"))
//						{
//							updatedUrl=updatedUrl.substring(0, updatedUrl.length() -1);
//						}
						//certificateConfigurtions(updatedUrl);
					}// redirect end
					
					//writing into file
					//writeIntoXL(filename, row);
				}catch(Exception e)
				{
					readWriteURLSSL.info("Exception occured in " + methodsName + " : " + e);
				}
				
			}// if close
			
			/*
			 * If the request URL doesn't follow Regex expression, then the else block executes by manually 
			 * concatenating "http://" so that we can avoid "No Protocol" error.
			 * Again the http:// request_URL are been checked for redirection, status code, status message.
			 * 
			 */
			
			else
			{// Non-Redirection else statement start
				 urltest="http://" +text;
				 readWriteURLSSL.info("-----------------NON-REGEX URLs VALIDATON STARTED---------------");
					
				
				try
				{	
				requesturl = new URL(urltest);
				HttpURLConnection conn= (HttpURLConnection)requesturl.openConnection();
				conn.setReadTimeout(50000);
				conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
				conn.addRequestProperty("User-Agent", "Chrome");
				conn.addRequestProperty("Referer", "google.com");
				
				readWriteURLSSL.info((slno++)+ ")"+ "Request URL : "+requesturl);
				
				//Getting the response code and response message
				status = conn.getResponseCode();
				statusmessage=conn.getResponseMessage();
			
				if (	
						status == HttpURLConnection.HTTP_MOVED_TEMP 
						|| status == HttpURLConnection.HTTP_SEE_OTHER
						|| status == HttpURLConnection.HTTP_MOVED_PERM
						||status ==HttpURLConnection.HTTP_MULT_CHOICE
						||status ==HttpURLConnection.HTTP_USE_PROXY
						||status ==HttpURLConnection.HTTP_NOT_MODIFIED) {
				
						redirect =true;
					}
				
				
			
				readWriteURLSSL.info("Response Code : "+status);
				readWriteURLSSL.info("Status Message : "+statusmessage);
				
				
				if(redirect){// redirect start
					
					// get redirect URL from "location" header field
					updatedUrl = conn.getHeaderField("Location");
					// get the cookie if need, for login
					String cookies = conn.getHeaderField("Set-Cookie");
					// open the new Connection again
					conn = (HttpURLConnection) new URL(updatedUrl).openConnection();
					conn.setRequestProperty("Cookie", cookies);
					conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
					conn.addRequestProperty("User-Agent", "Chrome");
					conn.addRequestProperty("Referer", "google.com");	
					readWriteURLSSL.info("NON-REGEX REDIRECTION URL: "+updatedUrl);
//					if(updatedUrl.endsWith("/"))
//					{
//						updatedUrl=updatedUrl.substring(0, updatedUrl.length() -1);
//					}
					//certificateConfigurtions(updatedUrl);
							
					}// redirect end
					//writing into file
					//writeIntoXL(filename,row);
			
				}catch(Exception e)
				{
					readWriteURLSSL.info("Exception occured in " + methodsName + " : " + e);
				}
				
			}// Non-Redirection else statement end
			
//---------------------------------------------------------------------------------------------------------			
			// Redirected URL other than 300 series execution.Ex: 200, 404,403, 500, 503
			if(status == 200 || status ==403|| status ==404 || status ==503 || status ==500)	
				{
				readWriteURLSSL.info("-----------------200x,400x,500x VALIDATON STARTED---------------");
				
					readWriteURLSSL.info("Status code not in 300 series ");
					Cell redirectedurl =row.createCell(2);
					redirectedurl.setCellValue(updatedUrl);
					writeIntoXL(filename, row);
//					Cell code = row.createCell(2);
//					code.setCellValue(status);
//					
//
//					Cell message = row.createCell(3);
//					message.setCellValue(statusmessage);
//					
//					
//					output = new FileOutputStream(filename);
//					workbook.write(output);
//					output.flush();
					
					readWriteURLSSL.info("Written into excel for status code which are not in 300 series");
				}
//---------------------------------------------------------------------------------------------------------				
				//Here the 300x series redirected url are been checked for checking whether it is a real redirect.
				else
				{// 300x else statement start
					readWriteURLSSL.info("-----------------300X VALIDATON STARTED---------------");
//					System.out.println("URL is :"+urltest);
//					updatedUrl=urltest;
					// Checking for same request and redirected URL -- start
					if(updatedUrl.startsWith("https://www.")&& updatedUrl.endsWith("/"))
					{//Validation 1 start
					readWriteURLSSL.info("-----------------------------------------------------------------------");
					readWriteURLSSL.info("-----------VALIDATION 1: https://www.----------- ");
					
						// Removing the last "/"
						lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
						readWriteURLSSL.info("/ removed from end of the URL : "+lastlineremove);
						
						//Removing "https://www."
						firstlineremove1 = lastlineremove.replace("https://www.", "");
						readWriteURLSSL.info("https://www. removed from the URL : "+firstlineremove1);
						
						//In case the updated URL contains double slash. 
						//Removing 2nd "/"
						if(firstlineremove1.endsWith("/"))
						{// if statement for 2nd / start
						firstlineremove1=	 firstlineremove1.substring(0, firstlineremove1.length() -1);
						readWriteURLSSL.info("/ removed from end of the URL : "+firstlineremove1);	
						}// if statement for 2nd / end
						readWriteURLSSL.info("----Protocols completely removed----");
						
						
						/*Incase the request URL starts with 'www.'
						 * we are comparing whether the redirected url and request url are same by concatenating 'www.'in the
						 * updated url manually for verification purpose
						*/
						if(text.startsWith("www."))
						{//if statement for comparing request URL and response URL -- start
							//concatenating www. to the response URL
							firstlineremove1 = "www."+firstlineremove1;
							
							readWriteURLSSL.info("-----------VALIDATION 1.1: www.----------- " + firstlineremove1);
							if(text.equals(firstlineremove1) || text==firstlineremove1){
								readWriteURLSSL.info("Same Request and Redirected URL");
							}
							
						
						}//if statement for comparing request URL and response URL -- start
						readWriteURLSSL.info("Same request and redirected url in (https://www.) protocol  : "+firstlineremove1);
						readWriteURLSSL.info("-----------------------------------------------------------------------");
							
					}//Validation 1 end
					
//---------------------------------------------------------------------------------------------------------------------------------------------------------			
					
					else if(updatedUrl.startsWith("https://")&& updatedUrl.endsWith("/"))
					{// Validation 2 start
						readWriteURLSSL.info("-----------------------------------------------------------------------");
						readWriteURLSSL.info("-----------VALIDATION 2: https://----------- ");
						
							// Removing the last "/"
							lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
							readWriteURLSSL.info("/ removed from end of the URL : "+lastlineremove);
							//Removing "https://"
							firstlineremove1 = lastlineremove.replace("https://", "");
							readWriteURLSSL.info("https:// removed from the URL : "+firstlineremove1);
							
							//In case the updated URL contains double slash.
							//Removing 2nd "/"
							if(firstlineremove1.endsWith("/"))
							{
								firstlineremove1=	 firstlineremove1.substring(0, firstlineremove1.length() -1);
								readWriteURLSSL.info("/ removed from end of the URL : "+firstlineremove1);	
							}
							
							/*Incase the request URL starts with 'www.'
							 * we are comparing whether the redirected url and request url are same by concatenating 'www.'in the
							 * updated url manually for verification purpose
							*/
							if(text.startsWith("www."))
							{
								firstlineremove1 = "www."+firstlineremove1;
								readWriteURLSSL.info("-----------VALIDATION 2.1: www.----------- " + firstlineremove1);
								if(text.equals(firstlineremove1) || text==firstlineremove1){
									readWriteURLSSL.info("Same Request and Redirected URL");
								}
							}
						readWriteURLSSL.info("Same request and redirected url in (https://) protocol : "+firstlineremove1);		
						readWriteURLSSL.info("-----------------------------------------------------------------------");
					}// Validation 2 end
//---------------------------------------------------------------------------------------------------------------------------------------------------------			
					
					else if( updatedUrl.startsWith("http://www.")&&  updatedUrl.endsWith("/"))
					{//Validation 3 start
						readWriteURLSSL.info("-----------------------------------------------------------------------");
						readWriteURLSSL.info("-----------VALIDATION 3: http://www.----------- ");
						
							// Removing the last "/"
							lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
							readWriteURLSSL.info("/ removed from end of the URL : "+lastlineremove);
							//Removing "https://"
							firstlineremove1 = lastlineremove.replace("http://www.", "");
							readWriteURLSSL.info("http://www. removed from the URL : "+firstlineremove1);
							
							
							//In case the updated URL contains double slash. 
							//Removing 2nd "/"
							if(firstlineremove1.endsWith("/"))
							{
							firstlineremove1=	 firstlineremove1.substring(0, firstlineremove1.length() -1);
							readWriteURLSSL.info("/ removed from end of the URL : "+firstlineremove1);	
							}
							
							
							/*Incase the request URL starts with 'www.'
							 * we are comparing whether the redirected url and request url are same by concatenating 'www.'in the
							 * updated url manually for verification purpose
							*/
							if(text.startsWith("www."))
							{
								firstlineremove1 = "www."+firstlineremove1;
								readWriteURLSSL.info("-----------VALIDATION 3.1: www.----------- " + firstlineremove1);
								if(text.equals(firstlineremove1) || text==firstlineremove1){
									readWriteURLSSL.info("Same Request and Redirected URL");
								}
							}

						readWriteURLSSL.info("Same request and redirected url in (http://www.) protocol : "+firstlineremove1);		
						readWriteURLSSL.info("-----------------------------------------------------------------------");
						
					}// Validation 3 end
//---------------------------------------------------------------------------------------------------------------------------------------------------------			
					

					else if(updatedUrl.startsWith("http://")&& updatedUrl.endsWith("/"))
					{//Validation 4
						readWriteURLSSL.info("-----------------------------------------------------------------------");
						readWriteURLSSL.info("-----------VALIDATION 4: http://----------- ");
						
						// Removing the last "/"
							lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
							readWriteURLSSL.info("/ removed from end of the URL : "+lastlineremove);
						//Removing "https://"
							firstlineremove1 = lastlineremove.replace("http://", "");
							readWriteURLSSL.info("http://www. removed from the URL : "+firstlineremove1);
							
							
							//Incase the updated Url contains double slash. Removing 2nd "/"
							if(firstlineremove1.endsWith("/"))
							{
							firstlineremove1=	 firstlineremove1.substring(0, firstlineremove1.length() -1);
							readWriteURLSSL.info("/ removed from end of the URL : "+firstlineremove1);	
							}
							
							/*Incase the request URL starts with 'www.'
							 * we are comparing whether the redirected url and request url are same by concatenating 'www.'in the
							 * updated url manually for verification purpose
							*/
							if(text.startsWith("www."))
							{
								firstlineremove1 = "www."+firstlineremove1;
								readWriteURLSSL.info("-----------VALIDATION 4.1: www.----------- " + firstlineremove1);
								if(text.equals(firstlineremove1) || text==firstlineremove1){
									readWriteURLSSL.info("Same Request and Redirected URL");
								}
							}
						readWriteURLSSL.info("Same request and redirected url in (http://) protocol : "+firstlineremove1);		
						readWriteURLSSL.info("-----------------------------------------------------------------------");
				
					}// Validation 4 end
					//Else statement
					else
					{
						readWriteURLSSL.info("protocols not removed as it is redirected URL: "+updatedUrl);
						firstlineremove1=updatedUrl;
					}
					
//---------------------------------------------------------------------------------------------------------------------------------------------------------			
// Checking for same request and redirected URL -- end
					readWriteURLSSL.info(updatedUrl+ "    "+firstlineremove1);
					readWriteURLSSL.info("-----------VALIDATION COMPLETED SUCESSFULLY------------------");
					
					/*
					 * If request URL and response URL are matching and same, we are not writing into excel file
					 * as per requirement and also because they are same URLs
					 */
					if(firstlineremove1.equals(text)||updatedUrl.equals(text))
					{
					readWriteURLSSL.info("REQUEST URL: "+text+"------------"+"RESPONSE URL: "+firstlineremove1);
					readWriteURLSSL.info("Same REQUEST and RESPONSE/REDIRECT URL -------EXCEL NOT WRITING------");
					certificateConfigurtions(firstlineremove1);
					writeIntoXL(filename, row);
					}
					/*
					 * If request URL and response URL are not matching, we are  writing into excel file
					 * as per requirement
					 */
					else
					{
						Cell redirectedurl =row.createCell(2);
						redirectedurl.setCellValue(updatedUrl);
						certificateConfigurtions(firstlineremove1);
						//writing into file
						writeIntoXL(filename, row);
						readWriteURLSSL.info("Successfully written");
				}//else close 
		}		//300x statement end
		
		}// for loop end
		readWriteURLSSL.info("===========================================================");
		readWriteURLSSL.info("excelReadAndCheck method  done Successfully !!!");
		readWriteURLSSL.info("Inside "+methodsName+" -- End ");
		return filename;
}
	
	private void certificateConfigurtions(String responseURL)
	{
		methodsName="certificateConfigurtions()";
		try{
			readWriteURLSSL.info("Inside "+methodsName+" -- Start ");
			
		SocketFactory factory =SSLSocketFactory.getDefault();
		InetAddress address = InetAddress.getByName(responseURL);
		SSLSocket socket = (SSLSocket) factory.createSocket(address.getHostName(), 443);
		socket.startHandshake();
		SSLSession session = socket.getSession();
		javax.security.cert.X509Certificate[] servercerts = session.getPeerCertificateChain();
				if(servercerts.length > 0)
				{
					String principal = servercerts[0].getSubjectDN().getName();
					startdate = servercerts[0].getNotBefore();
					enddate =servercerts[0].getNotAfter();
					IssueTo= servercerts[0].getSubjectDN().toString();
					
					
					readWriteURLSSL.info("Start date: "+startdate);
					readWriteURLSSL.info("End date: "+enddate);
					readWriteURLSSL.info("Certificate Name(Issue To) : "+IssueTo);
					}
		}catch(Exception e)
		{
			readWriteURLSSL.info("Exception occured in " + methodsName + " : " + e);
		}
		
	
		readWriteURLSSL.info("Inside "+methodsName+" -- End ");
	}
	
	private void writeIntoXL(File fileName, Row row)
	{	FileOutputStream fos=null;
		methodsName="writeIntoXL()";
		try {
			readWriteURLSSL.info("Inside "+methodsName+" -- Start ");
			fos = new FileOutputStream(fileName);
			readWriteURLSSL.info("----------WRITING DATA INTO XL FILE----------");
			readWriteURLSSL.info("Update URL : "+updatedUrl);
			readWriteURLSSL.info("Response Status : "+status);
			readWriteURLSSL.info("Response Message: "+statusmessage);
			
			readWriteURLSSL.info("Start date: "+startdate);
			readWriteURLSSL.info("End date: "+enddate);
			readWriteURLSSL.info("Issuer Name : "+IssueTo);

			Cell code = row.createCell(3);
			code.setCellValue(status);
			
			Cell message = row.createCell(4);
			message.setCellValue(statusmessage);

//			Cell startdate1 = row.createCell(5);
//			startdate1.setCellValue(startdate);
//			
//			Cell enddate1 =row.createCell(6);
//			enddate1.setCellValue(enddate);
//			
//			Cell issuername =row.createCell(7);
//			issuername.setCellValue(IssueTo);	
			workbook.write(fos);
			fos.flush();
			

		} catch (FileNotFoundException e) {
			readWriteURLSSL.info("Exception occured in " + methodsName + " : " + e);
		} catch (IOException e) {
			readWriteURLSSL.info("Exception occured in " + methodsName + " : " + e);
		}finally
		{
			try {
				fos.close();
			} catch (IOException e) {
				readWriteURLSSL.info("Exception occured in " + methodsName + " : " + e);
			}
		}
		readWriteURLSSL.info("Inside "+methodsName+" -- End ");
		
	}
}
	
		
		