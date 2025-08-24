package com.wilp.bits.url;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

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
//Red
public class ReadWriteURLSSL {
	
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
	Row  row;

	
	String regex = "((http|https|www)://)(www.)?"
            + "[a-zA-Z0-9@:%._\\+~#?&//=]"
            + "{2,256}\\.[a-z]"
            + "{2,6}\\b([-a-zA-Z0-9@:%"
            + "._\\+~#?&//=]*)";
	
	
	public static void main(String[] args) throws Exception {

		ReadWriteURLSSL urls = new ReadWriteURLSSL();
		//String filename="D:/Gajendran/Python Virtual Environments/CyberSentinelX/MAH_20240121_153659_1.xlsx";
		String filename = args[0];
		// Reading URls, checking whether redirecting or not
		//File updatedFile =
urls.excelReadAndCheck(filename);
	System.out.println("---------------------Read and write operation done for URls------------------------");
	
	

	//	System.out.println("Wait!!!!");
		//Thread.sleep(5000);

			// Capturing the SSL start date and end date
		//urls.showCertInfo(filename);
			//System.out.println("---------------------Certificate datas operations done successfully------------------------");
	}
	
	
	//Reading excel, Checking redirection, Writing into excel
	public File excelReadAndCheck(String filename)throws Exception
	{
		input = new FileInputStream(filename);
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
		 header.createCell(3).setCellValue("RESPONSE URL");
		 header.createCell(4).setCellValue("STATUS CODES");
		 header.createCell(5).setCellValue("STATUS MESSAGE");
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
			
			System.out.println("-----------------------------------------------------------------");
			
			
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
				 
				 System.out.println("Request URL matching with Regex");
				 
				 requesturl = new URL(urltest);
					HttpURLConnection conn= (HttpURLConnection)requesturl.openConnection();
					conn.setReadTimeout(5000);
					conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
					conn.addRequestProperty("User-Agent", "Chrome");
					conn.addRequestProperty("Referer", "google.com");
					
					System.out.println((slno++)+ ")"+ "Request URL : "+requesturl);
					
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
					
				
					System.out.println("Response Code : "+status);
					System.out.println("Status Message : "+statusmessage);
					
					
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
						System.out.println("Redirected Url: "+updatedUrl);			
					}// redirect end
					
					Cell redirectedurl =row.createCell(3);
					redirectedurl.setCellValue(updatedUrl);

					Cell code = row.createCell(4);
					code.setCellValue(status);
					
					Cell message = row.createCell(5); 
					message.setCellValue(statusmessage);
 
					output = new FileOutputStream(filename);
					workbook.write(output);
					output.flush();
				}catch(Exception e)
				{
					System.out.println("Error occured : "+e);
				}
				
			}// if close
			
			/*
			 * If the request URL doesn't follow Regex expression, then the else block executes by manually 
			 * concatenating "http://" so that we can avoid "No Protocol" error.
			 * Again the http:// request_URL are been checked for redirection, status code, status message.
			 * 
			 */
			
			else
			{// else start
				 urltest="http://" +text;
				
				try
				{
				//Connection
					
					
				requesturl = new URL(urltest);
				HttpURLConnection conn= (HttpURLConnection)requesturl.openConnection();
				conn.setReadTimeout(50000);
				conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
				conn.addRequestProperty("User-Agent", "Chrome");
				conn.addRequestProperty("Referer", "google.com");
				
				System.out.println((slno++)+ ")"+ "Request URL : "+requesturl);
				
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
				
				
			
				System.out.println("Response Code : "+status);
				System.out.println("Status Message : "+statusmessage);
				
				
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
					System.out.println("Redirected Url: "+updatedUrl);			
					}// redirect end
			
				}catch(Exception e)
				{
					System.out.println("Error occured!!! "+e);
				}
				
			}
				// Redirected URL other than 300 series execution.Ex: 200, 404,403, 500, 503
			if(status == 200 || status ==403|| status ==404 || status ==503 || status ==500)	
				{
					System.out.println("Status code not in 300 series ");
					Cell code = row.createCell(4);
					code.setCellValue(status);
					

					Cell message = row.createCell(5);
					message.setCellValue(statusmessage);
					
					
					output = new FileOutputStream(filename);
					workbook.write(output);
					output.flush();
					
					System.out.println("Written into excel for status code which are not in 300 series");
				}
				
				//Here the 300x series redirected url are been checked for checking whether it is a real redirect.
				else
				{// if--1 start
					
					// Checking for same request and redirected url
					if(updatedUrl.startsWith("https://www.")&& updatedUrl.endsWith("/"))
						
					{	//if--2 start
						
						System.out.println("Entering checking request url vs redirected url in 'https://www.'");
							// Removing the last "/"
						lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
						
						//Removing "https://www."
						firstlineremove1 = lastlineremove.substring(12);
						
						//Incase the updated Url contains double slash. Removing 2nd "/"
						if(firstlineremove1.endsWith("/"))
						{// if--3 start
							System.out.println("New updated URL : "+firstlineremove1);
						firstlineremove1=	 firstlineremove1.substring(0, firstlineremove1.length() -1);
						
							System.out.println("Test 2 successfully completed. Protocols completely removed");
						}// if --3 ends
						
						
						/*Incase the request URL starts with 'www.'
						 * we are comparing whether the redirected url and request url are same by concatenating 'www.'in the
						 * updated url manually for verification purpose
						*/
						if(text.startsWith("www."))
						{//if -- 4 start
							firstlineremove1 = "www."+firstlineremove1;
							System.out.println("Entered 2nd if loop: "+firstlineremove1);
						
						}// if -- 4 ends
						
						System.out.println("Orginal excel url : "+text);
						System.out.println("Same request and redirected url in (https://www.) protocol  : "+firstlineremove1);
						
						
						
						}// if --2 ends
					
					
					else if(updatedUrl.startsWith("https://")&& updatedUrl.endsWith("/"))
					{
						System.out.println("Entered checking request url vs redirected url in 'https://' ");
							// Removing the last "/"
							lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
							//Removing "https://"
							firstlineremove1 = lastlineremove.substring(8);
							//Incase the updated Url contains double slash. Removing 2nd "/"
							if(firstlineremove1.endsWith("/"))
							{
								System.out.println("New updated URL : "+firstlineremove1);
								firstlineremove1=	 firstlineremove1.substring(0, firstlineremove1.length() -1);
								System.out.println("Test 2 successfully completed. Protocols completely removed : "+firstlineremove1);
							}
							
							/*Incase the request URL starts with 'www.'
							 * we are comparing whether the redirected url and request url are same by concatenating 'www.'in the
							 * updated url manually for verification purpose
							*/
							if(text.startsWith("www."))
							{
								firstlineremove1 = "www."+firstlineremove1;
								System.out.println("Entered 2nd if loop: "+firstlineremove1);
							}
						System.out.println("Orginal excel url : "+text);
						System.out.println("Same request and redirected url in (https://) protocol : "+firstlineremove1);		
					}//else if close
					
					else if( updatedUrl.startsWith("http://www.")&&  updatedUrl.endsWith("/"))
					{
						System.out.println("Entered checking request url vs redirected url in  'http://www.'");
						// Removing the last "/"
							lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
							//Removing "https://"
							firstlineremove1 = lastlineremove.substring(11);
							
							//Incase the updated Url contains double slash. Removing 2nd "/"
							if(firstlineremove1.endsWith("/"))
							{
								System.out.println("New updated URL : "+firstlineremove1);
							firstlineremove1=	 firstlineremove1.substring(0, firstlineremove1.length() -1);
								System.out.println("Test 2 successfully completed. Protocols completely removed: "+firstlineremove1);
							}
							
							
							/*Incase the request URL starts with 'www.'
							 * we are comparing whether the redirected url and request url are same by concatenating 'www.'in the
							 * updated url manually for verification purpose
							*/
							if(text.startsWith("www."))
							{
								firstlineremove1 = "www."+firstlineremove1;
								System.out.println("Entered 2nd if loop: "+firstlineremove1);
							}
					
						System.out.println("Orginal excel url : "+text);
						System.out.println("Same request and redirected url in (http://www.) protocol : "+firstlineremove1);		
					}//else if close
					

					else if(updatedUrl.startsWith("http://")&& updatedUrl.endsWith("/"))
					{
						System.out.println("Entered checking request url vs redirected url in 'http://'");
						
						// Removing the last "/"
							lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
						//Removing "https://"
							firstlineremove1 = lastlineremove.substring(7);
							
							//Incase the updated Url contains double slash. Removing 2nd "/"
							if(firstlineremove1.endsWith("/"))
							{
								System.out.println("New updated URL : "+firstlineremove1);
							firstlineremove1=	 firstlineremove1.substring(0, firstlineremove1.length() -1);
								System.out.println("Test 2 successfully completed. Protocols completely removed :"+firstlineremove1);
							}
							
							/*Incase the request URL starts with 'www.'
							 * we are comparing whether the redirected url and request url are same by concatenating 'www.'in the
							 * updated url manually for verification purpose
							*/
							if(text.startsWith("www."))
							{
								firstlineremove1 = "www."+firstlineremove1;
								System.out.println("Entered 2nd if loop: "+firstlineremove1);
							}
						System.out.println("Orginal excel url : "+text);
						System.out.println("Same request and redirected url in (http://) protocol : "+firstlineremove1);		
					}// else if close
					else
					{
						System.out.println("protocols not removed as it is redirected URL: "+updatedUrl);
						firstlineremove1 = updatedUrl;
					}
					
				//}// else close
					
					//If request URL and redirected URL are different 
					//else 
				
						//firstlineremove1 = updatedUrl;
					//	System.out.println("Request URL and Redirected URL are different : "+firstlineremove1);
					
		
					
					
					// Writing into excel file
					if(firstlineremove1.equalsIgnoreCase(text))
					{
						
						System.out.println("Orginal excel url : "+text);
						System.out.println("Same request and redirect url so (EXCEL NOT WRITING ): " +firstlineremove1);
						
					}
					
					else
					{
				Cell redirectedurl =row.createCell(3);
				redirectedurl.setCellValue(updatedUrl);
					}
					
					
				Cell code = row.createCell(4);
				code.setCellValue(status);
				

				Cell message = row.createCell(5);
				message.setCellValue(statusmessage);
				
				
				

				output = new FileOutputStream(filename);
				workbook.write(output);
				output.flush();
				
				System.out.println("Successfully written");

				
				
			//}
		}		
		}
		System.out.println("===========================================================");
		System.out.println("excelReadAndCheck method  done Successfully !!!");
		
		// close resources
		if (workbook != null) workbook.close();
		if (input != null) input.close();
		if (output != null) output.close();

		// return updated file
		return new File(filename);
	}

	
	
	
	//Reading excel, Checking SSL certificate start date and end date, Writing into excel
		public void showCertInfo(String filename)throws Exception
		{
			input = new FileInputStream(filename);
			workbook = new XSSFWorkbook(input);
			sheet =workbook.getSheetAt(0);
			formatter= new DataFormatter();
			

			Row header = sheet.createRow(0);
			
			/* In excel column no. starts from 0.
			 * First column =0 index no.
			 * Second column =1 index no.
			 * Third column =2 index no.
			 */
			header.createCell(5).setCellValue("Start Date");
			header.createCell(6).setCellValue("End Date");
			header.createCell(7).setCellValue("Certificate Name");
			
			for(Row row1: sheet)

			{

				if(row1.getRowNum()==0)
			{
				continue;
			}
			
			Cell cellnum= row1.getCell(0);
			cellRef  = new CellReference(row1.getRowNum(), cellnum.getColumnIndex());
			
			
			text =formatter.formatCellValue(cellnum);
			
			if(text.matches(regex))
			{
				 urltest =text;
							
			}
			else
			{
				 urltest="http://" +text;

				try
				{
				requesturl = new URL(urltest);
				HttpURLConnection conn= (HttpURLConnection)requesturl.openConnection();
				conn.setReadTimeout(50000);
				conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
				conn.addRequestProperty("User-Agent", "Chrome");
				conn.addRequestProperty("Referer", "google.com");
				
				System.out.println((slno++)+ ")"+ "Request URL : "+requesturl);
				
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
						System.out.println("Status if block checked and passed");
						
					}
				
			
				System.out.println("Response Code : "+status);
				System.out.println("Status Message : "+statusmessage);
				
				
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
					System.out.println("Redirected Url: "+updatedUrl);		
					
					}// redirect end

				System.out.println("Updated Url after redirection process: "+updatedUrl);
				
				// If urls has status code 200, 403, 404, 503, 500, then if block will be executed
				if(status == 200 || status ==403|| status ==404 || status ==503 || status ==500)	
				{
					SocketFactory factory =SSLSocketFactory.getDefault();
					InetAddress address = InetAddress.getByName(text);
					SSLSocket socket = (SSLSocket) factory.createSocket(address.getHostName(), 443);
					socket.startHandshake();
					SSLSession session = socket.getSession();
					javax.security.cert.X509Certificate[] servercerts = session.getPeerCertificateChain();
				
				
				System.out.println("200, 400s, 500s block executed :  "+text);
				
				if(servercerts.length > 0)
				{
					String principal = servercerts[0].getSubjectDN().getName();
					startdate = servercerts[0].getNotBefore();
					enddate =servercerts[0].getNotAfter();
					IssueTo= servercerts[0].getSubjectDN().toString();
					
					
					System.out.println("Start date: "+startdate);
					System.out.println("End date: "+enddate);
					System.out.println("Certificate Name(Issue To) : "+IssueTo);
				}
					
				}
				//if status code is 300 series then else block will be executed
				else
				{
				System.out.println("300s block started executing ");
				//Removing urls protocols and passing 
//				 if(updatedUrl.startsWith("https://www.")|| updatedUrl.endsWith("/"))
//					{	
//					lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
//					System.out.println("Gajednran Check 2: "+lastlineremove);
//					firstlineremove1 = lastlineremove.substring(12);
//					
//					System.out.println("Gajednran Check 2.1: "+firstlineremove1);
//					if(firstlineremove1.endsWith("/"))
//					{
//						System.out.println("New updated URL : "+firstlineremove1);
//					firstlineremove1=	 firstlineremove1.substring(0, firstlineremove1.length() -1);
//						System.out.println("Test 2 successfully completed. Protocols removed");
//					}
//					System.out.println(" 'https://www.' protocol block executed  : "+firstlineremove1);
//
//				}
				
				 if(updatedUrl.startsWith("https://")|| updatedUrl.endsWith("/"))
				{
					lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
					firstlineremove1 = lastlineremove.substring(8);
					System.out.println("Gajendran Check 1: "+firstlineremove1);
					if(firstlineremove1.endsWith("/"))
					{
						firstlineremove1= firstlineremove1.substring(0, firstlineremove1.length() -1);
						System.out.println("Test 2 successfully completed. Protocols removed");
					}
					System.out.println(" 'http://' protocol block  executed   :"+firstlineremove1);
				}
				else if(updatedUrl.startsWith("https://www.")|| updatedUrl.endsWith("/"))
					{	
					lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
					System.out.println("Gajednran Check 2: "+lastlineremove);
					firstlineremove1 = lastlineremove.substring(12);
					
					System.out.println("Gajednran Check 2.1: "+firstlineremove1);
					if(firstlineremove1.endsWith("/"))
					{
						System.out.println("New updated URL : "+firstlineremove1);
					firstlineremove1=	 firstlineremove1.substring(0, firstlineremove1.length() -1);
						System.out.println("Test 2 successfully completed. Protocols removed");
					}
					System.out.println(" 'https://www.' protocol block executed  : "+firstlineremove1);

				}
				
				else if(updatedUrl.startsWith("www.")|| updatedUrl.endsWith("/"))
				{
					lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
					firstlineremove1 = lastlineremove.substring(4);
					if(firstlineremove1.endsWith("/"))
					{
						firstlineremove1= firstlineremove1.substring(0, firstlineremove1.length() -1);
						System.out.println("Test 2 successfully completed. Protocols removed");
					}
					System.out.println(" 'www.' protocol block  executed  : "+firstlineremove1);
				}
				
				else if(updatedUrl.startsWith("http://www.")|| updatedUrl.endsWith("/"))
					
				{
					
					lastlineremove =updatedUrl.substring(0, firstlineremove1.length() -1);
					firstlineremove1 = lastlineremove.substring(11);
					if(firstlineremove1.endsWith("/"))
					{
						firstlineremove1= firstlineremove1.substring(0, updatedUrl.length() -1);
						System.out.println("Test 2 successfully completed. Protocols removed");
					}
					System.out.println(" 'http://www.' protocol block  executed : "+firstlineremove1);
				}
				
				else if(updatedUrl.startsWith("http://")|| updatedUrl.endsWith("/"))
				{
					
					lastlineremove =updatedUrl.substring(0, updatedUrl.length() -1);
					firstlineremove1 = lastlineremove.substring(7);
					if(firstlineremove1.endsWith("/"))
					{
						firstlineremove1= firstlineremove1.substring(0, updatedUrl.length() -1);
						System.out.println("Test 2 successfully completed. Protocols removed");
					}
					System.out.println(" 'http//' protocol block executed  :"+firstlineremove1  );
				}
					
				else
				{
					firstlineremove1 =updatedUrl;
					
					System.out.println("Else block executed : "+firstlineremove1);

					System.out.println(" Final Statement executed");
				}
					
					String firstline = "www." +firstlineremove1;
					System.out.println("After concatenating www: "+firstline);
					
				SocketFactory factory =SSLSocketFactory.getDefault();
				InetAddress address = InetAddress.getByName(firstline);
				SSLSocket socket = (SSLSocket) factory.createSocket(address.getHostName(), 443);
				socket.startHandshake();
				SSLSession session = socket.getSession();
				javax.security.cert.X509Certificate[] servercerts = session.getPeerCertificateChain();
				System.out.println("SSH Handshake done!!");
			
			
			
			
			if(servercerts.length > 0)
			{
				String principal = servercerts[0].getSubjectDN().getName();
				startdate = servercerts[0].getNotBefore();
				enddate =servercerts[0].getNotAfter();
				IssueTo= servercerts[0].getSubjectDN().toString();
				
				
				System.out.println("Start date: "+startdate);
				System.out.println("End date: "+enddate);
				System.out.println("Certificate Name(Issue To) : "+IssueTo);
			}
				}
			
			System.out.println("===================================================================================");
			
			//Writing into excel
			Cell startdate1 = row1.createCell(5);
			startdate1.setCellValue(startdate);
			
			Cell enddate1 =row1.createCell(6);
			enddate1.setCellValue(enddate);
			
			Cell issuername =row1.createCell(7);
			issuername.setCellValue(IssueTo);
			
			
			
			output = new FileOutputStream(filename);
			workbook.write(output);
			output.flush();
			
				System.out.println("Certificate checking done successfully");
			
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
			}
		}
		}
		
		
}