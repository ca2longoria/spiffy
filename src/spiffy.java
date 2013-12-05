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
				
				PrintWriter pout = new PrintWriter(out);
				pout.println("WOAH!");
				pout.flush();
				
				return null;
			}
		};
		
		spiff.listen(spiffy.defaultPort);
	}
}
