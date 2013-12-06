package net.synapsehaven.spiffy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Spiffy
{
	public Spiffy()
	{
		
	}
	
	ServerSocket serverSock = null;
	
	public void listen(int port)
	{
		InputStream sin = null;
		OutputStream sout = null;
		
		try
		{
			serverSock = new ServerSocket(port);
			// 20-second forced timeout
			serverSock.setSoTimeout(20000);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		// Boolean check intended to break loop out if running in thread and
		// things in the main thread go awry.
		while (continueAcceptLoop())
		{	
			Socket sock = null;
			try {
				sock = serverSock.accept();
				sin = sock.getInputStream();
				sout = sock.getOutputStream();
			}
			catch (SocketTimeoutException e)
			{
				//System.err.println("serverSock natrual timeout: "+new Date());
				continue;
			}
			catch (IOException e){}
			
			handleConnection(sin,sout);
			
			try { sin.close(); sout.close(); sock.close(); }
			catch (IOException e)
			{
				System.err.println("ERROR: on sock.close()");
				e.printStackTrace();
			}
		}
		System.out.println("Spiffy listen (port "+port+") loop ended.");
	}
	
	abstract protected Object handleConnection(InputStream in, OutputStream out);
	abstract protected boolean continueAcceptLoop();
	
	
	public static class Web extends Spiffy
	{
		@Override
		protected Object handleConnection(InputStream in, OutputStream out)
		{
			RequestHeader reqHeader = null;
			
			System.out.println(">> handleConnection start");
			reqHeader = new RequestHeader(in);
			System.out.println(">> handleConnection end");
			
			return reqHeader;
		}
		
		public Boolean acceptLoopFlag = new Boolean(true);
		
		@Override
		protected boolean continueAcceptLoop()
		{
			return acceptLoopFlag.booleanValue();
		}
		
		public static enum RequestMethod
		{
			DELETE,
			GET,
			POST,
			PUT,
			NULL
		}
		
		public static class RequestHeader
		{
			public RequestHeader(InputStream in)
			{
				Pattern pmethod = Pattern.compile("^([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)$");
				Pattern pleft = Pattern.compile("^([^:]+).*");
				Pattern pright = Pattern.compile("^[^:]+:\\s*([^\\s].*)");
				
				RequestMethod method = null;
				String uri = null;
				String host = null;
				boolean success = false;
				
				@SuppressWarnings("resource")
				Scanner sc = new Scanner(in);
				while (sc.hasNextLine())
				{
					String line = sc.nextLine();
					if (line.length() == 0) break;
					
					System.out.println(line);
					// TODO: More stringent qualifications for success.
					success = true;
					
					// Hahaha!  Regex was so very unnecessary...
					Matcher mm = pmethod.matcher(line);
					Matcher ml = pleft.matcher(line);
					Matcher mr = pright.matcher(line);
					if (ml.find() && mr.find())
					{
						String left = ml.group(1);
						String right = mr.group(1);
						
						//System.out.println(left+" <> "+right+"\n");
						if (left.equalsIgnoreCase("host"))
							host = right;
					}
					else if (mm.find())
					{
						String methodString = mm.group(1);
						if (methodString.equals("GET"))
							method = RequestMethod.GET;
						else if (methodString.equals("POST"))
							method = RequestMethod.POST;
						// TODO: Implement the rest of the RequestMethods.
						
						uri = mm.group(2);
					}
				}
				// Stream remains *open*
				//sc.close();
				
				this.method = method;
				this.uri = uri;
				this.host = host;
				this.success = success;
			}
			public final RequestMethod method;
			public final String uri;
			public final String host;
			
			public final boolean success;
		}
	}
}
