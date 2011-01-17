package nexi.sengoku.easy;

import org.apache.log4j.Logger;

public class DebugHandler implements CmdHandler{

	private static final Logger logger = Logger.getLogger(DebugHandler.class);
			
	@Override
	public void execute(Context context) {
		logger.info("execute");
	}

}
