package ru.aeltsov.wateredimage;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        WIProcessor wiProcessor = null;
    	
    	if (args.length == 1){
        	wiProcessor = new WIProcessor(args[0], "", "", 0.0f);
        }
    	
    	if (args.length == 2){
        	wiProcessor = new WIProcessor(args[0], args[1], "", 0.0f);
        }
    	
    	if (args.length == 3){
        	wiProcessor = new WIProcessor(args[0], args[1], args[2], 0.0f);
        }
    	
    	if (args.length == 4){
        	wiProcessor = new WIProcessor(args[0], args[1], args[2], Float.parseFloat(args[3]));
        }
    	
    	try {
			wiProcessor.process();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
