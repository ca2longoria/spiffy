import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import net.synapsehaven.spiffy.Spiffy;


public class spiffy
{
	public static final int defaultPort = 1337;
	
	public static void main(String[] args)
	{
		Spiffy spiff = new Spiffy.Web()
		{
			public Object handleConnection(InputStream in, OutputStream out)
			{
				RequestHeader rq = (RequestHeader)super.handleConnection(in, out);
				if (!rq.success) return null;
				
				System.out.println(rq.method);
				System.out.println(rq.uri);
				System.out.println(rq.host);
				
				String content = "<body>Hey there?</body>";
				
				PrintWriter pout = new PrintWriter(out);
				pout.println("HTTP/1.1 200 OK");
				pout.println("Content-Type: text/html");
				pout.println("Content-Length: "+content.length());
				pout.println();
				pout.println(content);
				pout.flush();
				
				return rq;
			}
		};
		
		spiff.listen(spiffy.defaultPort);
	}
}
